package com.aidanvii.toolbox.adapterviews.databinding

import androidx.annotation.LayoutRes
import android.util.SparseIntArray

class MultiViewTypeHandler<Item : BindableAdapterItem> : BindableAdapter.ViewTypeHandler<Item> {

    private lateinit var bindingAdapter: BindableAdapter<Item, *>

    private val cachedBindingIds = SparseIntArray()

    override fun initBindableAdapter(bindableAdapter: BindableAdapter<Item, *>) {
        this.bindingAdapter = bindableAdapter
    }

    override fun getItemViewType(position: Int): Int {
        return bindingAdapter.getItem(position).let { adapterItem ->
            adapterItem.layoutId.also {
                cachedBindingIds.put(it, adapterItem.bindingId)
            }
        }
    }

    @LayoutRes
    override fun getLayoutId(viewType: Int): Int = viewType

    override fun getBindingId(layoutId: Int): Int = cachedBindingIds.get(layoutId)
}
