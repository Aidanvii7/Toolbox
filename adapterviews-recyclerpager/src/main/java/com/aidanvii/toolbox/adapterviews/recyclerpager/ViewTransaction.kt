package com.aidanvii.toolbox.adapterviews.recyclerpager

import android.view.ViewGroup

internal enum class ViewTransaction {

    ADD, REMOVE, DESTROY;

    fun run(container: ViewGroup, pageItem: PageItem<*>) {
        when (this) {
            ADD -> addToContainer(container, pageItem)
            REMOVE -> removeFromContainer(container, pageItem)
            DESTROY -> {
                removeFromContainer(container, pageItem)
                pageItem.viewHolderWrapper.destroy()
            }
        }
    }

    private fun <VH : RecyclerPagerAdapter.ViewHolder> addToContainer(container: ViewGroup, pageItem: PageItem<VH>) {
        pageItem.apply {
            viewHolderWrapper.viewHolder.let {
                viewHolderWrapper.addViewToContainer(container)
            }
        }
    }

    private fun <VH : RecyclerPagerAdapter.ViewHolder> removeFromContainer(container: ViewGroup, pageItem: PageItem<VH>) {
        pageItem.apply {
            viewHolderWrapper.viewHolder.let {
                viewHolderWrapper.removeViewFromContainer(container)
            }
        }
    }
}

