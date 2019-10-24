[![CircleCI](https://circleci.com/gh/Aidanvii7/Toolbox.svg?style=svg)](https://circleci.com/gh/Aidanvii7/Toolbox)
[![](https://jitpack.io/v/Aidanvii7/Toolbox.svg)](https://jitpack.io/#Aidanvii7/Toolbox)

# Observable property delegates
Provides a set of property delegates that allow functional style syntax.

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
        .map { stringLength -> stringLength > 0 }
        .doOnTrue { /* do something when stringLenth is above 0 */ }
        .doOnFalse { /* do something when stringLenth is 0 */ }
```

Here is a list of the current decorators:
* [`EagerDecorator`](delegates-observable/src/main/java/com/aidanvii/toolbox/delegates/observable/EagerDecorator.kt)
* [`OnFirstAccessDecorator`](delegates-observable/src/main/java/com/aidanvii/toolbox/delegates/observable/OnFirstAccessDecorator.kt)
* [`FilterNotNullDecorator`](delegates-observable/src/main/java/com/aidanvii/toolbox/delegates/observable/FilterNotNullDecorator.kt)
* [`DoOnNextDecorator`](delegates-observable/src/main/java/com/aidanvii/toolbox/delegates/observable/DoOnNextDecorator.kt)
* [`DoOnTrueDecorator`](delegates-observable/src/main/java/com/aidanvii/toolbox/delegates/observable/DoOnTrueDecorator.kt)
* [`DoOnFalseDecorator`](delegates-observable/src/main/java/com/aidanvii/toolbox/delegates/observable/DoOnFalseDecorator.kt)
* [`SkipDecorator`](delegates-observable/src/main/java/com/aidanvii/toolbox/delegates/observable/SkipDecorator.kt)
* [`DistinctUntilChangedDecorator`](delegates-observable/src/main/java/com/aidanvii/toolbox/delegates/observable/DistinctUntilChangedDecorator.kt)
* [`FilterDecorator`](delegates-observable/src/main/java/com/aidanvii/toolbox/delegates/observable/FilterDecorator.kt)
* [`MapDecorator`](delegates-observable/src/main/java/com/aidanvii/toolbox/delegates/observable/MapDecorator.kt)
* [`SkipDecorator`](delegates-observable/src/main/java/com/aidanvii/toolbox/delegates/observable/SkipDecorator.kt)

# Android Databinding Integration
Integration with Android's databinding library is handled by a seperate package, see [here](../delegates-observable-databinding/README.md).

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
..
dependencies {
  ..
  implementation "com.github.Aidanvii7.Toolbox:delegates-observable:$toolbox_version"
}

```
