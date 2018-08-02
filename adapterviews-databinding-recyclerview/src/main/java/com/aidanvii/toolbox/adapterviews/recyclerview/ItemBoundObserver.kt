package com.aidanvii.toolbox.adapterviews.recyclerview

import com.aidanvii.toolbox.adapterviews.databinding.BindableAdapterItem

internal interface ItemBoundObserver<Item : BindableAdapterItem> {

    fun onItemBound(item: Item, adapter: BindingRecyclerViewAdapter<Item>) {}

    fun onItemUnBound(item: Item, adapter: BindingRecyclerViewAdapter<Item>) {}
}