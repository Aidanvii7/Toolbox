package com.aidanvii.toolbox.adapterviews.databinding.recyclerpager

import android.content.res.Resources
import android.databinding.ViewDataBinding
import android.view.ViewGroup
import com.aidanvii.toolbox.adapterviews.databinding.BindableAdapter
import com.aidanvii.toolbox.adapterviews.databinding.BindableAdapterDelegate
import com.aidanvii.toolbox.adapterviews.databinding.BindableAdapterItem
import com.aidanvii.toolbox.adapterviews.databinding.BindingInflater
import com.aidanvii.toolbox.adapterviews.recyclerpager.RecyclerPagerAdapter
import com.aidanvii.toolbox.databinding.IntBindingConsumer

/**
 * Implementation of [RecyclerPagerAdapter] and [BindableAdapter] that can automatically bind a list of type [BindableAdapterItem].
 *
 * Subclassing this is purely optional, see [BindingRecyclerPagerBinder] for usage with a subclass.
 */
open class BindingRecyclerPagerAdapter<Item : BindableAdapterItem>(
    builder: Builder<Item>,
    itemPoolContainer: ItemPoolContainer<BindingRecyclerPagerItemViewHolder<*, Item>> = itemPoolContainer()
) : RecyclerPagerAdapter<Item, BindingRecyclerPagerItemViewHolder<*, Item>>(itemPoolContainer),
    BindableAdapter<Item, BindingRecyclerPagerItemViewHolder<*, Item>> {

    companion object {
        fun <Item : BindableAdapterItem> itemPoolContainer() = ItemPoolContainer<BindingRecyclerPagerItemViewHolder<*, Item>>()
    }

    class Builder<Item : BindableAdapterItem> internal constructor(
        internal val delegate: BindableAdapterDelegate<Item, BindingRecyclerPagerItemViewHolder<*, Item>>,
        internal val viewTypeHandler: BindableAdapter.ViewTypeHandler<Item>,
        internal val bindingInflater: BindingInflater,
        internal val areItemAndContentsTheSame: ((old: Item, new: Item) -> Boolean)
    )

    private val delegate = builder.delegate.also { it.bindableAdapter = this }
    private val areItemAndContentsTheSame = builder.areItemAndContentsTheSame
    override val viewTypeHandler = builder.viewTypeHandler.also { it.initBindableAdapter(this) }
    override val bindingInflater = builder.bindingInflater
    override var itemBoundListener: IntBindingConsumer? = null
    lateinit var resources: Resources

    override var items = emptyList<Item>()
        set(value) {
            callBackOf(oldItems = field, newItems = value).also { changeCallback ->
                field = value
                notifyDataSetChanged(changeCallback)
            }
        }

    final override fun getItem(position: Int): Item = super.getItem(position)

    final override fun getItemPosition(item: Item): Int = super<BindableAdapter>.getItemPosition(item)

    final override fun getItemViewType(adapterPosition: Int) = viewTypeHandler.getItemViewType(adapterPosition)

    final override fun onCreateViewHolder(
        viewType: Int,
        position: Int,
        container: ViewGroup
    ): BindingRecyclerPagerItemViewHolder<*, Item> =
        delegate.onCreate(container, viewType)

    final override fun createWith(
        bindingResourceId: Int,
        viewDataBinding: ViewDataBinding
    ): BindingRecyclerPagerItemViewHolder<*, Item> {
        return BindingRecyclerPagerItemViewHolder(
            bindingResourceId = bindingResourceId,
            viewDataBinding = viewDataBinding.apply {
                if (::resources.isInitialized.not()) {
                    resources = root.context.applicationContext.resources
                }
            }
        )
    }

    final override fun onBindViewHolder(
        viewHolder: BindingRecyclerPagerItemViewHolder<*, Item>,
        adapterPosition: Int
    ) {
        delegate.onBind(viewHolder, adapterPosition, null)
    }

    final override fun onUnbindViewHolder(
        viewHolder: BindingRecyclerPagerItemViewHolder<*, Item>,
        adapterPosition: Int
    ) {
        delegate.onUnbind(viewHolder, adapterPosition, null)
    }

    final override fun onDestroyViewHolder(
        viewHolder: BindingRecyclerPagerItemViewHolder<*, Item>,
        adapterPosition: Int
    ) {
        delegate.onDestroy(viewHolder, adapterPosition)
    }

    final override fun getPageTitle(position: Int): CharSequence =
        items[position].run { if (itemTitleRes != 0) resources.getString(itemTitleRes) else itemTitle }

    final override fun getCount(): Int = items.size

    private fun callBackOf(oldItems: List<Item>, newItems: List<Item>): RecyclerPagerAdapter.OnDataSetChangedCallback<Item> =
        object : RecyclerPagerAdapter.OnDataSetChangedCallback<Item> {
            override fun getNewAdapterPositionOfItem(item: Item): Int = newItems.indexOf(item)
            override fun getOldItemAt(oldAdapterPosition: Int): Item = oldItems[oldAdapterPosition]
            override fun getNewItemAt(newAdapterPosition: Int): Item = newItems[newAdapterPosition]
            override fun areItemsTheSame(oldItem: Item, newItem: Item) = areItemAndContentsTheSame.invoke(oldItem, newItem)
        }
}
