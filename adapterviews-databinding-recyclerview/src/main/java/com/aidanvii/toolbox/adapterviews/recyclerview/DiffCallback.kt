package com.aidanvii.toolbox.adapterviews.recyclerview

import android.support.v7.util.DiffUtil

val defaultGetChangePayload: (Any, Any) -> Any? = { _: Any, _: Any -> null }

inline fun <Item : Any> diffCallback(
        oldItems: List<Item>,
        newItems: List<Item>,
        crossinline areItemsTheSame: ((old: Item, new: Item) -> Boolean),
        crossinline areContentsTheSame: ((old: Item, new: Item) -> Boolean),
        noinline getChangePayload: ((old: Item, new: Item) -> Any?) = defaultGetChangePayload
): DiffCallback<Item> {

    return object : DiffCallback<Item>(oldItems, newItems) {

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                areItemsTheSame(oldItems[oldItemPosition], newItems[newItemPosition])

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                areContentsTheSame(oldItems[oldItemPosition], newItems[newItemPosition])

        override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? =
                getChangePayload(oldItems[oldItemPosition], newItems[newItemPosition])
    }
}

abstract class DiffCallback<out Item>(
        val oldItems: List<Item>,
        val newItems: List<Item>
) : DiffUtil.Callback() {
    final override fun getOldListSize(): Int = oldItems.size
    final override fun getNewListSize(): Int = newItems.size
}