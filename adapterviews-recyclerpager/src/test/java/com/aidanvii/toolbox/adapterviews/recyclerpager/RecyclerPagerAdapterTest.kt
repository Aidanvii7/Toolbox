package com.aidanvii.toolbox.adapterviews.recyclerpager

import android.database.DataSetObservable
import android.view.View
import android.view.ViewGroup
import com.aidanvii.toolbox.unchecked
import com.nhaarman.mockito_kotlin.argumentCaptor
import com.nhaarman.mockito_kotlin.clearInvocations
import com.nhaarman.mockito_kotlin.inOrder
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.spy
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import de.jodamob.reflect.SuperReflect
import org.junit.Test
import org.mockito.InOrder
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class RecyclerPagerAdapterTest() {

    val mockView = mock<View>()
    val mockContainer = mock<ViewGroup>()
    val mockDataSetObservable = mock<DataSetObservable>()
    val spyPool = spy<ItemPool<ViewHolderWrapper<TestViewHolder>>>().apply {
        testableSparseArray<MutableList<ViewHolderWrapper<TestViewHolder>>>(
                variableName = "scrapHeapsByType",
                size = ItemPool.DEFAULT_MAX_SCRAP
        )
        testableSparseIntArray(
                variableName = "maxScrapByType",
                size = ItemPool.DEFAULT_MAX_SCRAP
        )
    }
    val tested = TestAdapter().apply {
        testableSparseArray<ViewHolderWrapper<TestViewHolder>>(
                variableName = "activeViewHolders"
        )
        SuperReflect.on(this).set("viewHolderWrapperPool", spyPool)
    }

    @Test
    @Suppress(unchecked)
    fun `instantiateItem returns PageItem with correct data`() {
        tested.position = 0
        tested.dataSet = listOf(
                Item(type = VIEW_TYPE_1, id = 1),
                Item(type = VIEW_TYPE_1, id = 2),
                Item(type = VIEW_TYPE_1, id = 3))
        val expectedPosition = tested.position
        val expectedTransaction = ViewTransaction.ADD

        val pageItem = tested.instantiateItem(mockContainer, tested.position) as PageItem<TestViewHolder>

        lateInitPropertyInitialised { pageItem.viewHolderWrapper }.assertFalse()
        pageItem.apply {
            assertEquals(expectedPosition, adapterPosition)
            assertEquals(expectedTransaction, viewTransaction)
        }
        verifyZeroInteractions(spyPool)
    }

    @Test
    fun `destroyItem sets ViewTransaction as REMOVE when recycling is enabled`() {
        tested.dataSet = listOf(
                Item(type = VIEW_TYPE_1, id = 1),
                Item(type = VIEW_TYPE_1, id = 2),
                Item(type = VIEW_TYPE_1, id = 3))
        val expectedTransaction = ViewTransaction.REMOVE
        val pageItem = instrumentCurrentPage()!!.pageItem
        clearInvocations(spyPool)

        tested.destroyItem(mockContainer, tested.position, pageItem)

        pageItem.apply {
            assertEquals(expectedTransaction, viewTransaction)
        }
        verifyZeroInteractions(spyPool)
    }

    @Test
    fun `destroyItem sets ViewTransaction as DESTROY when recycling is disabled`() {
        tested.recyclingEnabled = false
        tested.dataSet = listOf(
                Item(type = VIEW_TYPE_1, id = 1),
                Item(type = VIEW_TYPE_1, id = 2),
                Item(type = VIEW_TYPE_1, id = 3))
        val expectedTransaction = ViewTransaction.DESTROY
        val pageItem = instrumentCurrentPage()!!.pageItem
        clearInvocations(spyPool)

        tested.destroyItem(mockContainer, tested.position, pageItem)

        pageItem.apply {
            assertEquals(expectedTransaction, viewTransaction)
        }
        verifyZeroInteractions(spyPool)
    }

    @Test
    fun `notifyDataSetChanged with callback calls base notifyDataSetChanged`() {
        // property setter calls notifyDataSetChanged with callback
        tested.dataSet = listOf(
                Item(type = VIEW_TYPE_1, id = 1),
                Item(type = VIEW_TYPE_1, id = 2),
                Item(type = VIEW_TYPE_1, id = 3))

        verify(mockDataSetObservable).notifyChanged()
        verifyNoMoreInteractions(mockDataSetObservable)
    }

    @Test
    fun `instrumenting current page returns PageItem with correct data and interacts with pool correctly`() {
        tested.position = 0
        tested.dataSet = listOf(
                Item(type = VIEW_TYPE_1, id = 1),
                Item(type = VIEW_TYPE_1, id = 2),
                Item(type = VIEW_TYPE_1, id = 3))
        val expectedPosition = tested.position
        val expectedItemForPage = tested.dataSet[expectedPosition]

        val pageItem = instrumentCurrentPage()!!.pageItem

        pageItem.apply {
            lateInitPropertyInitialised { viewHolderWrapper }.assertTrue()
            assertEquals(expectedPosition, adapterPosition)
            assertEquals(expectedItemForPage, viewHolderWrapper.viewHolder.currentData)
            assertNull(viewTransaction)
        }
        inOrder(spyPool).apply {
            verifyPoolInteraction(popType = VIEW_TYPE_1)
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun `instrumenting swiping left once gives PageItem with correct data and interacts with pool correctly`() {
        tested.position = 0
        tested.dataSet = listOf(
                Item(type = VIEW_TYPE_1, id = 0),
                Item(type = VIEW_TYPE_1, id = 1),
                Item(type = VIEW_TYPE_1, id = 2))
        val expectedPosition = 2
        val expectedItemForPage = tested.dataSet[expectedPosition]

        val pageItem = instrumentSwipingLeft().last().pageItem

        pageItem.apply {
            lateInitPropertyInitialised { viewHolderWrapper }.assertTrue()
            assertEquals(expectedPosition, adapterPosition)
            assertEquals(expectedItemForPage, viewHolderWrapper.viewHolder.currentData)
            assertNull(viewTransaction)
        }
        inOrder(spyPool).apply {
            verifyPoolInteraction(popType = VIEW_TYPE_1)
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun `instrumenting swiping right once gives PageItem with correct data and interacts with pool correctly`() {
        tested.position = 2
        tested.dataSet = listOf(
                Item(type = VIEW_TYPE_1, id = 0),
                Item(type = VIEW_TYPE_1, id = 1),
                Item(type = VIEW_TYPE_1, id = 2))
        val expectedPosition = 0
        val expectedItemForPage = tested.dataSet[expectedPosition]

        val pageItem = instrumentSwipingRight().last().pageItem

        pageItem.apply {
            lateInitPropertyInitialised { viewHolderWrapper }.assertTrue()
            assertEquals(expectedPosition, adapterPosition)
            assertEquals(expectedItemForPage, viewHolderWrapper.viewHolder.currentData)
            assertNull(viewTransaction)
        }
        inOrder(spyPool).apply {
            verifyPoolInteraction(popType = VIEW_TYPE_1)
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun `instrumenting swiping right multiple times gives PageItems with correct data and interacts with pool correctly`() {
        tested.position = 4
        tested.dataSet = listOf(
                Item(type = VIEW_TYPE_1, id = 0),
                Item(type = VIEW_TYPE_1, id = 1),
                Item(type = VIEW_TYPE_1, id = 2),
                Item(type = VIEW_TYPE_1, id = 3),
                Item(type = VIEW_TYPE_1, id = 4))
        val expectedViewHolderWrapperCount = 3
        val pagesToMove = 4

        val pageItemContainers = instrumentSwipingRight(pagesToMove = pagesToMove)
        val actualWrapperCount = pageItemContainers.wrapperCount()

        assertEquals(expectedViewHolderWrapperCount, actualWrapperCount)
        pageItemContainers.filter { !it.destroyed }.forEach {
            it.pageItem.apply {
                lateInitPropertyInitialised { viewHolderWrapper }.assertTrue()
                val expectedPosition = tested.dataSet.indexOf(viewHolderWrapper.viewHolder.currentData)
                val expectedItemForPage = tested.dataSet[expectedPosition]
                assertEquals(expectedPosition, adapterPosition)
                assertEquals(expectedItemForPage, viewHolderWrapper.viewHolder.currentData)
                assertNull(viewTransaction)
            }
        }
        inOrder(spyPool).apply {
            verifyPoolInteraction(popType = VIEW_TYPE_1)
            verifyPoolInteraction(putType = VIEW_TYPE_1, popType = VIEW_TYPE_1)
            verifyPoolInteraction(putType = VIEW_TYPE_1, popType = VIEW_TYPE_1)
            verifyPoolInteraction(putType = VIEW_TYPE_1)
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun `instrumenting swiping left multiple times gives PageItems with correct data and interacts with pool correctly`() {
        tested.position = 0
        tested.dataSet = listOf(
                Item(type = VIEW_TYPE_1, id = 0),
                Item(type = VIEW_TYPE_1, id = 1),
                Item(type = VIEW_TYPE_1, id = 2),
                Item(type = VIEW_TYPE_1, id = 3),
                Item(type = VIEW_TYPE_1, id = 4))
        val expectedViewHolderWrapperCount = 3
        val pagesToMove = 4

        val pageItemContainers = instrumentSwipingLeft(pagesToMove = pagesToMove)
        val actualWrapperCount = pageItemContainers.wrapperCount()

        assertEquals(expectedViewHolderWrapperCount, actualWrapperCount)
        pageItemContainers.filter { !it.destroyed }.forEach {
            it.pageItem.apply {
                lateInitPropertyInitialised { viewHolderWrapper }.assertTrue()
                val expectedPosition = tested.dataSet.indexOf(viewHolderWrapper.viewHolder.currentData)
                val expectedItemForPage = tested.dataSet[expectedPosition]
                assertEquals(expectedPosition, adapterPosition)
                assertEquals(expectedItemForPage, viewHolderWrapper.viewHolder.currentData)
                assertNull(viewTransaction)
            }
        }
        inOrder(spyPool).apply {
            verifyPoolInteraction(popType = VIEW_TYPE_1)
            verifyPoolInteraction(putType = VIEW_TYPE_1, popType = VIEW_TYPE_1)
            verifyPoolInteraction(putType = VIEW_TYPE_1, popType = VIEW_TYPE_1)
            verifyPoolInteraction(putType = VIEW_TYPE_1)
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun `instrumenting swiping right multiple times with different view types gives PageItems with correct data and interacts with pool correctly`() {
        tested.position = 4
        tested.dataSet = listOf(
                Item(type = VIEW_TYPE_1, id = 0),
                Item(type = VIEW_TYPE_2, id = 1),
                Item(type = VIEW_TYPE_1, id = 2),
                Item(type = VIEW_TYPE_2, id = 3),
                Item(type = VIEW_TYPE_1, id = 4))
        val expectedWrapperType1Count = 2
        val expectedWrapperType2Count = 2
        val pagesToMove = 4

        val pageItemContainers = instrumentSwipingRight(pagesToMove = pagesToMove)
        val actualWrapperType1Count = pageItemContainers.wrapperCount(VIEW_TYPE_1)
        val actualWrapperType2Count = pageItemContainers.wrapperCount(VIEW_TYPE_2)

        assertEquals(expectedWrapperType1Count, actualWrapperType1Count)
        assertEquals(expectedWrapperType2Count, actualWrapperType2Count)
        pageItemContainers.filter { !it.destroyed }.forEach {
            it.pageItem.apply {
                lateInitPropertyInitialised { viewHolderWrapper }.assertTrue()
                val expectedPosition = tested.dataSet.indexOf(viewHolderWrapper.viewHolder.currentData)
                val expectedItemForPage = tested.dataSet[expectedPosition]
                assertEquals(expectedPosition, adapterPosition)
                assertEquals(expectedItemForPage, viewHolderWrapper.viewHolder.currentData)
                assertNull(viewTransaction)
            }
        }
        inOrder(spyPool).apply {
            verifyPoolInteraction(popType = VIEW_TYPE_1)
            verifyPoolInteraction(putType = VIEW_TYPE_1, popType = VIEW_TYPE_2)
            verifyPoolInteraction(putType = VIEW_TYPE_2, popType = VIEW_TYPE_1)
            verifyPoolInteraction(putType = VIEW_TYPE_1)
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun `instrumenting swiping left multiple times with different view types gives PageItems with correct data and interacts with pool correctly`() {
        tested.position = 0
        tested.dataSet = listOf(
                Item(type = VIEW_TYPE_1, id = 0),
                Item(type = VIEW_TYPE_2, id = 1),
                Item(type = VIEW_TYPE_1, id = 2),
                Item(type = VIEW_TYPE_2, id = 3),
                Item(type = VIEW_TYPE_1, id = 4))
        val expectedWrapperType1Count = 2
        val expectedWrapperType2Count = 2
        val pagesToMove = 4

        val pageItemContainers = instrumentSwipingLeft(pagesToMove = pagesToMove)
        val actualWrapperType1Count = pageItemContainers.wrapperCount(VIEW_TYPE_1)
        val actualWrapperType2Count = pageItemContainers.wrapperCount(VIEW_TYPE_2)

        assertEquals(expectedWrapperType1Count, actualWrapperType1Count)
        assertEquals(expectedWrapperType2Count, actualWrapperType2Count)
        pageItemContainers.filter { !it.destroyed }.forEach {
            it.pageItem.apply {
                lateInitPropertyInitialised { viewHolderWrapper }.assertTrue()
                val expectedPosition = tested.dataSet.indexOf(viewHolderWrapper.viewHolder.currentData)
                val expectedItemForPage = tested.dataSet[expectedPosition]
                assertEquals(expectedPosition, adapterPosition)
                assertEquals(expectedItemForPage, viewHolderWrapper.viewHolder.currentData)
                assertNull(viewTransaction)
            }
        }
        inOrder(spyPool).apply {
            verifyPoolInteraction(popType = VIEW_TYPE_1)
            verifyPoolInteraction(putType = VIEW_TYPE_1, popType = VIEW_TYPE_2)
            verifyPoolInteraction(putType = VIEW_TYPE_2, popType = VIEW_TYPE_1)
            verifyPoolInteraction(putType = VIEW_TYPE_1)
            verifyNoMoreInteractions()
        }
    }

    private fun List<PageItemContainer>.wrapperCount(viewType: Int? = null): Int {
        return if (viewType != null) {
            filter { it.isType(viewType) }.map { it.pageItem.viewHolderWrapper }.distinct().size
        } else {
            map { it.pageItem.viewHolderWrapper }.distinct().size
        }
    }

    private fun InOrder.verifyPoolInteraction(putType: Int? = null, popType: Int? = null) {
        putType?.let { verifyPutType(it) }
        popType?.let { verify(spyPool).popItem(it) }
    }

    private fun InOrder.verifyPutType(viewType: Int) {
        argumentCaptor<ViewHolderWrapper<TestViewHolder>>().apply {
            verify(spyPool).putItem(capture())
            assertEquals(viewType, firstValue.itemType)
        }
    }

    fun PageItemContainer.isType(viewType: Int): Boolean {
        val item = pageItem.viewHolderWrapper.viewHolder.currentData as Item
        return item.type == viewType
    }

    fun instrumentCurrentPage(): PageItemContainer? = instrumentPage(tested.position)
    fun instrumentLeftPage(): PageItemContainer? = instrumentPage(tested.position - 1)
    fun instrumentRightPage(): PageItemContainer? = instrumentPage(tested.position + 1)

    @Suppress(unchecked)
    fun instrumentPage(position: Int = 0): PageItemContainer? {
        with(tested) {
            var pageItemContainer: PageItemContainer? = null
            startUpdate(mockContainer)
            if (inBounds(position)) {
                val pageItem = instantiateItem(mockContainer, position) as PageItem<TestViewHolder>
                pageItemContainer = PageItemContainer(pageItem)
                tested.setPrimaryItem(mockContainer, position, pageItemContainer.pageItem)
            }
            tested.finishUpdate(mockContainer)
            return pageItemContainer
        }
    }

    fun instrumentSwipingLeft(initVisibleRange: Boolean = true, pagesToMove: Int = 1): List<PageItemContainer> {
        with(tested) {
            val pageItemContainers = mutableListOf<PageItemContainer>()
            var pageContainerToLeft: PageItemContainer? = null
            var pageContainerCenter: PageItemContainer? = null
            var pageContainerRight: PageItemContainer? = null
            if (initVisibleRange) {
                pageContainerToLeft = instrumentLeftPage()
                pageContainerCenter = instrumentCurrentPage()
                pageContainerRight = instrumentRightPage()
                pageContainerToLeft?.let { pageItemContainers.add(it) }
                pageContainerCenter?.let { pageItemContainers.add(it) }
                pageContainerRight?.let { pageItemContainers.add(it) }
                clearInvocations(spyPool)
            }
            runFor(pagesToMove) {
                startUpdate(mockContainer)
                val positionToInstantiate = position + 2
                val positionToDestroy = position - 1
                tryDestroy(positionToDestroy, pageContainerToLeft)
                val nextPageContainerToRight = tryInstantiate(positionToInstantiate, pageItemContainers)
                pageContainerToLeft = pageContainerCenter
                pageContainerCenter = pageContainerRight
                pageContainerRight = nextPageContainerToRight
                position++
                tested.setPrimaryItem(mockContainer, position, pageContainerCenter!!.pageItem)
                tested.finishUpdate(mockContainer)
            }
            return pageItemContainers
        }
    }

    fun instrumentSwipingRight(initVisibleRange: Boolean = true, pagesToMove: Int = 1): List<PageItemContainer> {
        with(tested) {
            val pageItemContainers = mutableListOf<PageItemContainer>()
            var pageContainerToRight: PageItemContainer? = null
            var pageContainerCenter: PageItemContainer? = null
            var pageContainerLeft: PageItemContainer? = null

            if (initVisibleRange) {
                pageContainerToRight = instrumentRightPage()
                pageContainerCenter = instrumentCurrentPage()
                pageContainerLeft = instrumentLeftPage()
                pageContainerToRight?.let { pageItemContainers.add(it) }
                pageContainerCenter?.let { pageItemContainers.add(it) }
                clearInvocations(spyPool)
                pageContainerLeft?.let { pageItemContainers.add(it) }
            }
            runFor(pagesToMove) {
                startUpdate(mockContainer)
                val positionToInstantiate = position - 2
                val positionToDestroy = position + 1
                val nextPageContainerToLeft = tryInstantiate(positionToInstantiate, pageItemContainers)
                tryDestroy(positionToDestroy, pageContainerToRight)
                pageContainerToRight = pageContainerCenter
                pageContainerCenter = pageContainerLeft
                pageContainerLeft = nextPageContainerToLeft
                position--
                tested.setPrimaryItem(mockContainer, position, pageContainerCenter!!.pageItem)
                tested.finishUpdate(mockContainer)
            }
            return pageItemContainers
        }
    }

    @Suppress(unchecked)
    private fun TestAdapter.tryInstantiate(positionToInstantiate: Int, pageItemContainers: MutableList<PageItemContainer>): PageItemContainer? {
        var nextPageContainer: PageItemContainer? = null
        if (inBounds(positionToInstantiate)) {
            val nextPage = instantiateItem(mockContainer, positionToInstantiate) as PageItem<TestViewHolder>
            nextPage.let {
                nextPageContainer = PageItemContainer(it).also {
                    pageItemContainers.add(it)
                }
            }
        }
        return nextPageContainer
    }

    private fun TestAdapter.tryDestroy(positionToDestroy: Int, pageContainer: PageItemContainer?) {
        if (inBounds(positionToDestroy) && pageContainer != null) {
            pageContainer.let {
                destroyItem(mockContainer, positionToDestroy, it.pageItem)
                it.destroyed = true
            }
        }
    }

    private fun TestAdapter.inBounds(position: Int) = position >= 0 && position < dataSet.size

    companion object {
        const val VIEW_TYPE_1 = 1
        const val VIEW_TYPE_2 = 2
        const val VIEW_TYPE_3 = 3
    }

    class PageItemContainer(val pageItem: PageItem<TestViewHolder>,
                            var destroyed: Boolean = false)

    data class Item(val type: Int, val id: Int)

    class ItemChangeResolver(
            val oldItems: List<Item>,
            val newItems: List<Item>)
        : RecyclerPagerAdapter.OnDataSetChangedCallback<Item> {
        override fun getNewAdapterPositionOfItem(item: Item) = newItems.indexOf(item)
        override fun getOldItemAt(oldAdapterPosition: Int) = oldItems[oldAdapterPosition]
        override fun getNewItemAt(newAdapterPosition: Int) = newItems[newAdapterPosition]
        override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean = oldItem == newItem
    }

    inner class TestAdapter : RecyclerPagerAdapter<Item, TestViewHolder>() {
        var position = 0
        var recyclingEnabled = true

        init {
            SuperReflect.on(this@TestAdapter).set("mObservable", mockDataSetObservable)
        }

        var dataSet: List<Item> = listOf()
            set(value) {
                val oldDataSet = field
                val newDataSet = value
                field = value
                    ItemChangeResolver(oldDataSet, newDataSet).also {
                        notifyDataSetChanged(it)
                    }
            }

        @Suppress(unchecked)
        fun pageItems(): List<PageItem<TestViewHolder>> {
            val mockContainer = mockContainer()
            val pageItems = dataSet.map { instantiateItem(mockContainer, dataSet.indexOf(it)) as PageItem<TestViewHolder> }
            finishUpdate(mockContainer)
            return pageItems
        }

        override fun shouldRecycleViewHolder(viewHolder: TestViewHolder, viewType: Int): Boolean = recyclingEnabled

        override fun getCount(): Int = dataSet.size

        override fun getItemViewType(adapterPosition: Int): Int = dataSet[adapterPosition].type

        override fun onCreateViewHolder(viewType: Int, position: Int, container: ViewGroup): TestViewHolder {
            return TestViewHolder(mockView())
        }

        override fun onBindViewHolder(viewHolder: TestViewHolder, adapterPosition: Int) {
            viewHolder.currentData = dataSet[adapterPosition]
        }
    }
}

