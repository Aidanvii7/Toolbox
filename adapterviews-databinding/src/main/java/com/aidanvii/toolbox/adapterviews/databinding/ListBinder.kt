package com.aidanvii.toolbox.adapterviews.databinding

abstract class ListBinder<Item : BindableAdapterItem>(
        val hasMultipleViewTypes: Boolean = false,
        val areItemsTheSame: ((oldItem: Item, newItem: Item) -> Boolean) = defaultAreItemsSame,
        val areContentsTheSame: ((oldItem: Item, newItem: Item) -> Boolean) = defaultAreContentsSame
) {
    val viewTypeHandler by lazy(LazyThreadSafetyMode.NONE) {
        if (hasMultipleViewTypes) MultiViewTypeHandler<Item>() else SingleViewTypeHandler<Item>()
    }
}