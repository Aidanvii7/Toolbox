package com.aidanvii.toolbox.adapterviews.recyclerview

import android.os.Parcelable
import android.support.annotation.RestrictTo
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import com.aidanvii.toolbox.adapterviews.databinding.BindableAdapter
import com.aidanvii.toolbox.adapterviews.databinding.BindableAdapterDelegate
import com.aidanvii.toolbox.adapterviews.databinding.BindableAdapterItem
import com.aidanvii.toolbox.adapterviews.databinding.BindingInflater
import com.aidanvii.toolbox.adapterviews.databinding.ListBinder
import com.aidanvii.toolbox.adapterviews.databinding.defaultAreContentsSame
import com.aidanvii.toolbox.adapterviews.databinding.defaultAreItemsSame
import com.aidanvii.toolbox.delegates.weak.weakLazy

/**
 * Intermediate class used to configure the [BindingRecyclerViewAdapter]
 *
 * Example use case:
 *
 * Suppose you have a custom [BindingRecyclerViewAdapter]:
 * ```
 * class MyCustomBindableAdapter(
 *          builder: Builder<MyAdapterItemViewModel>
 * ) : BindingRecyclerViewAdapter<MyAdapterItemViewModel>(builder) {
 *     //... custom optional overrides here ...
 * }
 * ```
 * Where the [BindableAdapterItem] implementation looks like:
 * ```
 * class MyAdapterItemViewModel(
 *          val id: Int,
 *          val content: String
 * ) : ObservableItemViewModel(), BindableAdapterItem {
 *     override val layoutId: Int get() = R.id.my_item
 *     override val bindingId: Int get() = BR.viewModel
 *     //... custom adapterBindable properties etc ...
 * }
 *
 * ```
 * Where [MyAdapterItemViewModel] has both [id] and [content] fields, and extends [ObservableItemViewModel]/implements [AdapterNotifier],
 * the [BindingRecyclerViewBinder] would be declared as follows:
 * ```
 * class MyListViewModel : ObservableViewModel() {
 *
 *     @get:Bindable
 *     var items by bindable(emptyList<MyAdapterItemViewModel>())
 *
 *     val binder = BindingRecyclerViewBinder<MyAdapterItemViewModel>(
 *              hasMultipleViewTypes = false,
 *              areItemsTheSame = { oldItem, newItem -> oldItem.id == newItem.id },
 *              areContentsTheSame = { oldItem, newItem -> oldItem.content == oldItem.content },
 *              adapterNotificationEnabled = true,
 *              adapterFactory = { MyCustomBindableAdapter(it) }
 *      )
 * }
 * ```
 * Where [MyListViewModel] is a data-bound variable in xml that provides the [BindingRecyclerViewBinder]:
 * ```
 * <variable
 *   name="listViewModel"
 *   type="com.example.MyListViewModel"/>
 * ```
 * And bound to the [RecyclerView]:
 * ```
 * <android.support.v7.widget.RecyclerView
 *   android:layout_width="match_parent"
 *   android:layout_height="wrap_content"
 *   android:items="@{listViewModel.items}"
 *   android:binder="@{viewModel.binder}"/>
 * ```
 * @param hasMultipleViewTypes if the [BindingRecyclerViewAdapter] will have [BindableAdapterItem] with different [BindableAdapterItem.layoutId]s, set to true. False otherwise (minor optimisation)
 * @param adapterNotificationEnabled if any [BindableAdapterItem.bindableItem] implements [AdapterNotifier], set to true
 * @param areItemsTheSame same logic as [DiffUtil.Callback.areItemsTheSame]
 * @param areContentsTheSame same logic as [DiffUtil.Callback.areContentsTheSame]
 * @param adapterFactory optional factory to provide a custom implementation of [BindingRecyclerViewAdapter], allowing you to override methods from [BindableAdapter]
 */
class BindingRecyclerViewBinder<Item : BindableAdapterItem>(
        hasMultipleViewTypes: Boolean = true,
        val adapterNotificationEnabled: Boolean = false,
        areItemsTheSame: ((oldItem: Item, newItem: Item) -> Boolean) = defaultAreItemsSame,
        areContentsTheSame: ((oldItem: Item, newItem: Item) -> Boolean) = defaultAreContentsSame,
        val adapterFactory: (BindingRecyclerViewAdapter.Builder<Item>) -> BindingRecyclerViewAdapter<Item> = { BindingRecyclerViewAdapter(it) }
) : ListBinder<Item>(
        hasMultipleViewTypes = hasMultipleViewTypes,
        areItemsTheSame = areItemsTheSame,
        areContentsTheSame = areContentsTheSame
) {

    internal var layoutManagerState: Parcelable? = null

    internal val adapter: BindingRecyclerViewAdapter<Item> by weakLazy {
        adapterFactory(
                BindingRecyclerViewAdapter.Builder(
                        delegate = BindableAdapterDelegate(),
                        areItemsTheSame = areItemsTheSame,
                        areContentsTheSame = areContentsTheSame,
                        viewTypeHandler = viewTypeHandler,
                        bindingInflater = BindingInflater
                )
        ).apply {
            if (adapterNotificationEnabled) {
                registerAdapterDataObserver(AdapterNotifierDataObserver(this))
            }
        }
    }

    @RestrictTo(RestrictTo.Scope.TESTS)
    fun testAdapter(
            viewTypeHandler: BindableAdapter.ViewTypeHandler<Item> = this.viewTypeHandler,
            bindingInflater: BindingInflater = BindingInflater,
            areItemsTheSame: ((oldItem: Item, newItem: Item) -> Boolean) = this.areItemsTheSame,
            areContentsTheSame: ((oldItem: Item, newItem: Item) -> Boolean) = this.areContentsTheSame
    ) = BindingRecyclerViewAdapter.Builder(
            delegate = BindableAdapterDelegate(),
            viewTypeHandler = viewTypeHandler,
            bindingInflater = bindingInflater,
            areItemsTheSame = areItemsTheSame,
            areContentsTheSame = areContentsTheSame
    ).let { builder ->
        adapterFactory(builder)
    }
}