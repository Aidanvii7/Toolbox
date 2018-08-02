package com.aidanvii.toolbox.adapterviews.recyclerview

import com.aidanvii.toolbox.adapterviews.databinding.BindableAdapterItem

internal class AdapterNotifierItemBoundObserver<Item : BindableAdapterItem> : ItemBoundObserver<Item> {

    override fun onItemBound(item: Item, adapter: BindingRecyclerViewAdapter<Item>) {
        (item.lazyBindableItem.value as? AdapterNotifier)?.bindAdapter(adapter)
    }

    override fun onItemUnBound(item: Item, adapter: BindingRecyclerViewAdapter<Item>) {
        item.lazyBindableItem.apply {
            if (isInitialized()) {
                (value as? AdapterNotifier)?.unbindAdapter(adapter)
            }
        }
    }
}