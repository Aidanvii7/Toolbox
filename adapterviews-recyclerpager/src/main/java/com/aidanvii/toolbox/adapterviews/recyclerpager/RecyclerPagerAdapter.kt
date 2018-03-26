package com.aidanvii.toolbox.adapterviews.recyclerpager

import android.os.Parcelable
import android.support.annotation.RestrictTo
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import com.aidanvii.toolbox.adapterviews.recyclerpager.RecyclerPagerAdapter.ViewHolder
import com.aidanvii.toolbox.iterator
import com.aidanvii.toolbox.minusAssign
import com.aidanvii.toolbox.plusAssign
import com.aidanvii.toolbox.unchecked

/**
 * A [PagerAdapter] that recycles Views similar to [RecyclerView].
 * @param Item The type of item that will back the adapter, e.g. a [List] of [Item]
 * @param itemPoolContainer Contains the [ItemPool] in which the [ViewHolder]s will be stored. This may be shared across multiple [RecyclerPagerAdapter]s.
 */
abstract class RecyclerPagerAdapter<Item, ViewHolder : RecyclerPagerAdapter.ViewHolder>(
        itemPoolContainer: ItemPoolContainer<ViewHolder> = ItemPoolContainer()
) : PagerAdapter() {

    private val viewHolderWrapperPool = itemPoolContainer.itemPool
    private val stagedForViewTransaction = mutableListOf<PageItem<ViewHolder>>()
    private val stagedForUnbind = mutableListOf<PageItem<ViewHolder>>()
    private val stagedForBind = mutableListOf<PageItem<ViewHolder>>()
    private val stagedForDestroy = mutableListOf<PageItem<ViewHolder>>()
    private val activeViewHolders = SparseArray<ViewHolderWrapper<ViewHolder>>()
    private var commitChangesOnFinish = false
    private var primaryPageItem: PageItem<ViewHolder>? = null
    private var dataSetChangeResolver: DataSetChangeResolver<Item, ViewHolder>? = null

    class ItemPoolContainer<ViewHolder : RecyclerPagerAdapter.ViewHolder> {
        internal val itemPool = ItemPool<ViewHolderWrapper<ViewHolder>>()

        fun setMaxRecycled(itemType: Int, max: Int) {
            itemPool.setMaxRecycled(itemType, max)
        }
    }

    /**
     * Callback used to resolve changes to the data-set backing the [ViewPager].
     *
     * Implementations should contain a before and after snapshot of the data.
     *
     * This callback should be passed to [notifyDataSetChanged]
     *
     * example implementation:
     *
     * ```
     * class MyOnDataSetChangedCallback(
     *          val oldData: List<Item>,
     *          val newData: List<Item>
     * ) : OnDataSetChangedCallback<Item>{
     *      override fun getNewAdapterPositionOfItem(item: Item) = oldData.indexOf(item)
     *      override fun getOldItemAt(oldAdapterPosition: Int) = oldData[oldAdapterPosition]
     *      override fun getNewItemAt(newAdapterPosition: Int) = newData[newAdapterPosition]
     * }
     * ```
     */
    interface OnDataSetChangedCallback<Item> {
        /**
         * Return the position of the given item in the new data-set, or negative number if absent.
         */
        fun getNewAdapterPositionOfItem(item: Item): Int
        fun getOldItemAt(oldAdapterPosition: Int): Item
        fun getNewItemAt(newAdapterPosition: Int): Item
        fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean = oldItem == newItem
    }

    /**
     * A [ViewHolder] is a recyclable [View] container.
     *
     * Implementations should implement this or subclass [BasePageViewHolder] (for Java impls).
     *
     * Instances of [ViewHolder] implementations should be provided by [onCreateViewHolder] in
     * custom [RecyclerPagerAdapter] implementations.
     */
    interface ViewHolder {
        val view: View

        /**
         * By the time this returns, the [view] should be added to the given [container].
         *
         * Not doing so may cause inconsistencies in the [ViewPager]
         */
        fun addViewToContainer(container: ViewGroup) {
            container += view
        }

        /**
         * By the time this returns, the [view] should be removed from the given [container]
         *
         * Not doing so may cause inconsistencies in the [ViewPager]
         */
        fun removeViewFromContainer(container: ViewGroup) {
            container -= view
        }

        fun onDestroyed() {}
    }

    /**
     * Return the view type of the item at the adapterPosition for the purposes of view recycling.
     *
     * The default implementation of this method returns 0, making the assumption of
     * a single view type for the adapter. Consider using id resources to uniquely identify item view types.

     * @param adapterPosition adapterPosition to query
     * @return integer value identifying the type of the view needed to represent the item at the adapterPosition.
     */
    open fun getItemViewType(adapterPosition: Int): Int = 0

    /**
     * Called when the [RecyclerPagerAdapter] needs a new [ViewHolder] of the given type to represent an item.
     *
     * @param viewType  The view type of the new View.
     * @param container The container to attach the View.
     * @return A new ViewHolder that holds a layout ID for a layout resource.
     * *
     * @see getItemViewType
     */
    protected abstract fun onCreateViewHolder(viewType: Int, position: Int, container: ViewGroup): ViewHolder

    /**
     * Called when the [RecyclerPagerAdapter] wants to recycle a [ViewHolder].
     * The default implementation returns true meaning that ViewHolders will always be recycled.
     * @param viewHolder the ViewHolder that the adapter is attempting to recycle
     * @param viewType the itemType of the [viewHolder] that the adapter is attempting to recycle
     * @return true to recycle, false to destroy
     */
    protected open fun shouldRecycleViewHolder(viewHolder: ViewHolder, viewType: Int) = true

    /**
     * Called when the [RecyclerPagerAdapter] is binding a [ViewHolder] at the [adapterPosition]
     * @param viewHolder the [ViewHolder] being bound
     * @param adapterPosition the position in the adapter's data set to which the [ViewHolder] is being bound
     * i.e. moved from left to right in a single swipe.
     */
    protected open fun onBindViewHolder(viewHolder: ViewHolder, adapterPosition: Int) {}

    /**
     * Called when the [RecyclerPagerAdapter] is unbinding a [ViewHolder] at the [adapterPosition]
     * @param viewHolder the [ViewHolder] being unbound
     * @param adapterPosition the position in the adapter's data set to which the [ViewHolder] is being unbound
     * i.e. moved from left to right in a single swipe.
     */
    protected open fun onUnbindViewHolder(viewHolder: ViewHolder, adapterPosition: Int) {}

    /**
     * Called when the [RecyclerPagerAdapter] is destroying a [ViewHolder] at the [adapterPosition]
     * @param viewHolder the [ViewHolder] being destroyed
     * @param adapterPosition the position in the adapter's data set to which the [ViewHolder] is being destroyed
     */
    protected open fun onDestroyViewHolder(viewHolder: ViewHolder, adapterPosition: Int) {}

    /**
     * Called by the [RecyclerPagerAdapter] to determine whether all active and pooled [ViewHolder]s should be destroyed.
     * This will result in a call to [ViewHolder.onDestroyed].
     * @return
     */
    protected open fun shouldDestroyViewHoldersOnSaveState() = false

    /**
     * Called when a [ViewHolder] is centered within the [ViewPager]
     */
    protected open fun setPrimaryViewHolder(container: ViewGroup, position: Int, viewHolder: ViewHolder) {}

    /**
     * This should be called by the application if the data backing this adapter has changed and associated views should update.
     *
     * @param callback The [OnDataSetChangedCallback] implementation used to determine what has changed.
     */
    fun notifyDataSetChanged(callback: OnDataSetChangedCallback<Item>) {
        this.dataSetChangeResolver = DataSetChangeResolver(callback, maxAdapterPosition)
        super.notifyDataSetChanged()
        this.dataSetChangeResolver = null
    }

    @Deprecated(
            message = """
                Use `notifyDataSetChanged(callback: OnDataSetChangedCallback<Item>)` instead,
                this will allow incremental changes to the ViewPager.
                """,
            replaceWith = ReplaceWith("""
                notifyDataSetChanged(object : OnDataSetChangedCallback<T> {
                    override fun getNewAdapterPositionOfItem(item: T) = newData.indexOf(item)
                    override fun getOldItemAt(oldAdapterPosition: Int) = oldData[oldAdapterPosition]
                    override fun getNewItemAt(newAdapterPosition: Int) = newData[newAdapterPosition]
                })
            """),
            level = DeprecationLevel.ERROR
    )
    final override fun notifyDataSetChanged() {
        throw UnsupportedOperationException("Instead use `notifyDataSetChanged(callback: OnDataSetChangedCallback<Item>)`")
    }

    fun getViewHolderAtPosition(position: Int): ViewHolder? =
            activeViewHolders.get(position)?.viewHolder

    fun getSizeForType(viewType: Int): Int = viewHolderWrapperPool.getSizeForType(viewType)

    val poolSize: Int get() = viewHolderWrapperPool.size

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    final override fun instantiateItem(container: ViewGroup, adapterPosition: Int): Any {
        commitChangesOnFinish(true)
        return PageItem(
                adapter = this,
                viewType = getItemViewType(adapterPosition),
                adapterPosition = adapterPosition
        ).also { pageItem ->
            pageItem.viewTransaction = ViewTransaction.ADD
            stagedForViewTransaction.add(pageItem)
            stagedForBind.add(pageItem)
        }
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    final override fun destroyItem(container: ViewGroup, adapterPosition: Int, uncastPageItem: Any) {
        commitChangesOnFinish(true)
        val pageItem = asPageItem(uncastPageItem)
        val viewHolderWrapper = pageItem.viewHolderWrapper
        if (shouldRecycleViewHolder(viewHolderWrapper.viewHolder, viewHolderWrapper.itemType)) {
            stagedForUnbind.add(pageItem)
            pageItem.viewTransaction = ViewTransaction.REMOVE
            stagedForViewTransaction.add(pageItem)
        } else {
            stagedForDestroy.add(pageItem)
            pageItem.viewTransaction = ViewTransaction.DESTROY
            stagedForViewTransaction.add(pageItem)
        }
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    final override fun isViewFromObject(view: View, uncastPageItem: Any): Boolean =
            asPageItem(uncastPageItem).viewHolderWrapper.viewHolder.view === view

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    final override fun finishUpdate(container: ViewGroup) {
        if (commitChangesOnFinish) {
            commitChangesOnFinish(false)
            commitChanges(container)
        }
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    final override fun getItemPosition(uncastPageItem: Any): Int {
        if (dataSetChangeResolver == null) {
            return PagerAdapter.POSITION_NONE
        } else {
            val pageItem = asPageItem(uncastPageItem)
            return dataSetChangeResolver!!.resolvePageItemPosition(pageItem)
        }
    }

    @Suppress(unchecked)
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    final override fun setPrimaryItem(container: ViewGroup, position: Int, uncastPageItem: Any) {
        (uncastPageItem as? PageItem<ViewHolder>)?.let { pageItem ->
            setPrimaryPageItem(pageItem)
        }
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    final override fun saveState(): Parcelable? {
        if (shouldDestroyViewHoldersOnSaveState()) {
            clearPooledViewHolders()
            clearActiveViewHolders()
        }
        return null
    }

    private val maxAdapterPosition: Int
        get() = count - 1

    private fun clearPooledViewHolders() {
        viewHolderWrapperPool.clear()
    }

    @Suppress(unchecked)
    private fun asPageItem(uncastPageItem: Any) = uncastPageItem as PageItem<ViewHolder>

    private fun clearActiveViewHolders() {
        activeViewHolders.iterator().forEach { it.destroy() }
        activeViewHolders.clear()
    }

    private fun commitChangesOnFinish(commitChangesOnFinish: Boolean) {
        this.commitChangesOnFinish = commitChangesOnFinish
    }

    private fun commitChanges(container: ViewGroup) {
        preProcessStagedForUnbind()
        preProcessStagedForDestroy()
        preProcessStagedForBind(container)
        runStagedViewTransactions(container)
        clearStaged()
        notifyPrimaryViewHolder(container)
    }

    private fun preProcessStagedForUnbind() {
        stagedForUnbind.forEach { pageItem ->
            onUnbindViewHolder(pageItem.viewHolderWrapper.viewHolder, pageItem.adapterPosition)
            activeViewHolders.remove(pageItem.adapterPosition)
            viewHolderWrapperPool.putItem(pageItem.viewHolderWrapper)
        }
    }

    private fun preProcessStagedForDestroy() {
        stagedForDestroy.forEach { pageItem ->
            onDestroyViewHolder(pageItem.viewHolderWrapper.viewHolder, pageItem.adapterPosition)
            activeViewHolders.remove(pageItem.adapterPosition)
        }
    }

    private fun preProcessStagedForBind(container: ViewGroup) {
        stagedForBind.forEach { pageItem ->
            val viewHolderWrapper = createOrGetRecycledViewHolderForType(pageItem, container)
            pageItem.viewHolderWrapper = viewHolderWrapper
            onBindViewHolder(pageItem.viewHolderWrapper.viewHolder, pageItem.adapterPosition)
            activeViewHolders.put(pageItem.adapterPosition, viewHolderWrapper)
        }
    }

    private fun runStagedViewTransactions(container: ViewGroup) {
        stagedForViewTransaction.forEach { it.runPendingTransaction(container) }
    }

    private fun clearStaged() {
        stagedForUnbind.clear()
        stagedForBind.clear()
        stagedForDestroy.clear()
        stagedForViewTransaction.clear()
    }

    private fun notifyPrimaryViewHolder(container: ViewGroup) {
        primaryPageItem?.apply {
            setPrimaryViewHolder(container, adapterPosition, viewHolderWrapper.viewHolder)
        }
        primaryPageItem = null
    }

    private fun setPrimaryPageItem(pageItem: PageItem<ViewHolder>?) {
        this.primaryPageItem = pageItem
    }

    private fun createOrGetRecycledViewHolderForType(
            pageItem: PageItem<ViewHolder>,
            container: ViewGroup
    ): ViewHolderWrapper<ViewHolder> {
        return viewHolderWrapperPool.popItem(pageItem.viewType) {
            val viewHolder = onCreateViewHolder(pageItem.viewType, pageItem.adapterPosition, container)
            ViewHolderWrapper(viewHolder, pageItem.viewType)
        }
    }
}
