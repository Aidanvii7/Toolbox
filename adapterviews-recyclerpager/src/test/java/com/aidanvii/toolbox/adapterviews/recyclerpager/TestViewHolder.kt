package com.aidanvii.toolbox.adapterviews.recyclerpager

import android.view.View
import android.view.ViewGroup
import com.aidanvii.toolbox.minusAssign
import com.aidanvii.toolbox.plusAssign
import com.nhaarman.mockito_kotlin.whenever

class TestViewHolder(
        override val view: View,
        val onAddViewToContainer: (ViewGroup) -> Unit = {
            it += view
            whenever(view.parent).thenReturn(it)
        },
        val onRemoveViewToContainer: (ViewGroup) -> Unit = {
            it -= view
            whenever(view.parent).thenReturn(null)
        },
        var currentData: Any? = null
) : RecyclerPagerAdapter.ViewHolder {


    override fun addViewToContainer(container: ViewGroup) {
        onAddViewToContainer(container)
    }

    override fun removeViewFromContainer(container: ViewGroup) {
        onRemoveViewToContainer(container)
    }

    companion object {
        const val VIEW_TYPE_0 = 0
    }
}