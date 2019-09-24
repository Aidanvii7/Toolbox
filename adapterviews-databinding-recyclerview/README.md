[![CircleCI](https://circleci.com/gh/Aidanvii7/Toolbox.svg?style=svg)](https://circleci.com/gh/Aidanvii7/Toolbox)
[![](https://jitpack.io/v/Aidanvii7/Toolbox.svg)](https://jitpack.io/#Aidanvii7/Toolbox)

# RecyclerView bindings
Provides a set of abstractions that can be used to easily keep a list-based viewmodel in sync with a [`RecyclerView`](https://developer.android.com/reference/android/support/v7/widget/RecyclerView) without explicit adapter implementations.

# Use Case
Let's say you have some data that needs to be represented in a list. 
You may do this by having a view model that hosts this list of data to be bound to a [`RecyclerView`](https://developer.android.com/reference/android/support/v7/widget/RecyclerView).
This data may be represented to each of the views in a [`RecyclerView`](https://developer.android.com/reference/android/support/v7/widget/RecyclerView) in the form of 'mini' view models of some sort.
The list-based view model that hosts these 'mini' view models should hold a collection that the [`RecyclerView`](https://developer.android.com/reference/android/support/v7/widget/RecyclerView) can consume.

# Example Usage
In this library, these 'mini' view models are bound to the [`RecyclerView`](https://developer.android.com/reference/android/support/v7/widget/RecyclerView) via binding adapters.
The list-based view model should host a list of [`BindableAdapterItem`](adapterviews-databinding/src/main/java/com/aidanvii/toolbox/adapterviews/databinding/BindableAdapterItem.kt) objects.
[`BindableAdapterItem`](adapterviews-databinding/src/main/java/com/aidanvii/toolbox/adapterviews/databinding/BindableAdapterItem.kt) provides information required by the internal adapter, such as:
- The instance of the 'mini' view model itself, which can be lazily created the first time it's bound to the adapter
- The layout ID that the 'mini' view model is bound to
- The BR ID of the data bound view model (defaults to `BR.viewModel`)
- The item title when bound to a [`ViewPager`](https://developer.android.com/reference/android/support/v4/view/ViewPager) instead, for tab names on a bound [`TabLayout`](https://developer.android.com/reference/android/support/design/widget/TabLayout)

Generally speaking, a [`BindableAdapterItem`](adapterviews-databinding/src/main/java/com/aidanvii/toolbox/adapterviews/databinding/BindableAdapterItem.kt) should provide a seperate view model such as:

```kotlin
class MyViewModel(
   val id: Int,
   val content: String
) {
  // some bound properties here
}

// ...

class MyAdapterItem(
   val id: Int,
   val content: String
) : BindableAdapterItem {
     override val layoutId: Int get() = R.layout.my_item
     override val bindingId: Int get() = BR.viewModel // <- default, no need to override
     override val lazyBindableItem = lazy(LazyThreadSafetyMode.NONE) { MyViewModel(id, content) }
}
```

It may seem overkill to seperate `MyAdapterItem` and `MyViewModel` into 2 classes at this point, but can be useful once complexity grows and where lazy creation may be beneficial.
In the case where you don't need this granularity, simply don't override `lazyBindableItem`, this will by default bind the [`BindableAdapterItem`](adapterviews-databinding/src/main/java/com/aidanvii/toolbox/adapterviews/databinding/BindableAdapterItem.kt) to the provided layout itself.
So in the simplest case, without explicitely overriding `bindingId` and `lazyBindableItem`, you can simply do:

```kotlin
class MyAdapterItemViewModel(
   val id: Int,
   val content: String
) : BindableAdapterItem {
     override val layoutId: Int get() = R.layout.my_item
}
```

An example of our items `my_item.xml` could simply be:
```xml
<?xml version="1.0" encoding="utf-8"?>
<layout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto">
    
    <data>
        <variable
            name="viewModel"
            type="com.example.MyAdapterItemViewModel" />
    </data>
    
    <LinearLayout
        android:layout_width="wrap_content"
        android:layout_height="match_parent">
        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="@{viewModel.id}"/>
        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="@{viewModel.content}"/>
    </LinearLayout>
</layout>
```
Notice the name of our data bound variable is "viewModel". If you want to name this something else you must override `bindingId` in your [`BindableAdapterItem`](../adapterviews-databinding/src/main/java/com/aidanvii/toolbox/adapterviews/databinding/BindableAdapterItem.kt) class.

In this example, our list-based view model that provides these will take advantage of the observable property delegates for Android Databinding package [here](delegates-observable-databinding/README.md).
It will also extend [`ObservableArchViewModel`](databinding-arch-viewmodel/src/main/java/com/aidanvii/toolbox/databinding/ObservableArchViewModel.kt) from the architecture components ViewModel extension package [here](../databinding-arch-viewmodel/README.md).

```kotlin
class MyListViewModel : ObservableArchViewModel() {
    
    @get:Bindable
    var items by bindable(emptyList<MyAdapterItemViewModel>())
      private set
      
    val binder = BindingRecyclerViewBinder<MyAdapterItemViewModel>(
        hasMultipleViewTypes = false
    )
    
    // items populated somewhere here..
}
```

What is [`BindingRecyclerViewBinder`](adapterviews-databinding-recyclerview/src/main/java/com/aidanvii/toolbox/adapterviews/recyclerview/BindingRecyclerViewBinder.kt)?
It simply provides configuration used by the binding adapter to do various things such as:
- Provide a custom [`LayoutManager`](https://developer.android.com/reference/android/support/v7/widget/RecyclerView.LayoutManager) via the `layoutManagerFactory`
- Provide a custom [`BindingRecyclerViewAdapter`](adapterviews-databinding-recyclerview/src/main/java/com/aidanvii/toolbox/adapterviews/recyclerview/BindingRecyclerViewAdapter.kt) via the `layoutManagerFactory`
- Disabling multiple view types by setting `hasMultipleViewTypes = false` (small performance gain for lists of a single view type)
- Customize the internal diffing logic used by [`DiffUtil`](https://developer.android.com/reference/android/support/v7/util/DiffUtil)
  - Both `areItemsTheSame` and `areContentsTheSame` have default implementations ([see here](adapterviews-databinding/src/main/java/com/aidanvii/toolbox/adapterviews/databinding/Utils.kt))
- [experimental] Enable adapter notifications (allows 'mini' view models to notify the adapter for a rebind)

In the above example, we only use `hasMultipleViewTypes = false` since we know there is only 1 type of item.

To bind `MyListViewModel` to a [`RecyclerView`](https://developer.android.com/reference/android/support/v7/widget/RecyclerView), 
simply bind both the `binder` and `items`to the [`RecyclerView`](https://developer.android.com/reference/android/support/v7/widget/RecyclerView) as follows:

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

    <androidx.recyclerview.widget.RecyclerView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:binder="@{viewModel.binder}"
        app:items="@{viewModel.items}" />
</layout>
```

And that's it!

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
  implementation "com.github.Aidanvii7.Toolbox:adapterviews-databinding-recyclerview:$toolbox_version"
}

```
