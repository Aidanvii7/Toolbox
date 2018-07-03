package com.aidanvii.toolbox.adapterviews.databinding

import android.databinding.ViewDataBinding
import android.view.ViewGroup
import com.aidanvii.toolbox.databinding.IntBindingConsumer
import com.aidanvii.toolbox.testableSparseIntArray
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.inOrder
import com.nhaarman.mockito_kotlin.spy
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.whenever
import org.amshove.kluent.mock
import org.junit.Test

internal class BindableAdapterDelegateTest {

    companion object {
        const val VIEW_TYPE_1 = 1
        const val VIEW_TYPE_2 = 2
        const val BINDING_ID_1 = 10
        const val BINDING_ID_2 = 20

        const val ADAPTER_POSITION_1 = 1337
        const val ADAPTER_POSITION_2 = 7331
    }

    val spiedTestItem1 = spy(TestItem(id = 10, viewType = VIEW_TYPE_1, bindingId = BINDING_ID_1))
    val spiedTestItem2 = spy(TestItem(id = 20, viewType = VIEW_TYPE_2, bindingId = BINDING_ID_2))

    val mockContainer1 = mock<ViewGroup>()
    val mockContainer2 = mock<ViewGroup>()
    val mockBinding1 = mock<ViewDataBinding>().apply {
        whenever(root).thenReturn(mock())
    }
    val mockBinding2 = mock<ViewDataBinding>().apply {
        whenever(root).thenReturn(mock())
    }
    val mockViewHolder1 = mock<BindableAdapter.ViewHolder<ViewDataBinding, TestItem>>().apply {
        whenever(viewDataBinding).thenReturn(mockBinding1)
        whenever(bindingResourceId).thenReturn(BINDING_ID_1)
        whenever(boundAdapterItem).thenReturn(spiedTestItem1)
    }
    val mockViewHolder2 = mock<BindableAdapter.ViewHolder<ViewDataBinding, TestItem>>().apply {
        whenever(viewDataBinding).thenReturn(mockBinding2)
        whenever(bindingResourceId).thenReturn(BINDING_ID_2)
        whenever(boundAdapterItem).thenReturn(spiedTestItem2)
    }
    val mockBindingInflater = mock<BindingInflater>().apply {
        whenever(eq(mockContainer1).unattachedBindingOf<ViewDataBinding>(any())).thenReturn(mockBinding1)
        whenever(eq(mockContainer2).unattachedBindingOf<ViewDataBinding>(any())).thenReturn(mockBinding2)
    }
    val spyViewTypeHandler = spy<BindableAdapter.ViewTypeHandler<TestItem>>(MultiViewTypeHandler()).apply {
        testableSparseIntArray("cachedBindingIds", size = 0).apply {
            put(VIEW_TYPE_1, BINDING_ID_1)
            put(VIEW_TYPE_2, BINDING_ID_2)
        }
    }

    val mockItemBoundListener = mock<IntBindingConsumer>()

    val mockBindableAdapter = mock<BindableAdapter<TestItem, BindableAdapter.ViewHolder<ViewDataBinding, TestItem>>>().apply {
        whenever(itemBoundListener).thenReturn(mockItemBoundListener)

        whenever(viewTypeHandler).thenReturn(spyViewTypeHandler)
        whenever(bindingInflater).thenReturn(mockBindingInflater)

        whenever(createWith(BINDING_ID_1, mockBinding1)).thenReturn(mockViewHolder1)
        whenever(createWith(BINDING_ID_2, mockBinding2)).thenReturn(mockViewHolder2)

        whenever(getItem(ADAPTER_POSITION_1)).thenReturn(spiedTestItem1)
        whenever(getItem(ADAPTER_POSITION_2)).thenReturn(spiedTestItem2)
    }
    val tested = BindableAdapterDelegate<TestItem, BindableAdapter.ViewHolder<ViewDataBinding, TestItem>>().apply {
        bindableAdapter = mockBindableAdapter
    }

    @Test
    fun `onCreate creates view holder 1 with correct data`() {
        tested.onCreate(mockContainer1, spiedTestItem1.layoutId).let { viewHolder ->

            inOrder(spyViewTypeHandler, mockBindingInflater, mockContainer1, mockBindableAdapter).apply {
                verify(spyViewTypeHandler).getLayoutId(spiedTestItem1.viewType)
                verify(spyViewTypeHandler).getBindingId(spiedTestItem1.layoutId)
                mockBindingInflater.run {
                }
                verify(mockBindingInflater).apply {
                    mockContainer1.unattachedBindingOf<ViewDataBinding>(spiedTestItem1.layoutId)
                }
                verify(mockBindableAdapter).createWith(spiedTestItem1.bindingId, mockBinding1)
                verify(mockBindableAdapter).onCreated(viewHolder)
            }
        }
    }

