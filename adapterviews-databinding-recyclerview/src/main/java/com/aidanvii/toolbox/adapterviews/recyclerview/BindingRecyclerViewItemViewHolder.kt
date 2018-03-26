package com.aidanvii.toolbox.adapterviews.recyclerview

import android.databinding.ViewDataBinding
import android.support.annotation.RestrictTo
import android.support.v7.widget.RecyclerView
import com.aidanvii.toolbox.adapterviews.databinding.BindableAdapter
import com.aidanvii.toolbox.adapterviews.databinding.BindableAdapterItem

class BindingRecyclerViewItemViewHolder<out Binding : ViewDataBinding, Item : BindableAdapterItem> internal constructor(
        override val bindingResourceId: Int,
        override val viewDataBinding: Binding
) : RecyclerView.ViewHolder(viewDataBinding.root), BindableAdapter.ViewHolder<Binding, Item> {

    override var boundAdapterItem: Item? = null

    companion object {
        @RestrictTo(RestrictTo.Scope.TESTS)
        fun <Item : BindableAdapterItem> typedTestViewHolder(
                bindingResourceId: Int = 0,
                viewDataBinding: ViewDataBinding
        ) = BindingRecyclerViewItemViewHolder<ViewDataBinding, Item>(bindingResourceId, viewDataBinding)
    }
}