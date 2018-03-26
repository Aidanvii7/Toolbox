package com.aidanvii.toolbox.adapterviews.databinding

import java.lang.ref.WeakReference
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

val defaultAreItemsSame = { oldItem: BindableAdapterItem, newItem: BindableAdapterItem ->
    when {
        // if the old item is empty, assume that they represent the same item as long as they represent the same view type
        oldItem.isEmpty -> oldItem.layoutId == newItem.layoutId
        oldItem == newItem -> true
        else -> false
    }
}

val defaultAreContentsSame = { oldItem: BindableAdapterItem, newItem: BindableAdapterItem -> oldItem == newItem }