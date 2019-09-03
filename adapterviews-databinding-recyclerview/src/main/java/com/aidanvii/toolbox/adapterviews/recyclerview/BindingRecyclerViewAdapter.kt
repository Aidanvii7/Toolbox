package com.aidanvii.toolbox.adapterviews.recyclerview

import androidx.databinding.ViewDataBinding
import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import com.aidanvii.toolbox.Action
import com.aidanvii.toolbox.adapterviews.databinding.BindableAdapter
import com.aidanvii.toolbox.adapterviews.databinding.BindableAdapterDelegate
import com.aidanvii.toolbox.adapterviews.databinding.BindableAdapterItem
import com.aidanvii.toolbox.adapterviews.databinding.BindingInflater
import com.aidanvii.toolbox.databinding.BindingAction
import com.aidanvii.toolbox.databinding.IntBindingConsumer
import com.aidanvii.toolbox.databinding.NotifiableObservable
import com.aidanvii.toolbox.delegates.coroutines.job.cancelOnReassign
import com.aidanvii.toolbox.findIndex
import com.aidanvii.toolbox.leakingThis
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

/**
 * Implementation of [RecyclerView.Adapter] and [BindableAdapter] that can automatically bind a list of type [BindableAdapterItem].
 *
 * Subclassing this is purely optional, see [BindingRecyclerViewBinder] for usage with a subclass.
 */
