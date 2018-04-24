package com.aidanvii.toolbox.adapterviews.recyclerview

import com.aidanvii.toolbox.adapterviews.databinding.BindableAdapterItem

internal class DataObserverAdapterNotifierPlugin<Item : BindableAdapterItem> : BindableAdapterItemDataObserver.Plugin<Item> {

    override fun onItemBound(item: Item, adapter: BindingRecyclerViewAdapter<Item>, isChanging: Boolean) {
        if (item.lazyBindableItem.isInitialized()) {
            (item.lazyBindableItem.value as? AdapterNotifier)?.bindAdapter(adapter)
        }
    }

    override fun onItemUnBound(item: Item, adapter: BindingRecyclerViewAdapter<Item>, isChanging: Boolean) {
        if (item.lazyBindableItem.isInitialized()) {
            (item.lazyBindableItem.value as? AdapterNotifier)?.unbindAdapter(adapter)
        }
    }
}