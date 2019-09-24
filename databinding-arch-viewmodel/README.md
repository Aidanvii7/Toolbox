[![CircleCI](https://circleci.com/gh/Aidanvii7/Toolbox.svg?style=svg)](https://circleci.com/gh/Aidanvii7/Toolbox)
[![](https://jitpack.io/v/Aidanvii7/Toolbox.svg)](https://jitpack.io/#Aidanvii7/Toolbox)

# Observable Architecture Components ViewModel
Provides an [`Observable`](https://developer.android.com/reference/android/databinding/Observable.html) implementation of the [`ViewModel`](https://developer.android.com/topic/libraries/architecture/viewmodel.html) class from the architecture components library, called [`ObservableArchViewModel`](databinding-arch-viewmodel/src/main/java/com/aidanvii/toolbox/databinding/ObservableArchViewModel.kt).
This class should be used in cases where you want to use [observable property delegates](delegates-observable-databinding/README.md) on a [`ViewModel`](https://developer.android.com/topic/libraries/architecture/viewmodel.html).

Example:
```kotlin
class ViewModel : ObservableArchViewModel() {
    
    @get:Bindable
    var expanded by bindable(false)
    ...
}
```

# Setup
 Add the JitPack repository to your build file: 

```gradle
repositories {
  ..
  maven { url 'https://jitpack.io' }    
}
```

In your module's build.gradle, add:
```gradle

android {
  ..
  dataBinding.enabled = true
}

..
dependencies {
  ..
  implementation "com.github.Aidanvii7.Toolbox:databinding-arch-viewmodel:$toolbox_version"
}

```
