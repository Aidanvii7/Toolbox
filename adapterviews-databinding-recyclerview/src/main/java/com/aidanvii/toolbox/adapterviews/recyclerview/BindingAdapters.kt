package com.aidanvii.toolbox.adapterviews.recyclerview

import android.databinding.BindingAdapter
import android.support.v7.widget.RecyclerView
import com.aidanvii.toolbox.adapterviews.databinding.BindableAdapterItem
import com.aidanvii.toolbox.adapterviews.databinding.recyclerview.R
import com.aidanvii.toolbox.databinding.IntBindingConsumer
import com.aidanvii.toolbox.databinding.getTrackedValue
import com.aidanvii.toolbox.databinding.trackInstance

@BindingAdapter(
    "android:binder",
    "android:items",
    "android:onItemBoundAt", requireAll = false
)
internal fun <Item : BindableAdapterItem> RecyclerView._bind(
    binder: BindingRecyclerViewBinder<Item>?,
    items: List<Item>?,
    itemBoundListener: IntBindingConsumer?
) {
    val localAdapter = binder?.adapter
    setItemsOnAdapter(localAdapter, items)
    rebind(binder, itemBoundListener)
}

private fun <Item : BindableAdapterItem> RecyclerView.rebind(
    binder: BindingRecyclerViewBinder<Item>?,
    itemBoundListener: IntBindingConsumer?
) {
    trackInstance(
        newInstance = binder,
        instanceResId = R.id.list_binder,
        onDetached = { detachedBinder ->
            detachedBinder.layoutManagerState = layoutManager?.onSaveInstanceState()
            detachedBinder.adapter.itemBoundListener = null
        },
        onAttached = { attachedBinder ->
            val localAdapter = attachedBinder.adapter
            if (itemBoundListener != null) {
                localAdapter.itemBoundListener = itemBoundListener
            }
            adapter = localAdapter
            localAdapter.runAfterUpdate = {
                layoutManager?.onRestoreInstanceState(attachedBinder.layoutManagerState)
            }
            layoutManager = attachedBinder.layoutManagerFactory(context)
        }
    )
}

private fun <Item : BindableAdapterItem> setItemsOnAdapter(
    adapter: BindingRecyclerViewAdapter<Item>?,
    items: List<Item>?
) {
    if (adapter != null && items != null) {
        adapter.items = items
    }
}

@BindingAdapter("android:layoutManager")
internal fun RecyclerView._bind(layoutManager: RecyclerView.LayoutManager?) {
    trackInstance(
        newInstance = layoutManager,
        instanceResId = R.id.layout_manager,
        onDetached = { this.layoutManager = null },
        onAttached = { this.layoutManager = it })
}

@BindingAdapter("android:itemDecoration")
internal fun RecyclerView._bind(itemDecoration: RecyclerView.ItemDecoration?) {
    trackInstance(
        newInstance = itemDecoration,
        instanceResId = R.id.item_decoration,
        onDetached = { this.removeItemDecoration(it) },
        onAttached = { this.addItemDecoration(it) })
}