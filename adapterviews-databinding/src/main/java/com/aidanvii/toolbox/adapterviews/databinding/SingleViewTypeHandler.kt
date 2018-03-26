package com.aidanvii.toolbox.adapterviews.databinding

import android.support.annotation.LayoutRes
import com.aidanvii.toolbox.delegates.weak.weakLazy

class SingleViewTypeHandler<Item : BindableAdapterItem> : BindableAdapter.ViewTypeHandler<Item> {

    private lateinit var bindableAdapter: BindableAdapter<Item, *>

    private val adapterItem by weakLazy { bindableAdapter.getItem(0) }
    private val bindingId get() = adapterItem.bindingId
    private val layoutId get() = adapterItem.layoutId

    override fun initBindableAdapter(bindableAdapter: BindableAdapter<Item, *>) {
        this.bindableAdapter = bindableAdapter
    }

    override fun getItemViewType(position: Int): Int = 0

    @LayoutRes
    override fun getLayoutId(viewType: Int): Int = layoutId

    override fun getBindingId(@LayoutRes layoutId: Int): Int = bindingId
}
