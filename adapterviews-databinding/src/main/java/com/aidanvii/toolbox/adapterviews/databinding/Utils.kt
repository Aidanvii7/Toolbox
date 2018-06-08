package com.aidanvii.toolbox.adapterviews.databinding

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