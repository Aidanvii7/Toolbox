// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {

    repositories {
        google()
        jcenter()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:$android_gradle_plugin_version")
        classpath( "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version")
        classpath( "de.mannodermaus.gradle.plugins:android-junit5:$junit5_plugin_version")
    }
}

allprojects {
    repositories {
        mavenCentral()
        jcenter()
        google()
        maven (url = "https://jitpack.io")
    }
}

