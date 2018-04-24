package com.aidanvii.toolbox.adapterviews.recyclerview

import com.aidanvii.toolbox.adapterviews.databinding.BindableAdapterItem

internal class DataObserverDisposalPlugin<Item : BindableAdapterItem> : BindableAdapterItemDataObserver.Plugin<Item> {

    override fun onItemUnBound(item: Item, adapter: BindingRecyclerViewAdapter<Item>, isChanging: Boolean) {
        if (!isChanging) {
            item.dispose()
        }
    }
}