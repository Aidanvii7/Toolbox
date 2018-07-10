package com.aidanvii.toolbox.paging

import android.support.annotation.IntRange
import com.aidanvii.toolbox.Action
import com.aidanvii.toolbox.Consumer
import com.aidanvii.toolbox.checkAboveMin
import com.aidanvii.toolbox.checkInBounds
import com.aidanvii.toolbox.paging.PagedList.Companion.UNDEFINED
import com.aidanvii.toolbox.paging.PagedList.DataSource
import com.aidanvii.toolbox.paging.PagedList.DataSource.Page
import com.aidanvii.toolbox.paging.PagedList.DataSource.Page.Builder
import com.aidanvii.toolbox.paging.PagedList.ElementState
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.exceptions.OnErrorNotImplementedException
import io.reactivex.subjects.BehaviorSubject

private val onErrorStub: Consumer<Throwable> = { throw OnErrorNotImplementedException(it) }

/**
 * A [PagedList] is a form of list that lazily loads its data in chunks ([DataSource.Page]) from a [DataSource].
 *
 * Items can be retrieved with [get] or simply accessed via [loadAround] - both may trigger a call to [DataSource.loadPage].
 *
 * To listen for changes, you may observe either [distinctObservableList], [observableListNonNull] or [observableChangePayload].
 *
 * Typical usage would be to:
 *
 * 1st - Subscribe to either [distinctObservableList], [observableListNonNull] or [observableChangePayload]
 *
 * 2nd - Use [DiffUtil.calculateDiff] on a background thread to determined the changes
 *
 * 3rd - Update the [RecyclerView.Adapter] with the newly observed list
 *
 * 4th - Apply the calculated [DiffUtil.DiffResult] to the [RecyclerView.Adapter] (immediately after last step)
 *
 * 5th - Forward calls from the [RecyclerView.Adapter.onBindViewHolder] to [PagedList.loadAround] with the adapter position to trigger pagination calls to the provided [DataSource]
 *
 * @param dataSource The [DataSource] implementation that will provide the data.
 * @param pageSize The length of data that will determine the [DataSource.Page.Builder.pageSize]. May by [UNDEFINED] if unknown.
 * @param prefetchDistance Determines the range of elements to scan when [get] or [loadAround] is called. If elements in the scanned range are [ElementState.EMPTY] or [ElementState.DIRTY], [DataSource.loadPage] will be called at least once.
 * @param loadInitialPages Optional [IntArray] of page indexes. Non-zero indexing (first page is 1).
 * @param publishChangesOnInit Determines whether any subscribers to [distinctObservableList], [observableListNonNull] or [observableChangePayload] will be notified upon subscription when no changes to the data have been made (the initial empty state).
 * @param synchroniseAccess Determines whether access to the internal data is synchronised for the case where [get] or [loadAround] may be called from different threads.
 * @param onError Errors when loading pages will be passed here. The default implementation will throw a [OnErrorNotImplementedException].
 * @param pageLoader The [PageLoader] that coordinates calls to the provided [DataSource].
 */
