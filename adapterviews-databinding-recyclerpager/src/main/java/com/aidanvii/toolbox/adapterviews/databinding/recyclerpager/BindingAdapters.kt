package com.aidanvii.toolbox.adapterviews.databinding.recyclerpager

import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import androidx.viewpager.widget.ViewPager
import com.aidanvii.toolbox.adapterviews.databinding.BindableAdapterItem
import com.aidanvii.toolbox.databinding.IntBindingConsumer
import com.aidanvii.toolbox.databinding.trackInstance
import com.aidanvii.toolbox.unchecked

@BindingAdapter(
    "binder",
    "items", requireAll = true
)
internal fun <Item : BindableAdapterItem> ViewPager._bind(
    binder: BindingRecyclerPagerBinder<Item>?,
    items: List<Item>?
) {
    tryRebind(binder)
    setItemsOnExistingAdapter(items)
    tryRestorePosition(binder)
}

private fun <Item : BindableAdapterItem> ViewPager.tryRebind(binder: BindingRecyclerPagerBinder<Item>?) {
    trackInstance(
        newInstance = binder,
        instanceResId = R.id.list_binder,
        onDetached = { detachedBinder ->
            detachedBinder.adapterState = bindingRecyclerPagerAdapter<Item>()?.saveState()
            bindingRecyclerPagerAdapter<Item>()?.items = emptyList()
            adapter = null
        },
        onAttached = { attachedBinder ->
            attachedBinder.applicationContext = context.applicationContext
            adapter = attachedBinder.adapter
        })
}

private fun <Item : BindableAdapterItem> ViewPager.setItemsOnExistingAdapter(items: List<Item>?) {
    bindingRecyclerPagerAdapter<Item>()?.apply {
        items?.let { this.items = items }
    }
}

private fun <Item : BindableAdapterItem> ViewPager.tryRestorePosition(binder: BindingRecyclerPagerBinder<Item>?) {
    val bindingRecyclerPagerAdapter = bindingRecyclerPagerAdapter<Item>()
    val stateFromBinder = binder?.adapterState
    val stateFromRestoredAdapter = bindingRecyclerPagerAdapter?.pendingSavedState
    currentItem = when {
        stateFromBinder != null -> stateFromBinder.restoredPosition.also {
            binder.adapterState = null
        }
        stateFromRestoredAdapter != null -> stateFromRestoredAdapter.restoredPosition.also {
            bindingRecyclerPagerAdapter.pendingSavedState = null
        }
        else -> 0
    }
}

@Suppress(unchecked)
private fun <Item : BindableAdapterItem> ViewPager.bindingRecyclerPagerAdapter() = adapter as? BindingRecyclerPagerAdapter<Item>

internal var ViewPager._currentItem: Int
    @InverseBindingAdapter(attribute = "currentItem", event = "currentItemAttrChanged")
    get() = currentItem
    @BindingAdapter(value = ["currentItem"])
    set(value) {
        if (currentItem != value)
            currentItem = value
    }

@BindingAdapter(
    "onPageSelected",
    "currentItemAttrChanged", requireAll = false
)
internal fun ViewPager._bind(
    onPageSelected: IntBindingConsumer?,
    currentItemAttrChanged: InverseBindingListener?
) {
    val onPageChangedListener = if (onPageSelected != null || currentItemAttrChanged != null) {
        object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                onPageSelected?.invoke(position)
                currentItemAttrChanged?.onChange()
            }
        }
    } else null

    trackInstance(
        newInstance = onPageChangedListener,
        instanceResId = R.id.on_page_changed_listener,
        onDetached = { removeOnPageChangeListener(it) },
        onAttached = { addOnPageChangeListener(it) })
}