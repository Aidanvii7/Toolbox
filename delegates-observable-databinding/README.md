[![CircleCI](https://circleci.com/gh/Aidanvii7/Toolbox.svg?style=svg)](https://circleci.com/gh/Aidanvii7/Toolbox)
[![](https://jitpack.io/v/Aidanvii7/Toolbox.svg)](https://jitpack.io/#Aidanvii7/Toolbox)

# Observable property delegates for Android Databinding
Provides an alternative way to express observable view model properties to views using primitive data binding constructs like [`Observable`](https://developer.android.com/reference/android/databinding/Observable) as opposed to [`LiveData`](https://developer.android.com/topic/libraries/architecture/livedata).

If you're not using data binding and prefer to bind your views and view models manually, then this solution won't work for you. 


## Why use this over [`LiveData`](https://developer.android.com/topic/libraries/architecture/livedata) to bind views and view models?
[`LiveData`](https://developer.android.com/topic/libraries/architecture/livedata) can be used to transport data throughout an application with lifecycle awareness built in, not just for keeping views and view models in sync. [`LiveData`](https://developer.android.com/topic/libraries/architecture/livedata) is able to prevent views from updating when the observed [`LifecycleOwner`](https://developer.android.com/reference/android/arch/lifecycle/LifecycleOwner) is stopped/backgrounded. [`LiveData`](https://developer.android.com/topic/libraries/architecture/livedata) integrates with the data binding library as described [here](https://developer.android.com/topic/libraries/data-binding/architecture#livedata). It requires passing a [`LifecycleOwner`](https://developer.android.com/reference/android/arch/lifecycle/LifecycleOwner) to generated sub classes of [`ViewDataBinding`](https://developer.android.com/reference/android/databinding/ViewDataBinding). It uses this in conjunction with any bound [`LiveData`](https://developer.android.com/topic/libraries/architecture/livedata) objects on your view model to ensure the view will never be updated while the app is not started/foregrounded. 

What they wont tell you is that standard [`Observable`](https://developer.android.com/reference/android/databinding/Observable) objects are also prevented from updating views when a [`LifecycleOwner`](https://developer.android.com/reference/android/arch/lifecycle/LifecycleOwner) is passed to generated sub classes of [`ViewDataBinding`](https://developer.android.com/reference/android/databinding/ViewDataBinding). This fact renders the argument to use [`LiveData`](https://developer.android.com/topic/libraries/architecture/livedata) to bind views and view models somewhat obselete.

It also doesn't allow us to express the contract of a view model in an idiomatic way.

Consider this, you have a view model that loads some things from it's data source and you want to express it's `loading` state to a view. You want the view model's `loading` state to be expressed as a `Boolean` value, and want to make it clear that `loading` can only be changed internally (private setter). With [`LiveData`](https://developer.android.com/topic/libraries/architecture/livedata) you would do something like:
```kotlin
class MyViewModel : ViewModel() {

    private val _loading = MutableLiveData<Boolean>().apply { value = false }
    val loading: LiveData<Boolean> get() = _loading

    //.. _loading mutated somewhere here
}
```
It takes longer than it should to understand the intent of the code above. It fits the original goal, but it isn't great Kotlin code. It's also based on example code from the Google IO app ([here](https://github.com/google/iosched/blob/89df01ebc19d9a46495baac4690c2ebfa74946dc/mobile/src/main/java/com/google/samples/apps/iosched/ui/info/EventInfoViewModel.kt) and [here](https://github.com/google/iosched/blob/89df01ebc19d9a46495baac4690c2ebfa74946dc/mobile/src/main/java/com/google/samples/apps/iosched/ui/onboarding/OnboardingViewModel.kt])). This is boilerplate that doesn't scale well as view models gain more properties. And while it's [considered acceptable](https://kotlinlang.org/docs/reference/coding-conventions.html#property-names) to use underscores for backing fields, the fact that it's mutability can't be expressed with visibility modifiers doesn't seem very Kotlin friendly. On top of that, it doesn't read particularly great when accessing the boxed values as they have to be 'unwrapped' with `_loading.value`.

What we ideally want is something like:

```kotlin
class MyViewModel : ViewModel() {

    var loading = false
        private set

    //.. loading mutated somewhere here
}
```
Now `loading` can still be read from the view, but mutated internally. Accessing the value of the property also doesn't require 'unwrapping' it with `.value`. 

Unfortunitely this still isn't enough as mutating `loading` will not trigger databinding to rebind. By following the instructions [here](https://developer.android.com/topic/libraries/data-binding/architecture#observable-viewmodel), you can build your own `ObservableViewModel` that implements [`Observable`](https://developer.android.com/reference/android/databinding/Observable). Then you can do something like this:

```kotlin
class MyViewModel : ObservableArchViewModel() {

    @Bindable
    var loading = false
        private set(value) {
            if (field != value) {
                field = value
                notifyPropertyChanged(BR.loading)
            }
        }

    //.. loading mutated somewhere here
}
```

This will handle notifying databinding when a new value is set, but it's still a lot of boilerplate to write for every property you want to bind to a view.

The answer to this problem is [delegated properties](https://kotlinlang.org/docs/reference/delegated-properties.html). 
With some tricks described in greater detail [here](https://android.jlelse.eu/make-your-view-models-great-again-ead9ee98f4f2), it allows us to extract the logic in the setter elsewhere and ultimately be able to do this:
```kotlin
class MyViewModel : ObservableViewModel() {

    @get:Bindable
    var loading by bindable(false)
        private set

    //.. loading mutated somewhere here
}
```

# Usage

If you've read this far, then you already know how to use it. It is worth noting that this is an extension of the [observable delegates](../delegates-observable/README.md) package, so all decorator functions from it work here too.

Two types of delegates exist, `bindable(..)` and `bindableEvent(..)`.
* `bindable(..)` will only propagate values downstream and notify data binding if the value given is different from the previous value.
* `bindableEvent(..)` will propagate values downstream and notify data binding even if the given value is the same as the previous value.

Please see the [documentation](src/main/java/com/aidanvii/toolbox/databinding/BindableProperty.kt) of each for a deeper explanation and usage suggestions.

Both of these delegates can only be used as member properties of [`NotifiableObservable`](../databinding/src/main/java/com/aidanvii/toolbox/databinding/NotifiableObservable.kt) implementations, such as:

```kotlin
class ViewModel : NotifiableObservable by NotifiableObservable.delegate() {

    init {
        initDelegator(this) // required
    }
    
    @get:Bindable
    var expanded by bindable(false)
            .eager()
            .doOnNext { /* Do something */ }

    @get:Bindable
    var showToast by bindableEvent(false)
            .distinctUntilChanged()
            .doOnNext { /* do something with distinct value */ }
}
```

A base class exists for [`NotifiableObservable`](../databinding/src/main/java/com/aidanvii/toolbox/databinding/NotifiableObservable.kt) which handles some of the boilerplate, called [`ObservableViewModel`](../databinding/src/main/java/com/aidanvii/toolbox/databinding/ObservableViewModel.kt):
```kotlin
class ViewModel : ObservableViewModel() {
    
    @get:Bindable
    var expanded by bindable(false)
    ...
}
```

Both [`NotifiableObservable`](../databinding/src/main/java/com/aidanvii/toolbox/databinding/NotifiableObservable.kt) and [`ObservableViewModel`](../databinding/src/main/java/com/aidanvii/toolbox/databinding/ObservableViewModel.kt) come from the base [databinding package](../databinding/README.md), but are shipped with this package.

Both of these delegates use reflection in a inexpensive way to match the property name to a property constant ID in your app's `BR.java` class that is generated by the data binding compiler. The [`PropertyMapper`](../databinding/src/main/java/com/aidanvii/toolbox/databinding/PropertyMapper.kt) object must be able to access your app's `BR.java` class. If you are using this in a library module, simply having a raw dependency on the data binding library is not enough - that is, the data binding flag must be true in gradle to trigger the generation of the `BR.java` class.

For an implementation of [`NotifiableObservable`](../databinding/src/main/java/com/aidanvii/toolbox/databinding/NotifiableObservable.kt) that extends the architecture components [`ViewModel`](https://developer.android.com/topic/libraries/architecture/viewmodel.html), see [`ObservableArchViewModel`](../databinding-arch-viewmodel/src/main/java/com/aidanvii/toolbox/databinding/ObservableArchViewModel.kt) from the architecture components ViewModel extension package [here](../databinding-arch-viewmodel/README.md).

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
  implementation "com.github.Aidanvii7.Toolbox:delegates-observable-databinding:$toolbox_version"
}

```
