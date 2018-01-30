import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.kotlin
import org.gradle.kotlin.dsl.repositories
import org.gradle.kotlin.dsl.setValue
import de.mannodermaus.gradle.plugins.junit5.*
import org.junit.platform.console.options.Details


plugins {
    id("android-library")
    kotlin("android")
    id("de.mannodermaus.android-junit5")
}


android {
    compileSdkVersion(compile_sdk_version)

    defaultConfig {
        minSdkVersion(min_sdk_version)
        targetSdkVersion(target_sdk_version)
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles("proguard-rules.pro")
        }

        getByName("debug") {
            isTestCoverageEnabled = true
        }
    }

    lintOptions {
        disable = setOf("UnusedResources")
    }

    testOptions {
        junitPlatform.apply {
            details = Details.TREE

        }
    }
}

dependencies {
    api("com.android.support:appcompat-v7:$supportlib_version")
    api("org.jetbrains.kotlin:kotlin-stdlib-jre7:$kotlin_version")
    api("com.android.databinding:library:$databinding_version")
    testImplementation("org.amshove.kluent:kluent:$kluent_version")
    testImplementation("com.github.dpreussler:KuperReflect:$kuper_reflect_version")

    (dependencies.ext["junit5"] as JUnit5DependencyHandler).apply {
        unitTests().forEach { testImplementation(it) }
        parameterized().forEach { testImplementation(it) }
        unitTestsRuntime().forEach{ testCompileOnly(it) }  // workaround for AS3.0
    }

}
