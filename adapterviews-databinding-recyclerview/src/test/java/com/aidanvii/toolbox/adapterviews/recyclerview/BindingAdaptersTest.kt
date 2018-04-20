package com.aidanvii.toolbox.adapterviews.recyclerview

import android.content.Context
import android.os.Parcelable
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.makeNotifyNotCrash
import com.aidanvii.toolbox.adapterviews.databinding.BindableAdapterItem
import com.aidanvii.toolbox.databinding.IntBindingConsumer
import com.aidanvii.toolbox.rxSchedulers
import com.nhaarman.mockito_kotlin.inOrder
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.spy
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Rule
import org.junit.Test
import org.mockito.InOrder
import java.util.concurrent.atomic.AtomicBoolean

class BindingAdaptersTest {

    @get:Rule
    val schedulers = rxSchedulers { prepareMain().prepareComputation() }
    @get:Rule
    val viewTagTrackerRule = ViewTagTrackerRule()

    val givenItems = listOf(TestItem())

    val givenItemBoundListener = object : IntBindingConsumer {
        override fun invoke(value: Int) {
        }
    }

    val mockLayoutManagerState1 = mock<Parcelable>()
    val mockLayoutManager1 = mock<RecyclerView.LayoutManager>().apply {
        whenever(onSaveInstanceState()).thenReturn(mockLayoutManagerState1)
    }

    val mockLayoutManagerState2 = mock<Parcelable>()
    val mockLayoutManager2 = mock<RecyclerView.LayoutManager>().apply {
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
            layoutManagerFactory = { mockLayoutManager1 }
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
            layoutManagerFactory = { mockLayoutManager2 }
        )
    )

    val mockContext = mock<Context>()
    val mockRecyclerView = mock<RecyclerView>().apply {
        whenever(context).thenReturn(mockContext)
        whenever(layoutManager).thenReturn(mockLayoutManager1)
    }

    @Test
    fun `does things in correct order when called first time`() {

        mockRecyclerView._bind(
            binder = givenSpyBinder1,
            items = givenItems,
            itemBoundListener = givenItemBoundListener
        )

        inOrder(mockRecyclerView, givenSpyBinder1, spyAdapter1, mockLayoutManager1) {
            verifyItemsUpdated(spyAdapter1, givenItems)
            verifyBound(spyAdapter1, mockLayoutManager1)
            verifyNoMoreInteractions()
        }
    }

    @Test
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
            verifyBound(spyAdapter1, mockLayoutManager1)
            verifyItemsUpdated(spyAdapter1, givenItems)
            verifyNoMoreInteractions()
        }
    }

    @Test
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
            verifyBound(spyAdapter1, mockLayoutManager1)
            verifyItemsUpdated(spyAdapter2, givenItems)
            verifyUnbound(givenSpyBinder1, spyAdapter1, mockLayoutManagerState1)
            verifyBound(spyAdapter2, mockLayoutManager2)
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
        adapter: BindingRecyclerViewAdapter<TestItem>,
        layoutManager: RecyclerView.LayoutManager
    ) {
        verify(adapter).itemBoundListener = givenItemBoundListener
        verify(mockRecyclerView).adapter = adapter
        verify(mockRecyclerView).layoutManager = layoutManager
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

    class TestItem() : BindableAdapterItem {
        override val disposed = AtomicBoolean(false)
        override val layoutId: Int get() = 1
        override val bindingId: Int get() = 1
    }
}
