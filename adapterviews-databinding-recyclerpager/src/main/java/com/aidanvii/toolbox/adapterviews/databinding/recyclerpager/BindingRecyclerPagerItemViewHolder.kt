package com.aidanvii.toolbox.adapterviews.databinding.recyclerpager

import android.databinding.ViewDataBinding
import android.support.annotation.RestrictTo
import com.aidanvii.toolbox.adapterviews.databinding.BindableAdapter
import com.aidanvii.toolbox.adapterviews.databinding.BindableAdapterItem
import com.aidanvii.toolbox.adapterviews.recyclerpager.RecyclerPagerAdapter

class BindingRecyclerPagerItemViewHolder<out Binding : ViewDataBinding, Item : BindableAdapterItem> internal constructor(
        override val bindingResourceId: Int,
        override val viewDataBinding: Binding
) : RecyclerPagerAdapter.ViewHolder, BindableAdapter.ViewHolder<Binding, Item> {

    override val view get() = viewDataBinding.root
    override var boundAdapterItem: Item? = null

    companion object {
        @RestrictTo(RestrictTo.Scope.TESTS)
        fun <Item : BindableAdapterItem> typedTestViewHolder(
                viewDataBinding: ViewDataBinding,
                bindingResourceId: Int = 0
        ) = BindingRecyclerPagerItemViewHolder<ViewDataBinding, Item>(bindingResourceId, viewDataBinding)
    }
}