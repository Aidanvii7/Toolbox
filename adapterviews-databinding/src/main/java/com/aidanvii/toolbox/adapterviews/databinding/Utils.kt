package com.aidanvii.toolbox.adapterviews.databinding

import com.aidanvii.toolbox.DisposableItem

val defaultAreItemsSame = { oldItem: BindableAdapterItem, newItem: BindableAdapterItem ->
    when {
    // if the old item is empty, assume that they represent the same item as long as they represent the same view type
        oldItem.isEmpty -> oldItem.layoutId == newItem.layoutId
        oldItem == newItem -> true
        else -> false
    }
}

val defaultAreContentsSame = { oldItem: BindableAdapterItem, newItem: BindableAdapterItem -> oldItem == newItem }

val defaultGetChangedProperties: (Any, Any) -> IntArray? = { _, _ -> null }

fun List<BindableAdapterItem>.disposeAll() {
    for (bindableAdapterItem in this) {
        bindableAdapterItem.lazyBindableItem.apply {
            if (isInitialized()) {
                (value as? DisposableItem)?.dispose()
            }
        }
    }
}