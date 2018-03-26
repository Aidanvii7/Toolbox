package com.aidanvii.toolbox.adapterviews.recyclerpager

import android.util.SparseArray
import android.util.SparseIntArray
import com.aidanvii.toolbox.Provider
import com.aidanvii.toolbox.arrayListOfSize
import com.aidanvii.toolbox.getNullable
import com.aidanvii.toolbox.getWithDefault
import com.aidanvii.toolbox.iterator
import com.aidanvii.toolbox.valueAtNullable

/**
 * Represents a pool of [PooledItem] objects that are keyed on their [PooledItem.itemType] -
 * essentially a wrapper around:
 * ```
 * Map<Int, <MutableList<PooledItem>>>
 * ```
 */
class ItemPool<P : PooledItem> {

    companion object {
        const val DEFAULT_MAX_SCRAP = 3
        const val DEFAULT_TYPE_SIZE = 1
    }

    private val scrapHeapsByType = SparseArray<ScrapHeap<P>>(DEFAULT_TYPE_SIZE)
    private val maxScrapByType = SparseIntArray(DEFAULT_TYPE_SIZE)

    /**
     * A flattened list of all [PooledItem] objects in all typed pools.
     */
    val scrapItems: List<P> get() = scrapHeapsByType.iterator().flatten()

    /**
     * Puts the [pooledItem] in it's respective pool based on it's [PooledItem.itemType].
     *
     * If the pool already contains the maximum amount of [PooledItem] for the [PooledItem.itemType],
     * the [pooledItem] will not be put in the pool.
     *
     * @throws IllegalArgumentException if the [pooledItem] already exists within the pool.
     */
    fun putItem(pooledItem: P) {
        val itemType = pooledItem.itemType
        val scrapItems = getScrapHeapForType(itemType)
        if (maxScrapByType.get(itemType) <= scrapItems.size) {
            pooledItem.destroy()
        } else {
            if (scrapItems.contains(pooledItem)) {
                throw IllegalArgumentException("this scrap item already exists in the scrapHeap")
            }
            scrapItems.add(pooledItem)
        }
    }

    /**
     * Pops the last [PooledItem] for the given [itemType] - if any, otherwise null.
     */
    fun popItem(itemType: Int): P? {
        return scrapHeapsByType.getNullable(itemType)?.let { scrapItems ->
            if (scrapItems.isNotEmpty()) {
                val index = scrapItems.size - 1
                scrapItems[index].also {
                    scrapItems.removeAt(index)
                }
            } else null
        }
    }

    /**
     * Pops the last [PooledItem] for the given [itemType] - if any,
     * otherwise [provideDefault] should provide a [PooledItem].
     */
    inline fun popItem(itemType: Int, provideDefault: Provider<P>): P =
            popItem(itemType) ?: provideDefault()

    /**
     * Sets the maximum amount of [PooledItem] objects that can be held
     * within the pool for the given [itemType].
     */
    fun setMaxRecycled(itemType: Int, max: Int) {
        maxScrapByType.put(itemType, max)
        val scrapItems = scrapHeapsByType.getNullable(itemType)
        if (scrapItems != null) {
            while (scrapItems.size > max) {
                scrapItems.removeAt(scrapItems.size - 1)
            }
        }
    }

    /**
     * Returns true if [pooledItem] exists in the pool.
     *
     * Usage:
     * ```
     * if (pooled in itemPool) doAction()
     * ```
     */
    operator fun contains(pooledItem: P): Boolean {
        return pooledItem.itemType.let { itemType ->
            if (itemType < scrapHeapsByType.size()) {
                scrapHeapsByType.get(itemType).contains(pooledItem)
            } else false
        }
    }

    /**
     * Gets the size of all typed pools for all item types combined.
     */
    val size: Int get() = (0 until scrapHeapsByType.size()).sumBy { getSizeForType(it) }

    /**
     * Gets the size of the pool for [itemType].
     */
    fun getSizeForType(itemType: Int): Int {
        return if (itemType < scrapHeapsByType.size()) {
            scrapHeapsByType.valueAtNullable(itemType)?.size ?: 0
        } else 0
    }

    /**
     * Clears all pools for all item types.
     */
    fun clear() {
        scrapItems.forEach { destroyScrapHeap(it) }
        scrapHeapsByType.clear()
    }

    private fun getScrapHeapForType(itemType: Int): ScrapHeap<P> {
        return scrapHeapsByType.getWithDefault(itemType) {
            arrayListOfSize<P>(DEFAULT_MAX_SCRAP).also { newScrapHeap ->
                this.scrapHeapsByType.put(itemType, newScrapHeap)
                if (maxScrapByType.indexOfKey(itemType) < 0) {
                    maxScrapByType.put(itemType, DEFAULT_MAX_SCRAP)
                }
            }
        }
    }

    private fun destroyScrapHeap(scrapItem: P) {
        scrapItem.destroy()
    }
}

