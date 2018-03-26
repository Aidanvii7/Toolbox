package com.aidanvii.toolbox.adapterviews.recyclerview

import android.databinding.ViewDataBinding
import android.support.annotation.MainThread
import android.support.annotation.WorkerThread
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.aidanvii.toolbox.Action
import com.aidanvii.toolbox.adapterviews.databinding.BindableAdapter
import com.aidanvii.toolbox.adapterviews.databinding.BindableAdapterDelegate
import com.aidanvii.toolbox.adapterviews.databinding.BindableAdapterItem
import com.aidanvii.toolbox.adapterviews.databinding.BindingInflater
import com.aidanvii.toolbox.databinding.IntBindingConsumer
import com.aidanvii.toolbox.findIndex
import com.aidanvii.toolbox.leakingThis
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

/**
 * Implementation of [RecyclerView.Adapter] and [BindableAdapter] that can automatically bind a list of type [BindableAdapterItem].
 *
 * Subclassing this is purely optional, see [BindingRecyclerViewBinder] for usage with a subclass.
 */
@Suppress(leakingThis)
open class BindingRecyclerViewAdapter<Item : BindableAdapterItem>(
        builder: Builder<Item>
) : RecyclerView.Adapter<BindingRecyclerViewItemViewHolder<*, Item>>(), BindableAdapter<Item, BindingRecyclerViewItemViewHolder<*, Item>> {

    class Builder<Item : BindableAdapterItem>
    internal constructor(
            internal val delegate: BindableAdapterDelegate<Item, BindingRecyclerViewItemViewHolder<*, Item>>,
            internal val areItemsTheSame: (old: Item, new: Item) -> Boolean,
            internal val areContentsTheSame: (old: Item, new: Item) -> Boolean,
            internal val viewTypeHandler: BindableAdapter.ViewTypeHandler<Item>,
            internal val bindingInflater: BindingInflater
    )

    private var nextPropertyChangePayload: AdapterNotifier.ChangePayload? = null
    private var disposable: Disposable? = null

    private var _items = emptyList<Item>()
    override var items: List<Item>
        get() = _items
        set(newItems) {
            disposable?.dispose()
            disposable = Single.just(createDiffCallback(oldItems = _items, newItems = newItems))
                    .subscribeOn(Schedulers.computation())
                    .map { it.toChangePayload() }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeBy {
                        tempPreviousItems = _items
                        _items = it.allItems
                        it.diffResult.dispatchUpdatesTo(this)
                        tempPreviousItems = null
                        runAfterUpdate = runAfterUpdate?.let { afterDispatched ->
                            afterDispatched.invoke()
                            null
                        }
                    }
        }

    private val delegate = builder.delegate.also { it.bindableAdapter = this }
    private val areItemsTheSame = builder.areItemsTheSame
    private val areContentsTheSame = builder.areContentsTheSame
    internal var tempPreviousItems: List<Item>? = null
    internal var runAfterUpdate: Action? = null
    internal var attachedRecyclerView: RecyclerView? = null

    override val viewTypeHandler = builder.viewTypeHandler.also { it.initBindableAdapter(this) }
    override val bindingInflater = builder.bindingInflater
    override var itemBoundListener: IntBindingConsumer? = null


    final override fun getItem(position: Int): Item = super.getItem(position)

    final override fun getItemViewType(position: Int): Int = viewTypeHandler.getItemViewType(position)

    internal fun getItemPositionFromBindableItem(bindableItem: Any): Int? = items.findIndex { it.bindableItem === bindableItem }

    final override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingRecyclerViewItemViewHolder<*, Item> =
            delegate.onCreate(parent, viewType)

    final override fun createWith(bindingResourceId: Int, viewDataBinding: ViewDataBinding): BindingRecyclerViewItemViewHolder<*, Item> =
            BindingRecyclerViewItemViewHolder(
                    bindingResourceId = bindingResourceId,
                    viewDataBinding = viewDataBinding)

    final override fun onBindViewHolder(holder: BindingRecyclerViewItemViewHolder<*, Item>, position: Int, payloads: List<Any>) {
        nextPropertyChangePayload = getChangedProperties(payloads)
        onBindViewHolder(holder, position)
    }

    final override fun onInterceptOnBind(viewHolder: BindingRecyclerViewItemViewHolder<*, Item>, adapterPosition: Int): Boolean {
        return nextPropertyChangePayload?.let {
            onPartialBindViewHolder(it)
            nextPropertyChangePayload = null
            true
        } ?: false
    }

    final override fun onBindViewHolder(holder: BindingRecyclerViewItemViewHolder<*, Item>, position: Int) {
        delegate.onBind(holder, position)
    }

    final override fun onViewRecycled(holder: BindingRecyclerViewItemViewHolder<*, Item>) {
        delegate.onUnbind(holder, holder.adapterPosition)
    }

    final override fun getItemCount(): Int = items.count()

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        attachedRecyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        attachedRecyclerView = null
    }

    private fun getChangedProperties(payloads: List<Any>): AdapterNotifier.ChangePayload? =
            // changed payload may contain a single AdapterNotifier.ChangePayload or a list of custom payloads.
            // The AdapterNotifier.ChangePayload should contain the BR IDs that have changed.
            payloads.getOrNull(0)?.let {
                (it as? AdapterNotifier.ChangePayload) ?: throwCustomPayloadsNotSupported(it)
            }

    private fun throwCustomPayloadsNotSupported(unsupportedPayload: Any): Nothing =
            throw UnsupportedOperationException("Custom payload of type: ${unsupportedPayload::class.java} not supported.")

    private fun onPartialBindViewHolder(changePayload: AdapterNotifier.ChangePayload) {
        changePayload.apply {
            sender.adapterBindStart(this@BindingRecyclerViewAdapter)
            for (changedProperty in changedProperties) {
                sender.notifyAdapterPropertyChanged(changedProperty, false)
            }
            sender.adapterBindEnd(this@BindingRecyclerViewAdapter)
        }
    }

    @MainThread
    private fun createDiffCallback(oldItems: List<Item>, newItems: List<Item>): DiffCallback<Item> {
        return diffCallback(
                oldItems = oldItems,
                newItems = newItems,
                areItemsTheSame = areItemsTheSame,
                areContentsTheSame = areContentsTheSame)
    }

    @WorkerThread
    private fun DiffCallback<Item>.toChangePayload() =
            ChangePayload(
                    allItems = newItems,
                    diffResult = DiffUtil.calculateDiff(this)
            )

    private data class ChangePayload<out Item : BindableAdapterItem>(
            val allItems: List<Item>,
            val diffResult: DiffUtil.DiffResult
    )
}