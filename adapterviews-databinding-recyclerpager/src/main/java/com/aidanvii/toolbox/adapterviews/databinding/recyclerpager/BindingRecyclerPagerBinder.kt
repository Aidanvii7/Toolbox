package com.aidanvii.toolbox.adapterviews.databinding.recyclerpager

import android.os.Parcelable
import android.support.annotation.RestrictTo
import android.support.v4.view.ViewPager
import com.aidanvii.toolbox.adapterviews.databinding.BindableAdapter
import com.aidanvii.toolbox.adapterviews.databinding.BindableAdapterDelegate
import com.aidanvii.toolbox.adapterviews.databinding.BindableAdapterItem
import com.aidanvii.toolbox.adapterviews.databinding.BindingInflater
import com.aidanvii.toolbox.adapterviews.databinding.ListBinder
import com.aidanvii.toolbox.adapterviews.databinding.defaultAreContentsSame
import com.aidanvii.toolbox.delegates.weak.weakLazy

/**
 * Intermediate class used to configure the [BindingRecyclerPagerAdapter]
 *
 * Example use case:
 *
 * Suppose you have a custom [BindingRecyclerPagerAdapter]:
 * ```
 * class MyCustomBindableAdapter(
 *          builder: Builder<MyAdapterItemViewModel>
 * ) : BindingRecyclerPagerAdapter<MyAdapterItemViewModel>(builder) {
 *     //... custom optional overrides here ...
 * }
 * ```
 * Where the [BindableAdapterItem] implementation looks like:
 * ```
 * class MyAdapterItemViewModel(
 *          val id: Int,
 *          val content: String
 * ) : ObservableViewModel(), BindableAdapterItem {
 *     override val layoutId: Int get() = R.id.my_item
 *     override val bindingId: Int get() = BR.viewModel
 *     //... custom adapterBindable properties etc ...
 * }
 *
 * ```
 * Where [MyAdapterItemViewModel] has both [id] and [content] fields, the [BindingRecyclerPagerBinder] would be declared as follows:
 * ```
 * class MyListViewModel : ObservableViewModel() {
 *
 *     @get:Bindable
 *     var items by bindable(emptyList<MyAdapterItemViewModel>())
 *
 *     val binder = BindingRecyclerPagerBinder<MyAdapterItemViewModel>(
 *              hasMultipleViewTypes = false,
 *              areItemsAndContentsTheSame = { oldItem, newItem -> oldItem.id == newItem.id && oldItem.content == oldItem.content },
 *              adapterFactory = { MyCustomBindableAdapter(it) }
 *      )
 * }
 * ```
 * Where [MyListViewModel] is a data-bound variable in xml that provides the [BindingRecyclerPagerBinder]:
 * ```
 * <variable
 *   name="listViewModel"
 *   type="com.example.MyListViewModel"/>
 * ```
 * And bound to the [ViewPager]:
 * ```
 * <android.support.v4.view.ViewPager
 *   android:layout_width="match_parent"
 *   android:layout_height="wrap_content"
 *   android:items="@{listViewModel.items}"
 *   android:binder="@{viewModel.binder}"/>
 * ```
 * @param hasMultipleViewTypes if the [BindingRecyclerViewAdapter] will have [BindableAdapterItem] with different [BindableAdapterItem.layoutId]s, set to true. False otherwise (minor optimisation)
 * @param areItemAndContentsTheSame equivalent of [DiffUtil.Callback.areItemsTheSame] plus [DiffUtil.Callback.areContentsTheSame]. Internally forwards to [RecyclerPagerAdapter.OnDataSetChangedCallback.areItemsTheSame]
 * @param adapterFactory optional factory to provide a custom implementation of [BindingRecyclerPagerAdapter], allowing you to override methods from [BindableAdapter]
 */
class BindingRecyclerPagerBinder<Item : BindableAdapterItem>(
        hasMultipleViewTypes: Boolean = true,
        val areItemAndContentsTheSame: ((oldItem: Item, newItem: Item) -> Boolean) = defaultAreContentsSame,
        val adapterFactory: (BindingRecyclerPagerAdapter.Builder<Item>) -> BindingRecyclerPagerAdapter<Item> = { BindingRecyclerPagerAdapter(it) }
) : ListBinder<Item>(
        hasMultipleViewTypes = hasMultipleViewTypes,
        areItemsTheSame = areItemAndContentsTheSame,
        areContentsTheSame = areItemAndContentsTheSame
) {

    internal var viewPagerState: Parcelable? = null

    internal val adapter by weakLazy {
        adapterFactory(
                BindingRecyclerPagerAdapter.Builder(
                        delegate = BindableAdapterDelegate(),
                        viewTypeHandler = viewTypeHandler,
                        bindingInflater = BindingInflater,
                        areItemAndContentsTheSame = areItemAndContentsTheSame
                )
        )
    }

    @RestrictTo(RestrictTo.Scope.TESTS)
    fun testAdapter(
            viewTypeHandler: BindableAdapter.ViewTypeHandler<Item> = this.viewTypeHandler,
            bindingInflater: BindingInflater = BindingInflater,
            areItemAndContentsTheSame: ((old: Item, new: Item) -> Boolean) = this.areItemAndContentsTheSame
    ) = BindingRecyclerPagerAdapter.Builder(
            delegate = BindableAdapterDelegate(),
            viewTypeHandler = viewTypeHandler,
            bindingInflater = bindingInflater,
            areItemAndContentsTheSame = areItemAndContentsTheSame
    ).let { builder ->
        adapterFactory(builder)
    }
}