package com.aidanvii.toolbox.adapterviews.recyclerpager

import android.view.ViewGroup

/**
 * Represents the item held internally by the [android.support.v4.view.ViewPager]
 */
internal class PageItem<ViewHolder : RecyclerPagerAdapter.ViewHolder>(
    val viewType: Int,
    val adapterPosition: Int
) {

    lateinit var viewHolderWrapper: ViewHolderWrapper<ViewHolder>
    var viewTransaction: ViewTransaction? = null

    fun runPendingTransaction(container: ViewGroup) {
        viewTransaction?.run(container, this)
        viewTransaction = null
    }
}