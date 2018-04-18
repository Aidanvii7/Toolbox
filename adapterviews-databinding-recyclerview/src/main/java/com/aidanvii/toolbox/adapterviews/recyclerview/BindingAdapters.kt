package com.aidanvii.toolbox.adapterviews.recyclerview

import android.databinding.BindingAdapter
import android.support.v7.widget.RecyclerView
import com.aidanvii.toolbox.adapterviews.databinding.BindableAdapterItem
import com.aidanvii.toolbox.adapterviews.databinding.recyclerview.R
import com.aidanvii.toolbox.databinding.IntBindingConsumer
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
    if (localAdapter != null && items != null) {
        localAdapter.items = items
    }
    trackInstance(
        newInstance = binder,
        instanceResId = R.id.list_binder,
        onDetached = { detachedBinder ->
            detachedBinder.layoutManagerState = layoutManager?.onSaveInstanceState()
            detachedBinder.adapter.itemBoundListener = null
        },
        onAttached = { attachedBinder ->
            itemBoundListener?.let { attachedBinder.adapter.itemBoundListener = it }
            adapter = attachedBinder.adapter
            attachedBinder.apply {
                layoutManagerState?.let { layoutManagerState ->
                    attachedBinder.adapter.runAfterUpdate = {
                        layoutManager?.onRestoreInstanceState(layoutManagerState)
                    }
                }
                layoutManager = layoutManagerFactory(context)
            }
        }
    )
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