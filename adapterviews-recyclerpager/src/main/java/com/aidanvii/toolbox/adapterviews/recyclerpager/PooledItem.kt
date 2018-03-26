package com.aidanvii.toolbox.adapterviews.recyclerpager


/**
 * Represents an item that can exist inside of an [ItemPool]
 */
interface PooledItem {
    val itemType: Int
    fun destroy() {}
}