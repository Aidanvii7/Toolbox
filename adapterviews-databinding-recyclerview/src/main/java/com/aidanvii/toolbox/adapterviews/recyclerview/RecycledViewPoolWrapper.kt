package com.aidanvii.toolbox.adapterviews.recyclerview

import androidx.recyclerview.widget.RecyclerView
import com.aidanvii.toolbox.Provider

class RecycledViewPoolWrapper : Provider<androidx.recyclerview.widget.RecyclerView.RecycledViewPool> {

    private val recycledViewPool by lazy(LazyThreadSafetyMode.NONE) { androidx.recyclerview.widget.RecyclerView.RecycledViewPool() }

    override fun invoke() = recycledViewPool

    fun setMaxRecycledViews(viewType: Int, max: Int) {
        recycledViewPool.setMaxRecycledViews(viewType, max)
    }

    fun clear() {
        recycledViewPool.clear()
    }
}