package com.aidanvii.toolbox.adapterviews.databinding

import android.databinding.ViewDataBinding
import android.support.annotation.LayoutRes
import android.support.annotation.RestrictTo
import android.view.View
import android.view.ViewGroup
import com.aidanvii.toolbox.databinding.IntBindingConsumer
import com.aidanvii.toolbox.databinding.NotifiableObservable

/**
 * Represents A data-binding adapter that can automatically bind a list of type [BindableAdapterItem].
 *
 * You shouldn't implement this directly.
 *
 * If you need to override methods, subclass [BindingRecyclerPagerAdapter] or [BindingRecyclerViewAdapter],
 *
 * See [BindingRecyclerViewBinder] or [BindingRecyclerPagerBinder] for typical usage.
 */
interface BindableAdapter<Item : BindableAdapterItem, VH : BindableAdapter.ViewHolder<*, Item>> {

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    interface ViewHolder<out Binding : ViewDataBinding, Item : BindableAdapterItem> {
        val bindingResourceId: Int
        val viewDataBinding: Binding
        val view: View get() = viewDataBinding.root
        var boundAdapterItem: Item?
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    interface ViewTypeHandler<Item : BindableAdapterItem> {
        fun initBindableAdapter(bindableAdapter: BindableAdapter<Item, *>)
        fun getItemViewType(position: Int): Int
        fun getLayoutId(viewType: Int): Int
        fun getBindingId(@LayoutRes layoutId: Int): Int
    }

    /**
     * represents the current data-set in the [BindableAdapter]
     */
    var items: List<Item>

    /**
     * Retrieves the current [Item] at the given [position] from [items]
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    fun getItem(position: Int): Item = items[position]

    /**
     * Retrieves the current position of the given [Item] from [items]
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    fun getItemPosition(item: Item): Int = items.indexOf(item)

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    fun createWith(bindingResourceId: Int, viewDataBinding: ViewDataBinding): VH

    /**
     * Called when the [BindableAdapter] has created a [ViewHolder]
     */
    fun onCreated(adapterView: ViewGroup, viewHolder: VH) {}

    /**
     * Called when the [BindableAdapter] is binding a [ViewHolder] for the given [adapterPosition]
     *
     * @return
     * true to signal to the [BindableAdapter] that you do not want the default binding to occur,
     * and that it has been taken care of in the overridden implementation of [onInterceptOnBind].
     *
     * false (default) to signal to the [BindableAdapter] to execute the default binding.
     * The default will call [ViewDataBinding.setVariable] with the
     * [BindableAdapterItem.bindingId] as the variableId and [BindableAdapterItem.bindableItem] as the value.
     */
        fun onInterceptOnBind(viewHolder: VH, adapterPosition: Int, observable: NotifiableObservable?): Boolean = false

    /**
     * Called when the [BindableAdapter] is binding a [ViewHolder] for the given [adapterPosition]
     *
     * regardless of what [onInterceptOnBind] returns, this will always be called.
     * This gives you the opportunity to bind extra data bound variables for the current [Item].
     */
    fun onBindExtras(viewHolder: VH, adapterPosition: Int) {}

    /**
     * Called when the [BindableAdapter] has finished binding a [ViewHolder] for the given [adapterPosition]
     */
    fun onBound(viewHolder: VH, adapterPosition: Int) {}

    /**
     * Called when the [BindableAdapter] is un-binding a [ViewHolder] from the given [adapterPosition]
     *
     * @return
     * true to signal to the [BindableAdapter] that you do not want the default un-binding to occur,
     * and that it has been taken care of in the overridden implementation of [onInterceptUnbind].
     *
     * false (default) to signal to the [BindableAdapter] to execute the default binding.
     * The default will call [ViewDataBinding.setVariable] with the
     * [BindableAdapterItem.bindingId] as the variableId and null as the value.
     */
    fun onInterceptUnbind(viewHolder: VH, adapterPosition: Int): Boolean = false

    /**
     * Called when the [BindableAdapter] is un-binding a [ViewHolder] from the given [adapterPosition]
     *
     * regardless of what [onInterceptOnBind] returns, this will always be called.
     * This gives you the opportunity to un-bind extra data bound variables for the current [Item].
     */
    fun onUnbindExtras(viewHolder: VH, adapterPosition: Int) {}

    /**
     * Called when the [BindableAdapter] has finished un-binding a [ViewHolder] from the given [adapterPosition]
     */
    fun onUnbound(viewHolder: VH, adapterPosition: Int) {}

    /**
     * Called when the [BindableAdapter] no longer needs the [ViewHolder] at the given [adapterPosition]
     *
     * If an [Item] is bound at the given [adapterPosition], the [viewHolder] will be unbound as well.
     */
    fun onDestroyed(viewHolder: VH, adapterPosition: Int) {}

    @get:RestrictTo(RestrictTo.Scope.LIBRARY)
    val viewTypeHandler: ViewTypeHandler<Item>

    @get:RestrictTo(RestrictTo.Scope.LIBRARY)
    val bindingInflater: BindingInflater

    @get:RestrictTo(RestrictTo.Scope.LIBRARY)
    var itemBoundListener: IntBindingConsumer?
}