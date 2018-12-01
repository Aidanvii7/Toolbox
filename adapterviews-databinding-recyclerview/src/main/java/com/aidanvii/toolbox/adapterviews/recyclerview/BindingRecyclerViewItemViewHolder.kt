package com.aidanvii.toolbox.adapterviews.recyclerview

import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.aidanvii.toolbox.adapterviews.databinding.BindableAdapter
import com.aidanvii.toolbox.adapterviews.databinding.BindableAdapterItem

class BindingRecyclerViewItemViewHolder<out Binding : ViewDataBinding, Item : BindableAdapterItem> internal constructor(
    override val bindingResourceId: Int,
    override val viewDataBinding: Binding,
    private val itemBoundObservers: List<ItemBoundObserver<Item>>
) : androidx.recyclerview.widget.RecyclerView.ViewHolder(viewDataBinding.root), BindableAdapter.ViewHolder<Binding, Item> {

    internal var currentAdapter: BindingRecyclerViewAdapter<Item>? = null

    override var boundAdapterItem: Item? = null
        set(value) {
            itemBoundObservers.forEach { observer ->
                field?.let { observer.onItemUnBound(it, currentAdapter!!) }
                value?.let { observer.onItemBound(it, currentAdapter!!) }
            }
            field = value
        }
}