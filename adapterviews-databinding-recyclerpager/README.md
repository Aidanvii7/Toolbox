[![CircleCI](https://circleci.com/gh/Aidanvii7/Toolbox.svg?style=svg)](https://circleci.com/gh/Aidanvii7/Toolbox)
[![](https://jitpack.io/v/Aidanvii7/Toolbox.svg)](https://jitpack.io/#Aidanvii7/Toolbox)

# ViewPager bindings
Provides a set of abstractions that can be used to easily keep a page-based viewmodel in sync with a [`ViewPager`](https://developer.android.com/reference/android/support/v7/widget/RecyclerView) without explicit adapter implementations.

# Use Case
Let's say you have some data that needs to be represented in a set of adjacent scrollable pages (aka inside a [`ViewPager`](https://developer.android.com/reference/android/support/v4/view/ViewPager)). 
You may do this by having a view model that hosts this list of data to be bound to a [`ViewPager`](https://developer.android.com/reference/android/support/v4/view/ViewPager).
This data may be represented to each of the views in a [`ViewPager`](https://developer.android.com/reference/android/support/v4/view/ViewPager) in the form of a view model for each page.
The page-based view model that hosts these page view models should hold a collection that the [`ViewPager`](https://developer.android.com/reference/android/support/v4/view/ViewPager) can consume.

# Example Usage
This uses most of the same abstractions as the [RecyclerView bindings library](adapterviews-databinding-recyclerview/README.md). 
The only difference between that is the replacement of [`BindingRecyclerViewBinder`](adapterviews-databinding-recyclerview/src/main/java/com/aidanvii/toolbox/adapterviews/recyclerview/BindingRecyclerViewBinder.kt) 
with [`BindingRecyclerPagerBinder`](adapterviews-databinding-recyclerpager/src/main/java/com/aidanvii/toolbox/adapterviews/databinding/recyclerpager/BindingRecyclerPagerBinder.kt).

It's worth noting that the underlying [`PagerAdapter`](https://developer.android.com/reference/android/support/v4/view/PagerAdapter) implementation is not one of the standard implementations
such as [`FragmentPagerAdapter`](https://developer.android.com/reference/android/support/v4/app/FragmentPagerAdapter.html) or [`FragmentStatePagerAdapter`](https://developer.android.com/reference/android/support/v4/app/FragmentStatePagerAdapter.html).
Instead it uses a custom implementation that deals only with views. It's behaviour is similar in ways to [`RecyclerView`](https://developer.android.com/reference/android/support/v7/widget/RecyclerView), 
in that it recycles views as you move between adjacent pages. For a full understanding of the underlying adapter implementation, see:
- [`RecyclerPagerAdapter`](adapterviews-recyclerpager/src/main/java/com/aidanvii/toolbox/adapterviews/recyclerpager/RecyclerPagerAdapter.kt)
  - The base implementation that offers an API similar to [`RecyclerView`](https://developer.android.com/reference/android/support/v7/widget/RecyclerView)
- [`BindingRecyclerPagerAdapter`](adapterviews-databinding-recyclerpager/src/main/java/com/aidanvii/toolbox/adapterviews/databinding/recyclerpager/BindingRecyclerPagerAdapter.kt)
  - An implementation of [`RecyclerPagerAdapter`](adapterviews-recyclerpager/src/main/java/com/aidanvii/toolbox/adapterviews/recyclerpager/RecyclerPagerAdapter.kt) that integrates with databinding

Based on the example given on the [RecyclerView bindings library](adapterviews-databinding-recyclerview/README.md), the only changes required are to swap the binder implementation in `MyListViewModel` as follows:

```kotlin
class MyListViewModel : ObservableArchViewModel() {
    
    @get:Bindable
    var items by bindable(emptyList<MyAdapterItemViewModel>())
      private set
      
    // Replaced `BindingRecyclerViewBinder` with `BindingRecyclerPagerBinder`
    val binder = BindingRecyclerPagerBinder<MyAdapterItemViewModel>(
        hasMultipleViewTypes = false
    )
    
    // pages populated somewhere here..
}
```

Then in your xml, the following:
```xml
<?xml version="1.0" encoding="utf-8"?>
<layout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools">
    
    <data>
        <variable
            name="viewModel"
            type="com.example.MyListViewModel" />
    </data>

    <androidx.viewpager.widget.ViewPager
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:binder="@{viewModel.binder}"
        app:items="@{viewModel.items}" />
</layout>
```

As you can see, it uses the same binding adapter names as the [RecyclerView bindings library](adapterviews-databinding-recyclerview/README.md) such as `binder` and `items`.

And that's it!

# ViewPager2
This library does not integrate with the new [`ViewPager2`](https://developer.android.com/jetpack/androidx/releases/viewpager2) library that's based on [`RecyclerView`](https://developer.android.com/reference/android/support/v7/widget/RecyclerView), expect this to come soon as a seperate library!

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
  implementation "com.github.Aidanvii7.Toolbox:adapterviews-databinding-recyclerpager:$toolbox_version"
}

```
