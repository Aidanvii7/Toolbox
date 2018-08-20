package com.aidanvii.toolbox.adapterviews.recyclerpager

import com.nhaarman.mockito_kotlin.inOrder
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import kotlin.test.assertNull

internal class PageItemTest() {

    @Mock lateinit var mockAdapter: RecyclerPagerAdapter<DataSetChangeResolverTest.Item, TestViewHolder>

    @Before
    fun before() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun `runPendingTransaction on Transaction type ADD calls correct methods`() {
        val tested = pageItem()
        val container = mockContainer()
        val viewHolder = mockViewHolder()
        val viewHolderWrapper = mockViewHolderWrapper(viewHolder = viewHolder)
        tested.viewHolderWrapper = viewHolderWrapper
        tested.viewTransaction = ViewTransaction.ADD

        tested.runPendingTransaction(container)

        assertNull(tested.viewTransaction)
        inOrder(mockAdapter, viewHolderWrapper).apply {
            verify(viewHolderWrapper).viewHolder
            verify(viewHolderWrapper).addViewToContainer(container)
            verifyNoMoreInteractions(mockAdapter, viewHolderWrapper)
        }
    }

    @Test
    fun `runPendingTransaction on Transaction type REMOVE calls correct methods`() {
        val tested = pageItem()
        val container = mockContainer()
        val viewHolder = mockViewHolder()
        val viewHolderWrapper = mockViewHolderWrapper(viewHolder = viewHolder)
        tested.viewHolderWrapper = viewHolderWrapper
        tested.viewTransaction = ViewTransaction.REMOVE

        tested.runPendingTransaction(container)

        assertNull(tested.viewTransaction)
        inOrder(mockAdapter, viewHolderWrapper).apply {
            verify(viewHolderWrapper).viewHolder
            verify(viewHolderWrapper).removeViewFromContainer(container)
            verifyNoMoreInteractions(mockAdapter, viewHolderWrapper)
        }
    }

    @Test
    fun `runPendingTransaction on Transaction type DESTROY calls correct methods`() {
        val tested = pageItem()
        val container = mockContainer()
        val viewHolder = mockViewHolder()
        val viewHolderWrapper = mockViewHolderWrapper(viewHolder = viewHolder)
        tested.viewHolderWrapper = viewHolderWrapper
        tested.viewTransaction = ViewTransaction.DESTROY

        tested.runPendingTransaction(container)

        assertNull(tested.viewTransaction)
        inOrder(mockAdapter, viewHolderWrapper).apply {
            verify(viewHolderWrapper).viewHolder
            verify(viewHolderWrapper).removeViewFromContainer(container)
            verify(viewHolderWrapper).destroy()
            verifyNoMoreInteractions(mockAdapter, viewHolderWrapper)
        }
    }

    fun pageItem(viewType: Int = TestViewHolder.VIEW_TYPE_0, adapterPosition: Int = 0): PageItem<TestViewHolder> {
        return PageItem(mockAdapter, viewType, adapterPosition)
    }
}

