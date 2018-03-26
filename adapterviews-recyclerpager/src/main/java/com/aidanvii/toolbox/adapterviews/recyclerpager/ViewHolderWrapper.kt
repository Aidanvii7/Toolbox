package com.aidanvii.toolbox.adapterviews.recyclerpager

import android.view.View
import android.view.ViewGroup
import com.aidanvii.toolbox.throwIfTrue

internal class ViewHolderWrapper<out ViewHolder : RecyclerPagerAdapter.ViewHolder>(
        val viewHolder: ViewHolder,
        override val itemType: Int
) : PooledItem {

    init {
        validateView()
    }

    private var tempView: View? = null

    fun addViewToContainer(container: ViewGroup) {
        if (isAttachedToContainer(container)) {
            container.replaceViewWithTempView()
        }
        container.addViewHolderView()
    }

    fun removeViewFromContainer(container: ViewGroup) {
        if (tempView != null) {
            container.removeTempView()
        } else {
            container.removeViewHolderView()
        }
    }

    override fun destroy() {
        viewHolder.onDestroyed()
    }

    private fun validateView() {
        this.viewHolder.view.apply {
            (parent != null).throwIfTrue(this.toString() + " already has a parent.")
        }
    }

    private fun ViewGroup.addViewHolderView() {
        viewHolder.addViewToContainer(this)
        assertAttached(this)
    }

    private fun ViewGroup.removeViewHolderView() {
        viewHolder.removeViewFromContainer(this)
        assertDetached(this)
    }

    private fun assertAttached(container: ViewGroup) {
        if (!isAttachedToContainer(container)) {
            throw IllegalStateException("ViewHolder View is not attached to container")
        }
    }

    private fun assertDetached(container: ViewGroup) {
        if (isAttachedToContainer(container)) {
            throw IllegalStateException("ViewHolder View in attached to container")
        }
    }

    private fun ViewGroup.replaceViewWithTempView() {
        val viewIndex = indexOfChild(viewHolder.view)
        removeViewHolderView()
        tempView = View(context)
        addView(tempView, viewIndex)
    }

    private fun ViewGroup.removeTempView() {
        removeView(tempView)
        tempView = null
    }

    private fun isAttachedToContainer(container: ViewGroup): Boolean {
        return viewHolder.view.parent != null && viewHolder.view.parent == container
    }
}
