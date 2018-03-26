[![CircleCI](https://circleci.com/gh/Aidanvii7/Toolbox.svg?style=svg)](https://circleci.com/gh/Aidanvii7/Toolbox)
[![](https://jitpack.io/v/Aidanvii7/Toolbox.svg)](https://jitpack.io/#Aidanvii7/Toolbox)


# Toolbox
Toolbox is a collection of libraries for Android. 

Currently it provides the following functionality:
* [Observable property delegates](#observable-property-delegates)
* [Observable property delegates databinding integration](#observable-property-delegates-databinding-integration)
* [Observable property delegates RxJava integration](#observable-property-delegates-rxjava-integration)
* [Architecture Components ViewModel integration](#architecture-components-viewmodel-integration)

# Setup

Latest Toolbox version:
```gradle
toolbox_version = 'v0.3.4-alpha'
```

 Add the JitPack repository to your build file: 

```gradle
repositories {
  ...
	maven { url 'https://jitpack.io' }    
}
```

Pick and choose the artifacts you need:

```gradle

// The base library, currently there's only common typealiases and constants here. 
// It's unlikely you would use this directly.
api "com.github.Aidanvii7.Toolbox:common:$toolbox_version"

// Contains an alternative base implementation to BaseObservable from the data binding library (NotifiableObservable), 
// that uses composition - so view-models can simply implement NotifiableObservable and use any base class (if any).
api "com.github.Aidanvii7.Toolbox:databinding:$toolbox_version"

// Adds a convenience base class that implements NotifiableObservable and also 
// extends ViewModel from Google's Architecture components library.
api "com.github.Aidanvii7.Toolbox:databinding-arch-viewmodel:$toolbox_version"

// Contains a set of property delegates, serving as an enhancement over the ObservableProperty class 
// that is shipped with the Kotlin standard library.
// These delegates can be chained together in a functional way.
api "com.github.Aidanvii7.Toolbox:delegates-observable:$toolbox_version"

// Provides an ObservableProperty implementation that integrates with the android data binding library.
api "com.github.Aidanvii7.Toolbox:delegates-observable-databinding:$toolbox_version"

```

## Observable property delegates

The delegates-observable artifact provides a set of property delegates that allow functional style syntax.

Each delegate chain must begin with a [`ObservableProperty.Source`](https://github.com/Aidanvii7/Toolbox/blob/master/delegates-observable/src/main/java/com/aidanvii/toolbox/delegates/observable/ObservableProperty.kt) delegate, for example:

```kotlin
var myObservableProperty by observable("")
```
Where `observable(..)` provides a source observable (`ObservableProperty.Source` class).

Once a source observable is created, a set of 'decorator' observables (`ObservableProperty` interface implementations) can be chained in a functional way such as:

```kotlin
var nonNullString by observable<String>("")
        .eager() // propagate initial value downstream instead of waiting on subsequent assignments to property
        .onFirstAccess { /* lazily do something the first time this property is accessed/read */ }
        .filter { it.isNotEmpty() } // ignore empty strings
        .doOnNext { /* do something with the initial value */ }
        .skip(1) // ignore initial value
        .distinctUntilChanged() // ignore subsequent values that are the same as the previous value
        .doOnNext { /* do something with subsequent values */ }
        .map { it.length }
        .doOnNext { stringLength -> /* do something with length */ }
```

It also supports nullable types:
```kotlin
var nullableString by observable<String?>(null)
        .eager() // propagate initial value downstream instead of waiting on subsequent assignments to property
        .onFirstAccess { /* lazily do something the first time this property is accessed/read */ }
        .filterNotNullWith { it.isNotEmpty() } // ignore empty strings
        .doOnNext { /* do something with the initial value */ }
        .skip(1) // ignore initial value
        .distinctUntilChanged() // ignore subsequent values that are the same as the previous value
        .doOnNext { /* do something with subsequent values */ }
        .map { it.length }
        .doOnNext { stringLength -> /* do something with length */ }
```

Here is a list of the current decorators:
* [`EagerDecorator`](https://github.com/Aidanvii7/Toolbox/blob/master/delegates-observable/src/main/java/com/aidanvii/toolbox/delegates/observable/EagerDecorator.kt)
* [`DistinctUntilChangedDecorator`](https://github.com/Aidanvii7/Toolbox/blob/master/delegates-observable/src/main/java/com/aidanvii/toolbox/delegates/observable/DistinctUntilChangedDecorator.kt)
* [`FilterDecorator`](https://github.com/Aidanvii7/Toolbox/blob/master/delegates-observable/src/main/java/com/aidanvii/toolbox/delegates/observable/FilterDecorator.kt)
* [`FilterNotNullDecorator`](https://github.com/Aidanvii7/Toolbox/blob/master/delegates-observable/src/main/java/com/aidanvii/toolbox/delegates/observable/FilterNotNullDecorator.kt)
* [`MapDecorator`](https://github.com/Aidanvii7/Toolbox/blob/master/delegates-observable/src/main/java/com/aidanvii/toolbox/delegates/observable/MapDecorator.kt)
* [`SkipDecorator`](https://github.com/Aidanvii7/Toolbox/blob/master/delegates-observable/src/main/java/com/aidanvii/toolbox/delegates/observable/SkipDecorator.kt)
* [`DoOnNextDecorator`](https://github.com/Aidanvii7/Toolbox/blob/master/delegates-observable/src/main/java/com/aidanvii/toolbox/delegates/observable/DoOnNextDecorator.kt)
* [`OnFirstAccessDecorator`](https://github.com/Aidanvii7/Toolbox/blob/master/delegates-observable/src/main/java/com/aidanvii/toolbox/delegates/observable/OnFirstAccessDecorator.kt)
## Observable property delegates databinding integration
The delegates-observable-databinding artifact provides a source observable implementation that integrates with data-binding.

Two types of delegates exist, `bindable(..)` and `bindableEvent(..)`.
* `bindable(..)` will only propagate values downstream and notify data binding if the value given is different from the previous value.
* `bindableEvent(..)` will propagate values downstream and notify data binding even if the given value is the same as the previous value.

Please see the [documentation](https://github.com/Aidanvii7/Toolbox/blob/master/delegates-observable-databinding/src/main/java/com/aidanvii/toolbox/databinding/BindableProperty.kt) of each for a deeper explanation and usage suggestions.

Both of these delegates can only be used as member properties of [`NotifiableObservable`](https://github.com/Aidanvii7/Toolbox/blob/master/databinding/src/main/java/com/aidanvii/toolbox/databinding/NotifiableObservable.kt) implementations, such as:

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

A base class exists for [`NotifiableObservable`](https://github.com/Aidanvii7/Toolbox/blob/master/databinding/src/main/java/com/aidanvii/toolbox/databinding/NotifiableObservable.kt) which handles some of the boilerplate, called [`ObservableViewModel`](https://github.com/Aidanvii7/Toolbox/blob/master/databinding/src/main/java/com/aidanvii/toolbox/databinding/ObservableViewModel.kt):
```kotlin
class ViewModel : ObservableViewModel() {
    
    @get:Bindable
    var expanded by bindable(false)
    ...
}
```

Both of these delegates use reflection in a inexpensive way to match the property name to a property constant ID in your app's `BR.java` class that is generated by the data binding compiler. The [`PropertyMapper`](https://github.com/Aidanvii7/Toolbox/blob/master/databinding/src/main/java/com/aidanvii/toolbox/databinding/PropertyMapper.kt) object must know about your app's `BR.java` class, and should be initialised once during the lifetime of the app.

Consider doing this in a custom `Application` class such as:
```kotlin

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        PropertyMapper.initBRClass(
                brClass = BR::class.java,
                locked = true // prevents multiple invocations at runtime
        )
    }
}

```

## Observable property delegates RxJava integration
The delegates-observable-rxjava artifact provides an observable decorator with the extension method`toRx()` that transforms a [`ObservableProperty`](https://github.com/Aidanvii7/Toolbox/blob/master/delegates-observable/src/main/java/com/aidanvii/toolbox/delegates/observable/ObservableProperty.kt) to a [`RxObservableProperty`](https://github.com/Aidanvii7/Toolbox/blob/master/delegates-observable-rxjava/src/main/java/com/aidanvii/toolbox/delegates/observable/rxjava/RxObservableProperty.kt). Decorators for [`ObservableProperty`](https://github.com/Aidanvii7/Toolbox/blob/master/delegates-observable/src/main/java/com/aidanvii/toolbox/delegates/observable/ObservableProperty.kt) are not compatible with [`RxObservableProperty`](https://github.com/Aidanvii7/Toolbox/blob/master/delegates-observable-rxjava/src/main/java/com/aidanvii/toolbox/delegates/observable/rxjava/RxObservableProperty.kt), however similar implementations exist and use the same syntax such as:

```kotlin
var nonNullString by observable<String>("")
        .eager() // propagate initial value downstream instead of waiting on subsequent assignments to property
        .onFirstAccess { /* lazily do something the first time this property is accessed/read */ }
	.toRx() // transforms ObservableProperty to RxObservableProperty
	// the following decorator methods look the same, but return RxObservableProperty instead.
        .filter { it.isNotEmpty() } // ignore empty strings
        .doOnNext { /* do something with the initial value */ }
        .skip(1) // ignore initial value
        .distinctUntilChanged() // ignore subsequent values that are the same as the previous value
        .doOnNext { /* do something with subsequent values */ }
	.observeOn(Schedulers.computation()) // switch threads!
        .map { it.length }
        .doOnNext { stringLength -> /* do something with length */ }
```

Here is a list of the current decorators:
* [`RxObserveOnDecorator`](https://github.com/Aidanvii7/Toolbox/blob/master/delegates-observable-rxjava/src/main/java/com/aidanvii/toolbox/delegates/observable/rxjava/RxObserveOnDecorator.kt)
* [`RxDistinctUntilChangedDecorator`](https://github.com/Aidanvii7/Toolbox/blob/master/delegates-observable-rxjava/src/main/java/com/aidanvii/toolbox/delegates/observable/rxjava/RxDistinctUntilChangedDecorator.kt)
* [`RxFilterDecorator`](https://github.com/Aidanvii7/Toolbox/blob/master/delegates-observable-rxjava/src/main/java/com/aidanvii/toolbox/delegates/observable/rxjava/RxFilterDecorator.kt)
* [`RxFilterNotNullDecorator`](https://github.com/Aidanvii7/Toolbox/blob/master/delegates-observable-rxjava/src/main/java/com/aidanvii/toolbox/delegates/observable/rxjava/RxFilterNotNullDecorator.kt)
* [`RxMapDecorator`](https://github.com/Aidanvii7/Toolbox/blob/master/delegates-observable-rxjava/src/main/java/com/aidanvii/toolbox/delegates/observable/rxjava/RxMapDecorator.kt)
* [`RxSkipDecorator`](https://github.com/Aidanvii7/Toolbox/blob/master/delegates-observable-rxjava/src/main/java/com/aidanvii/toolbox/delegates/observable/rxjava/RxSkipDecorator.kt)
* [`RxDoOnNextDecorator`](https://github.com/Aidanvii7/Toolbox/blob/master/delegates-observable-rxjava/src/main/java/com/aidanvii/toolbox/delegates/observable/rxjava/RxDoOnNextDecorator.kt)

The main advantage of these extensions is the `observeOn(scheduler)` method, allowing thread switching in the stream.

# Architecture Components ViewModel integration
The databinding-arch-viewmodel artifact simply provides a base class implementation similar to [`ObservableViewModel`](https://github.com/Aidanvii7/Toolbox/blob/master/databinding/src/main/java/com/aidanvii/toolbox/databinding/ObservableViewModel.kt) which extends the [`ViewModel`](https://developer.android.com/topic/libraries/architecture/viewmodel.html) class from the architecture components library, called [`ObservableArchViewModel`](https://github.com/Aidanvii7/Toolbox/blob/master/databinding-arch-viewmodel/src/main/java/com/aidanvii/toolbox/databinding/ObservableArchViewModel.kt).

# Alpha status
This library is currently in alpha, meaning that the API may change from version to version, and also that I am looking for feedback.
