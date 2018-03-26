package com.aidanvii.toolbox.adapterviews.recyclerview

import android.support.v7.widget.RecyclerView
import com.aidanvii.toolbox.adapterviews.databinding.BindableAdapterItem

internal class AdapterNotifierDataObserver<Item: BindableAdapterItem>(
        val adapter: BindingRecyclerViewAdapter<Item>
) : RecyclerView.AdapterDataObserver() {

    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
        bindRange(positionStart, itemCount)
    }

    override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
        unbindRange(positionStart, itemCount)
    }

    override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
        unbindRange(positionStart, itemCount)
        bindRange(positionStart, itemCount)
    }

    private fun bindRange(positionStart: Int, itemCount: Int) {
        (positionStart until positionStart + itemCount).forEach { position ->
            (adapter.getItem(position)).let { adapterItem ->
                (adapterItem.bindableItem as? AdapterNotifier)?.bindAdapter(adapter)
            }
        }
    }

    private fun unbindRange(positionStart: Int, itemCount: Int) {
        (positionStart until positionStart + itemCount).forEach { position ->
            (adapter.tempPreviousItems?.get(position))?.let { adapterItem ->
                (adapterItem.bindableItem as? AdapterNotifier)?.unbindAdapter(adapter)
            }
        }
    }
}