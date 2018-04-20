package com.aidanvii.toolbox.adapterviews.recyclerview

import android.support.v7.widget.RecyclerView
import com.aidanvii.toolbox.DisposableItem
import com.aidanvii.toolbox.adapterviews.databinding.BindableAdapterItem
import com.aidanvii.toolbox.databinding.ObservableViewModel

internal class AdapterNotifierDataObserver<Item : BindableAdapterItem>(
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
                if (adapterItem.lazyBindableItem.isInitialized()) {
                    (adapterItem.lazyBindableItem.value as? AdapterNotifier)?.bindAdapter(adapter)
                }
            }
        }
    }

    private fun unbindRange(positionStart: Int, itemCount: Int) {
        (positionStart until positionStart + itemCount).forEach { position ->
            (adapter.tempPreviousItems?.get(position))?.let { adapterItem ->
                adapterItem.dispose()
                if (adapterItem.lazyBindableItem.isInitialized()) {
                    (adapterItem.lazyBindableItem.value as? AdapterNotifier)?.unbindAdapter(adapter)
                }
            }
        }
    }
}