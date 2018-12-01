package com.aidanvii.toolbox.adapterviews.recyclerview;

import android.graphics.Canvas
import android.graphics.Rect
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import com.aidanvii.toolbox.actionStub
import com.aidanvii.toolbox.adapterviews.databinding.BindableAdapterItem
import com.aidanvii.toolbox.children
import com.aidanvii.toolbox.unchecked

abstract class BindableItemDecoration<AdapterItem : BindableAdapterItem> : androidx.recyclerview.widget.RecyclerView.ItemDecoration() {

    protected abstract fun getItemOffsets(adapterItem: AdapterItem, outRect: Rect, layoutParams: androidx.recyclerview.widget.RecyclerView.LayoutParams, state: androidx.recyclerview.widget.RecyclerView.State)

    protected abstract fun onDraw(adapterItem: AdapterItem, canvas: Canvas, state: androidx.recyclerview.widget.RecyclerView.State)

    override fun onDraw(canvas: Canvas, recyclerView: androidx.recyclerview.widget.RecyclerView, state: androidx.recyclerview.widget.RecyclerView.State) {
        for (childView in recyclerView.children) {
            onAdapter(recyclerView) {
                onDraw(
                    adapterItem = items[recyclerView.getChildAdapterPosition(childView)],
                    canvas = canvas,
                    state = state
                )
            }
        }
    }

    override fun getItemOffsets(outRect: Rect, view: View, recyclerView: androidx.recyclerview.widget.RecyclerView, state: androidx.recyclerview.widget.RecyclerView.State) {
        onAdapter(recyclerView) {
            getItemOffsets(
                adapterItem = items[recyclerView.getChildAdapterPosition(view)],
                outRect = outRect,
                layoutParams = view.layoutParams as androidx.recyclerview.widget.RecyclerView.LayoutParams,
                state = state
            )
        }
    }

    @Suppress(unchecked)
    private inline fun onAdapter(
        recyclerView: androidx.recyclerview.widget.RecyclerView,
        block: BindingRecyclerViewAdapter<AdapterItem>.() -> Unit
    ) {
        recyclerView.adapter.let { adapter ->
            when (adapter) {
                null -> actionStub
                is BindingRecyclerViewAdapter<*> -> (adapter as BindingRecyclerViewAdapter<AdapterItem>).block()
                else -> throwWrongAdapterType(adapter)
            }
        }
    }

    private fun throwWrongAdapterType(unsupportedAdapter: androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>): Nothing {
        throw UnsupportedOperationException(
            """
            cannot attach ${this::class.java.simpleName} to ${unsupportedAdapter::class.java.simpleName},
            ${unsupportedAdapter::class.java.simpleName} must extend BindingRecyclerViewAdapter.
            """
        )
    }
}