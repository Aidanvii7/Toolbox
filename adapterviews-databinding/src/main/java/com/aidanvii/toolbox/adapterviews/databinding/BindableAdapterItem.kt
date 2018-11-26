package com.aidanvii.toolbox.adapterviews.databinding

import android.databinding.ViewDataBinding
import android.support.annotation.LayoutRes
import android.view.ViewGroup
import com.aidanvii.toolbox.adapterviews.databinding.BindableAdapter.ViewHolder

/**
 * Represents an item that can exist within a [BindingRecyclerViewAdapter] or [BindingRecyclerPagerAdapter].
 *
 * Provides basic information to these adapters to allow automatic layout inflation and binding.
 */
interface BindableAdapterItem {

    /**
     * The id of the data-binding enabled layout resource to inflate
     */
    @get:LayoutRes
    val layoutId: Int

    /**
     * The BR id associated with the data-bound variable [bindableItem] that is injected into the [ViewDataBinding]
     * via [ViewDataBinding.setVariable]
     */
    val bindingId: Int get() = BR.viewModel

    /**
     * The data-bound variable that is associated with the BR id [bindingId] that is injected into the [ViewDataBinding]
     * via [ViewDataBinding.setVariable]
     */
    val lazyBindableItem: Lazy<Any?> get() = lazy(LazyThreadSafetyMode.NONE) { this@BindableAdapterItem }

    /**
     * Optional property that is used by [defaultAreItemsSame] function when calculating the [DiffResult] in [BindingRecyclerViewAdapter]
     */
    val isEmpty: Boolean get() = false

    /**
     * Optional property that provides a page title to [BindingRecyclerPagerAdapter.getItemViewType]
     * This takes precedence over [itemTitle]
     */
    val itemTitleRes: Int get() = 0

    /**
     * Optional property that provides a page title to [BindingRecyclerPagerAdapter.getItemViewType]
     * This is a fallback if [itemTitleRes] is 0
     */
    val itemTitle: CharSequence get() = ""

    /**
     * Called when the [BindableAdapter] is binding a [ViewHolder] for the given [adapterPosition]
     *
     * Regardless of what [onInterceptOnBind] returns, this will always be called.
     * This gives you the opportunity to bind extra data bound variables for this [BindableAdapterItem].
     */
    fun onBindExtras(viewDataBinding: ViewDataBinding, adapterPosition: Int, adapterView: ViewGroup?) {}

    /**
     * Called when the [BindableAdapter] has finished binding a [ViewHolder] for the given [adapterPosition]
     */
    fun onBound(adapterPosition: Int) {}

    /**
     * Called when the [BindableAdapter] is un-binding a [ViewHolder] from the given [adapterPosition]
     *
     * Regardless of what [onInterceptOnBind] returns, this will always be called.
     * This gives you the opportunity to un-bind extra data bound variables for this [BindableAdapterItem].
     */
    fun onUnbindExtras(viewDataBinding: ViewDataBinding, adapterPosition: Int, adapterView: ViewGroup?) {}

    /**
     * Called when the [BindableAdapter] has finished un-binding a [ViewHolder] from the given [adapterPosition]
     */
    fun onUnBound(adapterPosition: Int) {}
}