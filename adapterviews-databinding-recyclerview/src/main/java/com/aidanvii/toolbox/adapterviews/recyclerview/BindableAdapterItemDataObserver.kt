package com.aidanvii.toolbox.adapterviews.recyclerview

import android.support.v7.widget.RecyclerView
import com.aidanvii.toolbox.adapterviews.databinding.BindableAdapterItem

internal class BindableAdapterItemDataObserver<Item : BindableAdapterItem>(
    private val adapter: BindingRecyclerViewAdapter<Item>,
    private vararg val plugins: Plugin<Item>
) : RecyclerView.AdapterDataObserver() {


    interface Plugin<Item : BindableAdapterItem> {

        fun onItemBound(item: Item, adapter: BindingRecyclerViewAdapter<Item>, isChanging: Boolean) {}

        fun onItemUnBound(item: Item, adapter: BindingRecyclerViewAdapter<Item>, isChanging: Boolean) {}
    }

    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
        bindRange(positionStart, itemCount, false)
    }

    override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
        unbindRange(positionStart, itemCount, false)
    }

    override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
        unbindRange(positionStart, itemCount, true)
        bindRange(positionStart, itemCount, true)
    }

    private fun bindRange(positionStart: Int, itemCount: Int, isChanging: Boolean) {
        (positionStart until positionStart + itemCount).forEach { position ->
            (adapter.getItem(position)).let { adapterItem ->
                plugins.forEach { it.onItemBound(adapterItem, adapter, isChanging) }
            }
        }
    }

    private fun unbindRange(positionStart: Int, itemCount: Int, isChanging: Boolean) {
        (positionStart until positionStart + itemCount).forEach { position ->
            (adapter.tempPreviousItems?.get(position))?.let { adapterItem ->
                plugins.forEach { it.onItemUnBound(adapterItem, adapter, isChanging) }
            }
        }
    }
}