package com.aidanvii.toolbox.adapterviews.recyclerpager

import androidx.viewpager.widget.PagerAdapter.POSITION_NONE
import androidx.viewpager.widget.PagerAdapter.POSITION_UNCHANGED
import com.aidanvii.toolbox.checkBelowMax

internal class DataSetChangeResolver<Item, ViewHolder : RecyclerPagerAdapter.ViewHolder>(
    private val callback: RecyclerPagerAdapter.OnDataSetChangedCallback<Item>,
    private val maxAdapterPosition: Int
) {

    fun resolvePageItemPosition(pageItem: PageItem<ViewHolder>): Int {
        val oldPosition = pageItem.adapterPosition
        val oldItem = getOldItem(oldPosition)
        val newPosition = getNewAdapterPosition(oldItem)
        return if (newPosition < 0) POSITION_NONE
        else resolvePosition(
            pageItem = pageItem,
            itemsDifferent = itemsDifferent(
                oldItem = oldItem,
                newItem = getNewItem(newPosition)
            ),
            positionChanged = oldPosition != newPosition,
            newAdapterPosition = newPosition
        )

    }

    private fun getOldItem(oldAdapterPosition: Int): Item =
        callback.getOldItemAt(oldAdapterPosition)

    private fun getNewAdapterPosition(oldItem: Item): Int =
        callback.getNewAdapterPositionOfItem(oldItem).checkBelowMax(maxAdapterPosition)

    private fun getNewItem(newAdapterPosition: Int): Item =
        callback.getNewItemAt(newAdapterPosition)

    private fun itemsDifferent(oldItem: Item, newItem: Item): Boolean =
        !callback.areItemsTheSame(oldItem, newItem)

    private fun resolvePosition(
        pageItem: PageItem<ViewHolder>,
        itemsDifferent: Boolean,
        positionChanged: Boolean,
        newAdapterPosition: Int
    ): Int =
        when {
            itemsDifferent -> POSITION_NONE
            positionChanged -> newAdapterPosition.also { pageItem.adapterPosition = newAdapterPosition }
            else -> POSITION_UNCHANGED
        }
}