    @Test
    fun `onCreate creates view holder 2 with correct data`() {
        tested.onCreate(mockContainer2, spiedTestItem2.layoutId).let { viewHolder ->

            inOrder(spyViewTypeHandler, mockBindingInflater, mockContainer2, mockBindableAdapter).apply {
                verify(spyViewTypeHandler).getLayoutId(spiedTestItem2.viewType)
                verify(spyViewTypeHandler).getBindingId(spiedTestItem2.layoutId)
                verify(mockBindingInflater).apply {
                    mockContainer2.unattachedBindingOf<ViewDataBinding>(spiedTestItem2.layoutId)
                }
                verify(mockBindableAdapter).createWith(spiedTestItem2.bindingId, mockBinding2)
                verify(mockBindableAdapter).onCreated(viewHolder)
            }
        }
    }

    @Test
    fun `onBind calls methods in correct order for test data 1 when onInterceptOnBind returns false`() {
        whenever(mockBindableAdapter.onInterceptOnBind(mockViewHolder1, ADAPTER_POSITION_1, null)).thenReturn(false)
        tested.onBind(mockViewHolder1, ADAPTER_POSITION_1, mockContainer1)
        inOrder(mockBindableAdapter, mockViewHolder1, mockBinding1, mockItemBoundListener, spiedTestItem1).apply {
            verify(mockBindableAdapter).getItem(ADAPTER_POSITION_1)
            verify(mockViewHolder1).boundAdapterItem = spiedTestItem1
            verify(mockBindableAdapter).onInterceptOnBind(mockViewHolder1, ADAPTER_POSITION_1, null)
            verify(mockBinding1).setVariable(BINDING_ID_1, spiedTestItem1)
            verify(mockBindableAdapter).onBindExtras(mockViewHolder1, ADAPTER_POSITION_1)
            verify(spiedTestItem1).onBindExtras(mockBinding1, ADAPTER_POSITION_1, mockContainer1)
            verify(mockBinding1).executePendingBindings()
            verify(mockBindableAdapter).onBound(mockViewHolder1, ADAPTER_POSITION_1)
            verify(spiedTestItem1).onBound(ADAPTER_POSITION_1)
            verify(mockBindableAdapter.itemBoundListener)?.invoke(ADAPTER_POSITION_1)
        }
    }

    @Test
    fun `onBind calls methods in correct order for test data 2 when onInterceptOnBind returns false`() {
        whenever(mockBindableAdapter.onInterceptOnBind(mockViewHolder2, ADAPTER_POSITION_2, null)).thenReturn(false)
        tested.onBind(mockViewHolder2, ADAPTER_POSITION_2, mockContainer2)
        inOrder(mockBindableAdapter, mockViewHolder2, mockBinding2, mockItemBoundListener, spiedTestItem2).apply {
            verify(mockBindableAdapter).getItem(ADAPTER_POSITION_2)
            verify(mockViewHolder2).boundAdapterItem = spiedTestItem2
            verify(mockBindableAdapter).onInterceptOnBind(mockViewHolder2, ADAPTER_POSITION_2, null)
            verify(mockBinding2).setVariable(BINDING_ID_2, spiedTestItem2)
            verify(mockBindableAdapter).onBindExtras(mockViewHolder2, ADAPTER_POSITION_2)
            verify(spiedTestItem2).onBindExtras(mockBinding2, ADAPTER_POSITION_2, mockContainer2)
            verify(mockBinding2).executePendingBindings()
            verify(mockBindableAdapter).onBound(mockViewHolder2, ADAPTER_POSITION_2)
            verify(spiedTestItem2).onBound(ADAPTER_POSITION_2)
            verify(mockBindableAdapter.itemBoundListener)?.invoke(ADAPTER_POSITION_2)
        }
    }