@Suppress(leakingThis)
open class BindingRecyclerViewAdapter<Item : BindableAdapterItem>(
    builder: Builder<Item>
) : RecyclerView.Adapter<BindingRecyclerViewItemViewHolder<*, Item>>(),
    BindableAdapter<Item, BindingRecyclerViewItemViewHolder<*, Item>> {

    companion object {
        // FIXME see Github issue #7
        internal var testModeEnabled = false
    }

    class Builder<Item : BindableAdapterItem> internal constructor(
        internal val delegate: BindableAdapterDelegate<Item, BindingRecyclerViewItemViewHolder<*, Item>>,
        internal val areItemsTheSame: (old: Item, new: Item) -> Boolean,
        internal val areContentsTheSame: (old: Item, new: Item) -> Boolean,
        internal val getChangedProperties: (old: Item, new: Item) -> IntArray?,
        internal val viewTypeHandler: BindableAdapter.ViewTypeHandler<Item>,
        internal val bindingInflater: BindingInflater,
        internal val itemBoundObservers: List<ItemBoundObserver<Item>>,
        internal val uiDispatcher: CoroutineDispatcher,
        internal val workerDispatcher: CoroutineDispatcher
    )

    private var nextPropertyChangePayload: AdapterNotifier.ChangePayload? = null
    private var nextPropertyChanges: IntArray? = null

    private var diffingJob by cancelOnReassign(null)

    private var _items = emptyList<Item>()
    override var items: List<Item>
        get() = _items
        set(newItems) {
            when {
                _items.isEmpty() && newItems.isNotEmpty() -> addAllImmediately(newItems)
                newItems.isEmpty() -> removeAllImmediately(newItems)
                newItems !== _items -> resolveDiffAsynchronously(newItems)
            }
        }

    private fun addAllImmediately(newItems: List<Item>) {
        _items = newItems
        notifySafely {
            notifyItemRangeInserted(0, newItems.size)
        }
        onItemsSet?.invoke()
    }

    private fun removeAllImmediately(newItems: List<Item>) {
        val oldItemsSize = _items.size
        _items = newItems
        notifySafely {
            notifyItemRangeRemoved(0, oldItemsSize)
        }
        onItemsSet?.invoke()
    }

    private inline fun notifySafely(action: Action) {
        if(testModeEnabled) {
            try {
                action()
            } catch (e: NullPointerException) {
                print(e.message)
            }
        } else action()
    }

    private fun resolveDiffAsynchronously(newItems: List<Item>) {
        diffingJob = GlobalScope.launch(uiDispatcher) {
            async(coroutineContext + workerDispatcher) {
                createDiffCallback(
                    oldItems = _items,
                    newItems = newItems
                ).toChangePayload()
            }.let { deferredChangePayload ->
                deferredChangePayload.await().let { changePayload ->
                    _items = changePayload.allItems
                    notifySafely {
                        changePayload.diffResult.dispatchUpdatesTo(this@BindingRecyclerViewAdapter)
                    }
                    onItemsSet?.invoke()
                }
            }
        }
    }

    private val delegate = builder.delegate.also { it.bindableAdapter = this }
    private val areItemsTheSame = builder.areItemsTheSame
    private val areContentsTheSame = builder.areContentsTheSame
    private val getChangedProperties = builder.getChangedProperties
    private val uiDispatcher = builder.uiDispatcher
    private val workerDispatcher = builder.workerDispatcher
    private val itemBoundObservers = builder.itemBoundObservers
    private var attachedRecyclerView: RecyclerView? = null

    override val viewTypeHandler = builder.viewTypeHandler.also { it.initBindableAdapter(this) }
    override val bindingInflater = builder.bindingInflater
    override var itemBoundListener: IntBindingConsumer? = null
    internal var onItemsSet: BindingAction? = null

    final override fun getItem(position: Int) = super.getItem(position)

    final override fun getItemViewType(position: Int): Int = viewTypeHandler.getItemViewType(position)

    internal fun getItemPositionFromBindableItem(bindableItem: Any): Int? =
        items.findIndex { it.lazyBindableItem.value === bindableItem }

    final override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BindingRecyclerViewItemViewHolder<*, Item> = delegate.onCreate(parent, viewType)

    final override fun createWith(
        bindingResourceId: Int,
        viewDataBinding: ViewDataBinding
    ) = BindingRecyclerViewItemViewHolder(
        bindingResourceId = bindingResourceId,
        viewDataBinding = viewDataBinding,
        itemBoundObservers = itemBoundObservers
    )

    final override fun onBindViewHolder(
        holder: BindingRecyclerViewItemViewHolder<*, Item>,
        position: Int,
        payloads: List<Any>
    ) {
        nextPropertyChangePayload = getChangedProperties(payloads)?.first
        nextPropertyChanges = getChangedProperties(payloads)?.second
        onBindViewHolder(holder, position)
    }

    final override fun onInterceptOnBind(
        viewHolder: BindingRecyclerViewItemViewHolder<*, Item>,
        adapterPosition: Int,
        observable: NotifiableObservable?
    ): Boolean {
        val nextPropertyChangePayload = nextPropertyChangePayload
        val nextPropertyChanges = nextPropertyChanges
        return when {
            nextPropertyChangePayload != null -> true.also {
                onPartialBindViewHolder(
                    nextPropertyChangePayload
                )
            }
            nextPropertyChanges != null && observable != null -> true.also {
                onPartialBindViewHolder(
                    viewHolder,
                    observable,
                    nextPropertyChanges
                )
            }
            else -> false
        }.also {
            this.nextPropertyChangePayload = null
            this.nextPropertyChanges = null
        }
    }

    final override fun onBindViewHolder(
        holder: BindingRecyclerViewItemViewHolder<*, Item>,
        position: Int
    ) {
        holder.currentAdapter = this
        delegate.onBind(holder, position, attachedRecyclerView)
    }

    final override fun onViewRecycled(holder: BindingRecyclerViewItemViewHolder<*, Item>) {
        delegate.onUnbind(holder, holder.adapterPosition, attachedRecyclerView)
        holder.currentAdapter = null
    }

    final override fun getItemCount(): Int = items.count()

    internal fun contains(item: Item): Boolean =
        _items.any { areItemsTheSame(it, item) && areContentsTheSame(it, item) }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        attachedRecyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        attachedRecyclerView = null
    }

    private fun getChangedProperties(payloads: List<Any>): Pair<AdapterNotifier.ChangePayload?, IntArray?>? =
        payloads.getOrNull(0)?.let { payload ->
            when (payload) {
                is AdapterNotifier.ChangePayload -> Pair(payload, null)
                is IntArray -> Pair(null, payload)
                else -> throwCustomPayloadsNotSupported(payload)
            }
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

    private fun onPartialBindViewHolder(
        viewHolder: BindingRecyclerViewItemViewHolder<*, Item>,
        observable: NotifiableObservable,
        changedProperties: IntArray
    ) {
        viewHolder.apply { viewDataBinding.setVariable(bindingResourceId, observable) }
        for (changedProperty in changedProperties) {
            observable.notifyPropertyChanged(changedProperty)
        }
    }

    @MainThread
    private fun createDiffCallback(oldItems: List<Item>, newItems: List<Item>): DiffCallback<Item> =
        diffCallback(
            oldItems = oldItems,
            newItems = newItems,
            areItemsTheSame = areItemsTheSame,
            areContentsTheSame = areContentsTheSame,
            getChangedProperties = getChangedProperties
        )

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
