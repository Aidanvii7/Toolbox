package com.aidanvii.toolbox.adapterviews.databinding.recyclerview

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.aidanvii.toolbox.adapterviews.databinding.BindableAdapterItem
import com.aidanvii.toolbox.databinding.BindingAction
import com.aidanvii.toolbox.databinding.IntBindingConsumer
import com.aidanvii.toolbox.databinding.trackInstance
import com.aidanvii.toolbox.unchecked

@BindingAdapter(
    "binder",
    "items",
    "onItemBoundAt",
    "onItemsSet", requireAll = false
)
internal fun <Item : BindableAdapterItem> RecyclerView._bind(
    binder: BindingRecyclerViewBinder<Item>?,
    items: List<Item>?,
    itemBoundListener: IntBindingConsumer?,
    onItemsSet: BindingAction? = null // TODO tidy this up
) {
    tryRebind(binder, itemBoundListener, onItemsSet)
    setItemsOnExistingAdapter(items)
}

private fun <Item : BindableAdapterItem> RecyclerView.tryRebind(
    binder: BindingRecyclerViewBinder<Item>?,
    itemBoundListener: IntBindingConsumer?,
    onItemsSet: BindingAction?
) {
    trackInstance(
        newInstance = binder,
        instanceResId = R.id.list_binder,
        onDetached = { detachedBinder ->
            detachedBinder.layoutManagerState = layoutManager?.onSaveInstanceState()
            bindingRecyclerViewAdapter<Item>()?.itemBoundListener = null
            adapter = null
        },
        onAttached = { attachedBinder ->
            val freshAdapter = attachedBinder.adapter
            if (itemBoundListener != null) {
                freshAdapter.itemBoundListener = itemBoundListener
            }
            if (onItemsSet != null) {
                freshAdapter.onItemsSet = onItemsSet
            }
            adapter = freshAdapter
            layoutManager = attachedBinder.layoutManagerFactory(context).apply {
                onRestoreInstanceState(attachedBinder.layoutManagerState)
            }
            attachedBinder.recycledViewPoolWrapper?.invoke()?.let { pool ->
                if (recycledViewPool !== pool) setRecycledViewPool(pool)
            }
        }
    )
}

@Suppress(unchecked)
private fun <Item : BindableAdapterItem> RecyclerView.bindingRecyclerViewAdapter() = adapter as? BindingRecyclerViewAdapter<Item>

private fun <Item : BindableAdapterItem> RecyclerView.setItemsOnExistingAdapter(items: List<Item>?) {
    val adapter = bindingRecyclerViewAdapter<Item>()
    if (adapter != null && items != null) {
        adapter.items = items
    }
}

@BindingAdapter("layoutManager")
internal fun RecyclerView._bind(layoutManager: RecyclerView.LayoutManager?) {
    trackInstance(
        newInstance = layoutManager,
        instanceResId = R.id.layout_manager,
        onDetached = { this.layoutManager = null },
        onAttached = { this.layoutManager = it })
}

@BindingAdapter("bindableItemDecoration")
internal fun <Item : BindableAdapterItem> RecyclerView._bind(itemDecoration: BindableItemDecoration<Item>?) {
    trackInstance(
        newInstance = itemDecoration,
        instanceResId = R.id.item_decoration,
        onDetached = { this.removeItemDecoration(it) },
        onAttached = { this.addItemDecoration(it) })
}