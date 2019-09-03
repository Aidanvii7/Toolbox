package com.aidanvii.toolbox.adapterviews.recyclerview

import androidx.recyclerview.widget.RecyclerView
import com.aidanvii.toolbox.Provider

class RecycledViewPoolWrapper : Provider<RecyclerView.RecycledViewPool> {

    private val recycledViewPool by lazy(LazyThreadSafetyMode.NONE) { RecyclerView.RecycledViewPool() }

    override fun invoke() = recycledViewPool

    fun setMaxRecycledViews(viewType: Int, max: Int) {
        recycledViewPool.setMaxRecycledViews(viewType, max)
    }

    fun clear() {
        recycledViewPool.clear()
    }
}