class PagedList<T : Any>(
    dataSource: DataSource<T>,
    pageSize: Int = UNDEFINED,
    val prefetchDistance: Int = 0,
    loadInitialPages: IntArray? = null,
    publishChangesOnInit: Boolean = true,
    synchroniseAccess: Boolean = false,
    private val onError: Consumer<Throwable> = onErrorStub,
    private val pageLoader: PageLoader<T> = PageLoader.Default()
) : Disposable by pageLoader {

    companion object {
        const val UNDEFINED = 0
    }

    /**
     * A [DataSource] is responsible for supplying the [PagedList] with data in the form of a [Page].
     *
     * The [PagedList] will call [loadPage] when it requires a new [Page].
     *
     */
    interface DataSource<T> {

        /**
         * Represents the count of the data set that will be paged. If unknown this may be zero.
         * When a new value is emitted, the [PagedList] will resize itself.
         */
        val dataCount: Observable<Int>

        /**
         * Called when the [PagedList] requires a new page.
         */
        fun loadPage(pageBuilder: Page.Builder<T>): Maybe<Page<T>>

        /**
         * Provide am 'empty' value for the given position.
         *
         * This is called to fill 'not-yet-loaded' elements with some default instance, defaults to null.
         */
        fun emptyAt(index: Int = UNDEFINED): T? = null

        /**
         * Represents a page in the [PagedList]. The [pageData] size cannot exceed the given [Builder.pageSize]
         */
        class Page<out T> private constructor(val pageNumber: Int, val pageData: List<T>) {
            /**
             * Used to build a single [Page]. Call [build]
             */
            class Builder<T> internal constructor(val pageNumber: Int, val pageSize: Int) {
                var used = false
                fun build(pageData: List<T>): Page<T> {
                    if (used) throw IllegalStateException("Cannot build the same page twice!")
                    used = true
                    if (pageSize != UNDEFINED) {
                        pageData.size.checkInBounds(0, pageSize, "given pageData has wrong size, should be between 0 and $pageSize")
                    }
                    return Page(pageNumber, pageData)
                }
            }
        }
    }

    /**
     * Represents the current state of an element in the [PagedList]
     */
    enum class ElementState {
        /**
         * The element does not have a value, and no requests for it are in-flight.
         */
        EMPTY,
        /**
         * The element does not have a value, though a request for it is in-flight.
         */
        LOADING,
        /**
         * The element has a value, and no requests for it are in-flight.
         */
        LOADED,
        /**
         * The element has a value, and no requests for it are in-flight,
         * but is considered 'dirty' and treated like [EMPTY], where a request to update it can occur
         */
        DIRTY,
        /**
         * The element has a value, but a request to update it is in-flight.
         */
        REFRESHING
    }

    /**
     * Represents both a snapshot of and changes to the [PagedList], provided by [observableChangePayload].
     *
     * If [observableChangePayload] will be used, the provided type [T] should implement [equals] and [hashCode]
     * as this will determine the [addedItems] and [removedItems] items.
     *
     * @param allItems a snapshot of the entire [PagedList] based on the last modification to the [PagedList]
     * @param removedItems a snapshot of the items that have been removed from the [PagedList] based on the last modification to the [PagedList]
     * @param addedItems a snapshot of the items that have been added to the [PagedList] based on the last modification to the [PagedList]
     */
    data class ChangePayload<out T>(
        val allItems: List<T> = emptyList(),
        val removedItems: Set<T> = emptySet(),
        val addedItems: Set<T> = emptySet()
    )

    private var lastAccessedIndex = UNDEFINED
    private var lastLoadedPageNumber = 1

    private val pageSizeUndefined: Boolean
    private val changeSubject = BehaviorSubject.create<List<T?>>()
    private val elements = Elements(publishChangesOnInit, synchroniseAccess, onError, dataSource, changeSubject)

    /**
     * The provided page size, or 1 if [UNDEFINED] is provided.
     */
    val pageSize: Int

    init {
        pageSize.checkAboveMin(UNDEFINED, "pageSize must be a positive  number, or PagedList.UNDEFINED")
        pageSizeUndefined = pageSize == UNDEFINED
        this.pageSize = if (pageSizeUndefined) 1 else pageSize
        pageLoader.dataSource = dataSource
        elements.runSynchronised { loadInitialPages?.forEach { loadPage(it) } }
    }

    /**
     * The current size of the list, including also the elements that have the state [ElementState.EMPTY].
     */
    val size get() = elements.runSynchronised { elements.size }

    /**
     * A distinct Observable List of the paged data. List elements may be null.
     */
    val distinctObservableList get(): Observable<List<T?>> = changeSubject.distinctUntilChanged().doOnDispose { dispose() }

    /**
     * An Observable List of the paged data. List elements may be null.
     */
    val observableList get(): Observable<List<T?>> = changeSubject.doOnDispose { dispose() }

    /**
     * Convenience version of [distinctObservableList] that provides non-null elements.
     *
     * This only works if the provided [DataSource] always returns non-null values for [DataSource.emptyAt],
     * otherwise an [KotlinNullPointerException] will be thrown.
     */
    val observableListNonNull get(): Observable<List<T>> = distinctObservableList.map { it.map { it!! } }

    /**
     * Version of [distinctObservableList] that provides data in the form of a [ChangePayload].
     *
     * As null elements are filtered, it is recommended that the provided [DataSource] always return a non-null value for [DataSource.emptyAt]
     */
    val observableChangePayload
        get(): Observable<ChangePayload<T>> = distinctObservableList
            .map { it.filterNotNull() }
            .scan(ChangePayload<T>()) { lastPayload, newItems ->
                ChangePayload(
                    allItems = newItems,
                    removedItems = lastPayload.allItems subtract newItems,
                    addedItems = newItems subtract lastPayload.allItems
                )
            }

    val lastIndex: Int get() = elements.runSynchronised { lastIndex }

    /**
     * Gets a snapshot in the form of a standard [List] of the current elements.
     */
    fun asList(): List<T?> = elements.runSynchronised { asList() }

    /**
     * Resets the state of the elements in the range from [startIndex] to [endIndex] to [ElementState.EMPTY],
     * and resets the element value to an 'empty' element from [DataSource.emptyAt] from the provided [DataSource].
     *
     * @param startIndex the starting zero-based element index
     * @param endIndex the ending zero-based element index
     * @param refreshElementsInRange optional param that will force pages within the given range to refresh.
     */
    fun invalidateAsEmpty(
        startIndex: Int = 0,
        endIndex: Int = lastIndex,
        refreshElementsInRange: Boolean = false
    ) {
        invalidate(refreshElementsInRange) {
            elements.invalidateAsEmpty(startIndex, endIndex)
        }
    }

    /**
     * Resets the state of the elements in the range from [startIndex] to [endIndex] that have the state [ElementState.LOADED] to [ElementState.DIRTY].
     *
     * @param startIndex the starting zero-based element index
     * @param endIndex the ending zero-based element index
     * @param refreshElementsInRange optional param that will force pages within the given range to refresh.
     */
    fun invalidateLoadedAsDirty(
        startIndex: Int = 0,
        endIndex: Int = lastIndex,
        refreshElementsInRange: Boolean = false
    ) {
        invalidate(refreshElementsInRange) {
            elements.invalidateLoadedAsDirty(startIndex, endIndex)
        }
    }

    /**
     * If the element at the given index, or any elements in range of the provided [prefetchDistance]
     * is considered 'empty' or 'dirty' (see [ElementState.EMPTY], [ElementState.DIRTY]),
     * It will trigger one or more pagination calls to [DataSource.loadPage] for those elements.
     * @param index the element to 'access'
     * @param growIfNecessary optional param that will cause the list to grow to the correct size if [index] exceeds [size] - 1
     */
    fun loadAround(@IntRange(from = 0) index: Int, growIfNecessary: Boolean = false) {
        elements.runSynchronised {
            if (growIfNecessary) {
                growIfNecessary(index + 1)
            } else if (!indexInBounds(index)) {
                throw IndexOutOfBoundsException("index: $index is out of bounds. current size is ${size}")
            }
            access(index)
        }
    }

    /**
     * Similar to [loadAround] but works with pageNumber indexes as opposed to element indexes.
     * @param pageNumber a non-zero based index for the pageNumber (the first pageNumber is not 0, it is 1)
     * @param growIfNecessary optional param that will cause the list to grow to the correct size if [pageNumber] exceeds [size] + [pageSize] - 1
     */
    fun loadAroundPage(pageNumber: Int, growIfNecessary: Boolean = false) {
        elements.runSynchronised {
            startingIndexOfPage(pageNumber).let { pageStartingIndex ->
                if (growIfNecessary) {
                    elements.growIfNecessary(pageStartingIndex + pageSize)
                } else if (!elements.indexInBounds(startingIndexOfPage(pageNumber))) {
                    throw IndexOutOfBoundsException(
                        "pageNumber: $pageNumber is out of bounds. max pageNumber is ${currentPageForIndex(
                            pageStartingIndex
                        )}"
                    )
                }
                access(pageStartingIndex)
            }
        }
    }

    /**
     * Retrieves the element at the given index which may be null,
     * or a default 'empty' element from [DataSource.emptyAt] from the provided [DataSource].
     *
     * Also calls [loadAround].
     */
    fun get(index: Int): T? {
        return elements.runSynchronised {
            loadAround(index, growIfNecessary = false)
            peek(index)
        }
    }

    /**
     * Retrieves the element at the given index which may be null,
     * or a default 'empty' element from [DataSource.emptyAt] from the provided [DataSource].
     *
     * Unlike [get], this will not call [loadAround], so no pagination calls will be triggered.
     */
    fun peek(index: Int): T? = elements.runSynchronised { get(index) }

    /**
     * Gets the [ElementState] for the given index.
     */
    fun elementStateFor(index: Int): ElementState = elements.runSynchronised { elementStateFor(index) }

    /**
     * If any elements within the given page are [ElementState.EMPTY] or [ElementState.DIRTY],
     * [DataSource.loadPage] will be called on the provided [DataSource].
     *
     * @param page a non-zero based index for the page (the first page is not 0, it is 1)
     */
    fun loadPage(pageNumber: Int = lastLoadedPageNumber): Maybe<Page<T>> {
        pageNumber.checkAboveMin(0, "pageNumber must be greater than 0")
        elements.runSynchronised {
            val pageStartIndex = startingIndexOfPage(pageNumber)
            val pageEndIndex = endIndexOfPageFromStartingIndex(pageStartIndex)
            for (index in pageStartIndex..pageEndIndex) {
                if (isEmpty(index)) setLoading(index)
                else setRefreshingIfDirty(index)
            }
        }
        lastLoadedPageNumber = pageNumber
        return pageLoader.loadPage(
            pageBuilder = DataSource.Page.Builder(pageNumber, if (pageSizeUndefined) UNDEFINED else pageSize),
            onDisposeOrErrorOrComplete = { resetPageToEmpty(pageNumber) },
            onError = onError,
            onSuccess = this::populatePage
        )
    }

    private fun access(index: Int) {
        for (pageNumber in pageNumbersForIndex(index)) {
            pageLoader.accessed(index)
            if (shouldLoadPage(pageNumber)) {
                loadPage(pageNumber)
            }
        }
        lastAccessedIndex = index
    }

    private fun shouldLoadPage(pageNumber: Int): Boolean {
        val pageStartIndex = startingIndexOfPage(pageNumber)
        val pageEndIndex = endIndexOfPageFromStartingIndex(pageStartIndex)
        return (pageStartIndex..pageEndIndex).any { elements.isEmptyOrDirty(it) }
    }

    private fun pageNumbersForIndex(index: Int): IntArray {
        return when {
            accessIsIncrementallySequential(index) -> getIncrementallySequentialPageIfNecessary(index).let { indexToPageNumberList(it) }
            accessIsDecrementallySequential(index) -> getDecrementallySequentialPageIfNecessary(index).let { indexToPageNumberList(it) }
            else -> getPages(fromPage = firstPageInRangeForIndex(index), toPage = lastPageInRangeForIndex(index))
        }
    }

    private fun accessIsIncrementallySequential(index: Int) = (lastAccessedIndex != UNDEFINED) && lastAccessedIndex == index - 1

    private fun accessIsDecrementallySequential(index: Int) = (lastAccessedIndex != UNDEFINED) && lastAccessedIndex == index + 1

    private fun getIncrementallySequentialPageIfNecessary(index: Int): Int =
        clamped(index + prefetchDistance).let {
            if (sequentialAccessCrossesPageBoundary(
                    indexWithPrefetchOffset = it,
                    previousIndexWithPrefetchOffset = clamped(lastAccessedIndex + prefetchDistance)
                )
            ) it else UNDEFINED
        }

    private fun getDecrementallySequentialPageIfNecessary(index: Int): Int =
        clamped(index - prefetchDistance).let {
            if (sequentialAccessCrossesPageBoundary(
                    indexWithPrefetchOffset = it,
                    previousIndexWithPrefetchOffset = clamped(lastAccessedIndex - prefetchDistance)
                )
            ) it else UNDEFINED
        }

    private fun indexToPageNumberList(index: Int) = if (index != UNDEFINED) {
        currentPageForIndex(index).let { pageNumber ->
            if (pageNumber != UNDEFINED) intArrayOf(pageNumber) else intArrayOf()
        }
    } else intArrayOf()

    private fun sequentialAccessCrossesPageBoundary(
        indexWithPrefetchOffset: Int,
        previousIndexWithPrefetchOffset: Int
    ) = currentPageForIndex(indexWithPrefetchOffset) != currentPageForIndex(previousIndexWithPrefetchOffset)

    private fun getPages(fromPage: Int, toPage: Int): IntArray {
        return if (fromPage <= toPage) {
            ((toPage - fromPage) + 1).let { size ->
                IntArray(size).also { pages ->
                    var currentPage = fromPage
                    for (index in 0 until size) pages[index] = currentPage++
                }
            }
        } else intArrayOf() // no reverse support
    }

    private fun currentPageForIndex(index: Int): Int = (index / pageSize) + 1

    private fun firstPageInRangeForIndex(index: Int): Int = currentPageForIndex(Math.max(index - prefetchDistance, 0))

    private fun lastPageInRangeForIndex(index: Int): Int = currentPageForIndex(Math.min(index + prefetchDistance, elements.size - 1))

    private fun startingIndexOfPage(pageNumber: Int): Int = (pageSize * (pageNumber - 1))

    private fun endIndexOfPageFromStartingIndex(pageStartingIndex: Int) = clamped(pageStartingIndex + pageSize - 1)

    private fun clamped(index: Int) = index.let { if (it >= elements.size) elements.size - 1 else if (it < 0) 0 else it }

    private fun populatePage(page: DataSource.Page<T>) {
        page.apply {
            pageNumber.checkAboveMin(0, "pageNumber must be greater than 0")
            startingIndexOfPage(pageNumber).let { pageStartingIndex ->
                elements.runSynchronised {
                    (pageStartingIndex + pageData.size).let { newSize ->
                        growIfNecessary(newSize)
                    }
                    pageData.mapIndexed { index, item ->
                        val offsetIndex = pageStartingIndex + index
                        setLoaded(offsetIndex, item)
                    }
                }
            }
        }
    }

    private fun resetPageToEmpty(pageNumber: Int) {
        elements.runSynchronised {
            val pageStartIndex = startingIndexOfPage(pageNumber)
            val pageEndIndex = endIndexOfPageFromStartingIndex(pageStartIndex)
            for (index in pageStartIndex..pageEndIndex) {
                if (indexInBounds(index)) {
                    setEmpty(index)
                }
            }
        }
    }

    private inline fun invalidate(refreshElementsInRange: Boolean, invalidateElements: Action) {
        elements.runSynchronised {
            invalidateElements()
        }
        dispose()
        this.lastAccessedIndex.let { lastAccessedIndex ->
            if (refreshElementsInRange) {
                loadAround(lastAccessedIndex)
            }
        }
    }
}