    @Test
    fun `onBind calls methods in correct order for test data 1 when onInterceptOnBind returns true`() {
        whenever(mockBindableAdapter.onInterceptOnBind(mockViewHolder1, ADAPTER_POSITION_1, null)).thenReturn(true)
        tested.onBind(mockViewHolder1, ADAPTER_POSITION_1, mockContainer1)
        inOrder(mockBindableAdapter, mockViewHolder1, mockBinding1, mockItemBoundListener, spiedTestItem1).apply {
            verify(mockBindableAdapter).getItem(ADAPTER_POSITION_1)
            verify(mockViewHolder1).boundAdapterItem = spiedTestItem1
            verify(mockBindableAdapter).onInterceptOnBind(mockViewHolder1, ADAPTER_POSITION_1, null)
            verify(mockBinding1, times(0)).setVariable(BINDING_ID_1, spiedTestItem1)
            verify(mockBindableAdapter).onBindExtras(mockViewHolder1, ADAPTER_POSITION_1)
            verify(spiedTestItem1).onBindExtras(mockBinding1, ADAPTER_POSITION_1, mockContainer1)
            verify(mockBinding1).executePendingBindings()
            verify(mockBindableAdapter).onBound(mockViewHolder1, ADAPTER_POSITION_1)
            verify(spiedTestItem1).onBound(ADAPTER_POSITION_1)
            verify(mockBindableAdapter.itemBoundListener)?.invoke(ADAPTER_POSITION_1)
        }
    }

    @Test
    fun `onBind calls methods in correct order for test data 2 when onInterceptOnBind returns true`() {
        whenever(mockBindableAdapter.onInterceptOnBind(mockViewHolder2, ADAPTER_POSITION_2, null)).thenReturn(true)
        tested.onBind(mockViewHolder2, ADAPTER_POSITION_2, mockContainer2)
        inOrder(mockBindableAdapter, mockViewHolder2, mockBinding2, mockItemBoundListener, spiedTestItem2).apply {
            verify(mockBindableAdapter).getItem(ADAPTER_POSITION_2)
            verify(mockViewHolder2).boundAdapterItem = spiedTestItem2
            verify(mockBindableAdapter).onInterceptOnBind(mockViewHolder2, ADAPTER_POSITION_2, null)
            verify(mockBinding2, times(0)).setVariable(BINDING_ID_2, spiedTestItem2)
            verify(mockBindableAdapter).onBindExtras(mockViewHolder2, ADAPTER_POSITION_2)
            verify(spiedTestItem2).onBindExtras(mockBinding2, ADAPTER_POSITION_2, mockContainer2)
            verify(mockBinding2).executePendingBindings()
            verify(mockBindableAdapter).onBound(mockViewHolder2, ADAPTER_POSITION_2)
            verify(spiedTestItem2).onBound(ADAPTER_POSITION_2)
            verify(mockBindableAdapter.itemBoundListener)?.invoke(ADAPTER_POSITION_2)
        }
    }

    @Test
    fun `onUnbind calls methods in correct order for test data 1 when onInterceptOnBind returns false`() {
        whenever(mockBindableAdapter.onInterceptUnbind(mockViewHolder1, ADAPTER_POSITION_1)).thenReturn(false)
        tested.onUnbind(mockViewHolder1, ADAPTER_POSITION_1, mockContainer1)
        inOrder(mockBindableAdapter, mockBinding1, mockViewHolder1, spiedTestItem1).apply {
            verify(mockBindableAdapter).onInterceptUnbind(mockViewHolder1, ADAPTER_POSITION_1)
            verify(mockBinding1).setVariable(BINDING_ID_1, null)
            verify(mockBindableAdapter).onUnbindExtras(mockViewHolder1, ADAPTER_POSITION_1)
            verify(spiedTestItem1).onUnbindExtras(mockBinding1, ADAPTER_POSITION_1, mockContainer1)
            verify(mockBinding1).executePendingBindings()
            verify(mockBindableAdapter).onUnbound(mockViewHolder1, ADAPTER_POSITION_1)
            verify(spiedTestItem1).onUnBound(ADAPTER_POSITION_1)
            verify(mockViewHolder1).boundAdapterItem = null
        }
    }

    @Test
    fun `onUnbind calls methods in correct order for test data 2 when onInterceptOnBind returns false`() {
        whenever(mockBindableAdapter.onInterceptUnbind(mockViewHolder2, ADAPTER_POSITION_2)).thenReturn(false)
        tested.onUnbind(mockViewHolder2, ADAPTER_POSITION_2, mockContainer2)
        inOrder(mockBindableAdapter, mockBinding2, mockViewHolder2, spiedTestItem2).apply {
            verify(mockBindableAdapter).onInterceptUnbind(mockViewHolder2, ADAPTER_POSITION_2)
            verify(mockBinding2).setVariable(BINDING_ID_2, null)
            verify(mockBindableAdapter).onUnbindExtras(mockViewHolder2, ADAPTER_POSITION_2)
            verify(spiedTestItem2).onUnbindExtras(mockBinding2, ADAPTER_POSITION_2, mockContainer2)
            verify(mockBinding2).executePendingBindings()
            verify(mockBindableAdapter).onUnbound(mockViewHolder2, ADAPTER_POSITION_2)
            verify(spiedTestItem2).onUnBound(ADAPTER_POSITION_2)
            verify(mockViewHolder2).boundAdapterItem = null
        }
    }

