package com.aidanvii.toolbox.adapterviews.recyclerpager

import android.support.v4.view.PagerAdapter.POSITION_NONE
import android.support.v4.view.PagerAdapter.POSITION_UNCHANGED
import com.aidanvii.toolbox.checkBelowMax

internal class DataSetChangeResolver<Item, ViewHolder : RecyclerPagerAdapter.ViewHolder>(
    private val callback: RecyclerPagerAdapter.OnDataSetChangedCallback<Item>,
    private val maxAdapterPosition: Int
) {

    fun resolvePageItemPosition(pageItem: PageItem<ViewHolder>): Int {
        val oldAdapterPosition = pageItem.adapterPosition
        val oldItem = getOldItemModel(oldAdapterPosition)
        val newAdapterPosition = getNewAdapterPosition(oldItem)
        return if (newAdapterPosition < 0) POSITION_NONE
        else resolvePosition(
            itemsDifferent = itemsDifferent(
                oldItem = oldItem,
                newItem = getNewItemModel(newAdapterPosition)
            ),
            positionChanged = oldAdapterPosition != newAdapterPosition,
            newAdapterPosition = newAdapterPosition
        )

    }

    private fun getOldItemModel(oldAdapterPosition: Int): Item =
        callback.getOldItemAt(oldAdapterPosition)

    private fun getNewAdapterPosition(oldItem: Item): Int =
        callback.getNewAdapterPositionOfItem(oldItem).checkBelowMax(maxAdapterPosition)

    private fun getNewItemModel(newAdapterPosition: Int): Item =
        callback.getNewItemAt(newAdapterPosition)

    private fun itemsDifferent(oldItem: Item, newItem: Item): Boolean =
        !callback.areItemsTheSame(oldItem, newItem)

    private fun resolvePosition(itemsDifferent: Boolean, positionChanged: Boolean, newAdapterPosition: Int): Int =
        when {
            itemsDifferent -> POSITION_NONE
            positionChanged -> newAdapterPosition
            else -> POSITION_UNCHANGED
        }
}
