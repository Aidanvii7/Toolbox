package com.aidanvii.toolbox.adapterviews.recyclerview

import android.content.Context
import android.os.Parcelable
import androidx.appcompat.widget.makeNotifyNotCrash
import com.aidanvii.toolbox.adapterviews.databinding.BindableAdapterItem
import com.aidanvii.toolbox.databinding.IntBindingConsumer
import com.nhaarman.mockito_kotlin.inOrder
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.spy
import com.nhaarman.mockito_kotlin.whenever
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.mockito.InOrder

const val ignoreReason = "#9: test is broken, difficult to fix as it's brittle. Needs rewritten"

@ExperimentalCoroutinesApi
class BindingAdaptersTest {

    @get:Rule
    val viewTagTrackerRule = ViewTagTrackerRule()

    val givenItems = listOf(TestItem())

    val givenItemBoundListener = object : IntBindingConsumer {
        override fun invoke(value: Int) {
        }
    }

    init {
        BindingRecyclerViewAdapter.testModeEnabled = true
    }

    val mockLayoutManagerState1 = mock<Parcelable>()
    val mockLayoutManager1 = mock<androidx.recyclerview.widget.RecyclerView.LayoutManager>().apply {
        whenever(onSaveInstanceState()).thenReturn(mockLayoutManagerState1)
    }

    val mockLayoutManagerState2 = mock<Parcelable>()
    val mockLayoutManager2 = mock<androidx.recyclerview.widget.RecyclerView.LayoutManager>().apply {
        whenever(onSaveInstanceState()).thenReturn(mockLayoutManagerState2)
    }

    lateinit var spyAdapter1: BindingRecyclerViewAdapter<TestItem>
    lateinit var spyAdapter2: BindingRecyclerViewAdapter<TestItem>

    val givenSpyBinder1 = spy(
        BindingRecyclerViewBinder<TestItem>(
            adapterFactory = {
                spy(BindingRecyclerViewAdapter(it)).also {
                    spyAdapter1 = it
                    spyAdapter1.makeNotifyNotCrash()
                }
            },
            layoutManagerFactory = { mockLayoutManager1 },
            uiDispatcher = Dispatchers.Unconfined,
            workerDispatcher = Dispatchers.Unconfined
        )
    )

    val givenSpyBinder2 = spy(
        BindingRecyclerViewBinder<TestItem>(
            adapterFactory = {
                spy(BindingRecyclerViewAdapter(it)).also {
                    spyAdapter2 = it
                    spyAdapter2.makeNotifyNotCrash()
                }
            },
            layoutManagerFactory = { mockLayoutManager2 },
            uiDispatcher = Dispatchers.Unconfined,
            workerDispatcher = Dispatchers.Unconfined
        )
    )

    val mockContext = mock<Context>()
    val mockRecyclerView = mock<androidx.recyclerview.widget.RecyclerView>().apply {
        whenever(context).thenReturn(mockContext)
        whenever(layoutManager).thenReturn(mockLayoutManager1)
    }

    @Test
    @Ignore(ignoreReason)
    fun `does things in correct order when called first time`() {

        mockRecyclerView._bind(
            binder = givenSpyBinder1,
            items = givenItems,
            itemBoundListener = givenItemBoundListener
        )

        inOrder(mockRecyclerView, givenSpyBinder1, spyAdapter1, mockLayoutManager1) {
            verifyItemsUpdated(spyAdapter1, givenItems)
            verifyBound(givenSpyBinder1, spyAdapter1, mockLayoutManager1)
            verifyNoMoreInteractions()
        }
    }

    @Test
    @Ignore(ignoreReason)
    fun `does not do anything when called with the same parameters again`() {
        mockRecyclerView._bind(
            binder = givenSpyBinder1,
            items = givenItems,
            itemBoundListener = givenItemBoundListener
        )

        mockRecyclerView._bind(
            binder = givenSpyBinder1,
            items = givenItems,
            itemBoundListener = givenItemBoundListener
        )

        inOrder(mockRecyclerView, givenSpyBinder1, spyAdapter1, mockLayoutManager1) {
            verifyItemsUpdated(spyAdapter1, givenItems)
            verifyBound(givenSpyBinder1, spyAdapter1, mockLayoutManager1)
            verifyItemsUpdated(spyAdapter1, givenItems)
            verifyNoMoreInteractions()
        }
    }

    @Test
    @Ignore(ignoreReason)
    fun `when given new binder instance, old binder instance is cleaned up`() {
        mockRecyclerView._bind(
            binder = givenSpyBinder1,
            items = givenItems,
            itemBoundListener = givenItemBoundListener
        )

        mockRecyclerView._bind(
            binder = givenSpyBinder2,
            items = givenItems,
            itemBoundListener = givenItemBoundListener
        )
        inOrder(
            mockRecyclerView,
            givenSpyBinder1,
            givenSpyBinder2,
            spyAdapter1,
            spyAdapter2,
            mockLayoutManager1,
            mockLayoutManager2
        ) {
            verifyItemsUpdated(spyAdapter1, givenItems)
            verifyBound(givenSpyBinder1, spyAdapter1, mockLayoutManager1)
            verifyItemsUpdated(spyAdapter2, givenItems)
            verifyUnbound(givenSpyBinder1, spyAdapter1, mockLayoutManagerState1)
            verifyBound(givenSpyBinder2, spyAdapter2, mockLayoutManager2)
            verifyNoMoreInteractions()
        }
    }

    private fun InOrder.verifyItemsUpdated(
        adapter: BindingRecyclerViewAdapter<TestItem>,
        givenItems: List<TestItem>
    ) {
        verify(adapter).items = givenItems
    }

    private fun InOrder.verifyBound(
        binder: BindingRecyclerViewBinder<TestItem>,
        adapter: BindingRecyclerViewAdapter<TestItem>,
        layoutManager: androidx.recyclerview.widget.RecyclerView.LayoutManager
    ) {
        verify(adapter).itemBoundListener = givenItemBoundListener
        verify(mockRecyclerView).adapter = adapter
        verify(mockRecyclerView).layoutManager = layoutManager
        verify(binder).recycledViewPoolWrapper
    }

    private fun InOrder.verifyUnbound(
        binder: BindingRecyclerViewBinder<TestItem>,
        adapter: BindingRecyclerViewAdapter<TestItem>,
        layoutManagerState: Parcelable
    ) {
        verify(binder).layoutManagerState = layoutManagerState
        verify(binder).adapter
        verify(adapter).itemBoundListener = null
    }

    class TestItem : BindableAdapterItem {
        override val layoutId: Int get() = 1
        override val bindingId: Int get() = 1
    }
}