    @Test
    fun `onUnbind calls methods in correct order for test data 1 when onInterceptOnBind returns true`() {
        whenever(mockBindableAdapter.onInterceptUnbind(mockViewHolder1, ADAPTER_POSITION_1)).thenReturn(true)
        tested.onUnbind(mockViewHolder1, ADAPTER_POSITION_1, mockContainer1)
        inOrder(mockBindableAdapter, mockBinding1, mockViewHolder1).apply {
            verify(mockBindableAdapter).onInterceptUnbind(mockViewHolder1, ADAPTER_POSITION_1)
            verify(mockBinding1, times(0)).setVariable(BINDING_ID_1, null)
            verify(mockBindableAdapter).onUnbindExtras(mockViewHolder1, ADAPTER_POSITION_1)
            verify(mockBinding1).executePendingBindings()
            verify(mockBindableAdapter).onUnbound(mockViewHolder1, ADAPTER_POSITION_1)
            verify(mockViewHolder1).boundAdapterItem = null
        }
    }

    @Test
    fun `onUnbind calls methods in correct order for test data 2 when onInterceptOnBind returns true`() {
        whenever(mockBindableAdapter.onInterceptUnbind(mockViewHolder2, ADAPTER_POSITION_2)).thenReturn(true)
        tested.onUnbind(mockViewHolder2, ADAPTER_POSITION_2, mockContainer2)
        inOrder(mockBindableAdapter, mockBinding2, mockViewHolder2).apply {
            verify(mockBindableAdapter).onInterceptUnbind(mockViewHolder2, ADAPTER_POSITION_2)
            verify(mockBinding2, times(0)).setVariable(BINDING_ID_2, null)
            verify(mockBindableAdapter).onUnbindExtras(mockViewHolder2, ADAPTER_POSITION_2)
            verify(mockBinding2).executePendingBindings()
            verify(mockBindableAdapter).onUnbound(mockViewHolder2, ADAPTER_POSITION_2)
            verify(mockViewHolder2).boundAdapterItem = null
        }
    }

    @Test
    fun `onDestroy calls methods in correct order for test data 1 when boundItem is not null`() {
        whenever(mockViewHolder1.boundAdapterItem).thenReturn(spiedTestItem1)
        tested.onDestroy(mockViewHolder1, ADAPTER_POSITION_1)
        inOrder(mockBindableAdapter).apply {
            verify(mockBindableAdapter).onUnbound(mockViewHolder1, ADAPTER_POSITION_1)
            verify(mockBindableAdapter).onDestroyed(mockViewHolder1, ADAPTER_POSITION_1)
        }
    }

    @Test
    fun `onDestroy calls methods in correct order for test data 2 when boundItem is not null`() {
        whenever(mockViewHolder2.boundAdapterItem).thenReturn(spiedTestItem2)
        tested.onDestroy(mockViewHolder2, ADAPTER_POSITION_2)
        inOrder(mockBindableAdapter).apply {
            verify(mockBindableAdapter).onUnbound(mockViewHolder2, ADAPTER_POSITION_2)
            verify(mockBindableAdapter).onDestroyed(mockViewHolder2, ADAPTER_POSITION_2)
        }
    }

    @Test
    fun `onDestroy calls methods in correct order for test data 1 when boundItem is null`() {
        whenever(mockViewHolder1.boundAdapterItem).thenReturn(null)
        tested.onDestroy(mockViewHolder1, ADAPTER_POSITION_1)
        inOrder(mockBindableAdapter).apply {
            verify(mockBindableAdapter, times(0)).onUnbound(mockViewHolder1, ADAPTER_POSITION_1)
            verify(mockBindableAdapter).onDestroyed(mockViewHolder1, ADAPTER_POSITION_1)
        }
    }

    @Test
    fun `onDestroy calls methods in correct order for test data 2 when boundItem is null`() {
        whenever(mockViewHolder2.boundAdapterItem).thenReturn(null)
        tested.onDestroy(mockViewHolder2, ADAPTER_POSITION_2)
        inOrder(mockBindableAdapter).apply {
            verify(mockBindableAdapter, times(0)).onUnbound(mockViewHolder2, ADAPTER_POSITION_2)
            verify(mockBindableAdapter).onDestroyed(mockViewHolder2, ADAPTER_POSITION_2)
        }
    }
}