package com.aidanvii.toolbox.adapterviews.recyclerview

import android.databinding.ViewDataBinding
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.makeNotifyNotCrash
import com.aidanvii.toolbox.adapterviews.databinding.BindableAdapter
import com.aidanvii.toolbox.adapterviews.databinding.BindableAdapterDelegate
import com.aidanvii.toolbox.adapterviews.databinding.BindingInflater
import com.aidanvii.toolbox.adapterviews.databinding.TestItem
import com.aidanvii.toolbox.adapterviews.databinding.defaultGetChangedProperties
import com.nhaarman.mockito_kotlin.inOrder
import com.nhaarman.mockito_kotlin.reset
import com.nhaarman.mockito_kotlin.spy
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should equal`
import org.amshove.kluent.`should throw`
import org.amshove.kluent.mock
import org.junit.Test
import java.util.Random

@ExperimentalCoroutinesApi
internal class BindingRecyclerViewAdapterTest {

    companion object {
        val random = Random()
        val VIEW_TYPE = random.nextInt()
        val BINDING_ID = random.nextInt()
        val ADAPTER_POSITION = random.nextInt()
    }

    val mockContainer = mock<RecyclerView>()

    val mockBinding = mock<ViewDataBinding>().apply {
        whenever(root).thenReturn(mock())
    }

    val mockViewHolder = mock<BindingRecyclerViewItemViewHolder<*, TestItem>>().apply {
        whenever(adapterPosition).thenReturn(ADAPTER_POSITION)
        whenever(viewDataBinding).thenReturn(mockBinding)
    }

    val mockViewTypeHandler = mock<BindableAdapter.ViewTypeHandler<TestItem>>().apply {
        whenever(getItemViewType(ADAPTER_POSITION)).thenReturn(VIEW_TYPE)
    }

    val mockBindingInflater = mock<BindingInflater>()
    val spyAreItemsTheSame = spy<(old: TestItem, new: TestItem) -> Boolean>({ old, new -> old == new })
    val spyAreContentsTheSame = spy<(old: TestItem, new: TestItem) -> Boolean>({ old, new -> old == new })

    val mockDelegate = mock<BindableAdapterDelegate<TestItem, BindingRecyclerViewItemViewHolder<*, TestItem>>>().apply {
        whenever(onCreate(mockContainer, VIEW_TYPE)).thenReturn(mockViewHolder)
    }

    val spyTested = spy(
        BindingRecyclerViewAdapter(
            BindingRecyclerViewAdapter.Builder(
                delegate = mockDelegate,
                areItemsTheSame = spyAreItemsTheSame,
                areContentsTheSame = spyAreContentsTheSame,
                getChangedProperties = defaultGetChangedProperties,
                viewTypeHandler = mockViewTypeHandler,
                bindingInflater = mockBindingInflater,
                itemBoundObservers = emptyList(),
                uiDispatcher = Dispatchers.Unconfined,
                workerDispatcher = Dispatchers.Unconfined
            )
        )
    ).apply {
        makeNotifyNotCrash()
        onAttachedToRecyclerView(mockContainer)
    }

    @Test
    fun `getItemViewType is forwarded to ViewTypeHandler`() {
        spyTested.getItemViewType(ADAPTER_POSITION).let { itemViewType ->
            verify(mockViewTypeHandler).getItemViewType(ADAPTER_POSITION)
            itemViewType `should be equal to` VIEW_TYPE
        }
    }

    @Test
    fun `onCreateViewHolder forwards to delegate`() {
        spyTested.onCreateViewHolder(mockContainer, VIEW_TYPE).let { viewHolder ->
            verify(mockDelegate).onCreate(mockContainer, VIEW_TYPE)
            viewHolder `should equal` mockViewHolder
        }
    }

    @Test
    fun `createWith creates viewHolder with correct data`() {
        spyTested.createWith(BINDING_ID, mockBinding).let { viewHolder ->
            viewHolder.bindingResourceId `should equal` BINDING_ID
            viewHolder.viewDataBinding `should equal` mockBinding
        }
    }

    @Test
    fun `onBindViewHolder with empty payloads forwards to onBindViewHolder without payloads`() {
        spyTested.onBindViewHolder(mockViewHolder, ADAPTER_POSITION, emptyList())

        verify(spyTested).onBindViewHolder(mockViewHolder, ADAPTER_POSITION)
    }

    @Test
    fun `onBindViewHolder with invalid payloads throws exception`() {
        val function = { spyTested.onBindViewHolder(mockViewHolder, ADAPTER_POSITION, listOf(Any())) }

        function `should throw` UnsupportedOperationException::class
    }

    @Test
    fun `given onBindViewHolder is called with empty payloads, onInterceptOnBind returns false`() {
        spyTested.onBindViewHolder(mockViewHolder, ADAPTER_POSITION, emptyList())

        spyTested.onInterceptOnBind(mockViewHolder, ADAPTER_POSITION, null) `should be equal to` false
    }

    @Test
    fun `given onBindViewHolder is called with valid payloads, onInterceptOnBind returns true, subsequent calls return false`() {
        val expectedChangePayload = AdapterNotifier.ChangePayload(mock(), intArrayOf(1, 2, 3, 4, 5))
        spyTested.onBindViewHolder(mockViewHolder, ADAPTER_POSITION, listOf(expectedChangePayload))

        spyTested.onInterceptOnBind(mockViewHolder, ADAPTER_POSITION, null) `should be equal to` true
        spyTested.onInterceptOnBind(mockViewHolder, ADAPTER_POSITION, null) `should be equal to` false
        expectedChangePayload.apply {
            inOrder(sender).apply {
                verify(sender).adapterBindStart(spyTested)
                for (propertyId in changedProperties) {
                    verify(sender).notifyAdapterPropertyChanged(propertyId, false)
                }
            }
        }
    }

    @Test
    fun `onBindViewHolder forwards to delegate onBind`() {
        spyTested.onBindViewHolder(mockViewHolder, ADAPTER_POSITION)

        verify(mockDelegate).onBind(mockViewHolder, ADAPTER_POSITION, mockContainer)
    }

    @Test
    fun `onViewRecycled forwards to delegate onUnbind`() {
        spyTested.onViewRecycled(mockViewHolder)

        verify(mockDelegate).onUnbind(mockViewHolder, ADAPTER_POSITION, mockContainer)
    }

    @Test
    fun `set items with test data 1 notifies adapter correctly`() {
        val newItems = listOf(
            TestItem(id = 0, viewType = 1, bindingId = 10),
            TestItem(id = 1, viewType = 1, bindingId = 10),
            TestItem(id = 2, viewType = 1, bindingId = 10),
            TestItem(id = 3, viewType = 2, bindingId = 20)
        )

        spyTested.items = newItems

        verify(spyTested).notifyItemRangeInserted(0, 4)
    }

    @Test
    fun `set items with test data 2 notifies adapter correctly`() {
        spyTested.items = listOf(
            TestItem(id = 0, viewType = 1, bindingId = 10),
            TestItem(id = 1, viewType = 1, bindingId = 10),
            TestItem(id = 2, viewType = 1, bindingId = 10),
            TestItem(id = 3, viewType = 2, bindingId = 20)
        )
        reset(spyTested)

        spyTested.items = listOf(
            TestItem(id = 0, viewType = 1, bindingId = 10),
            TestItem(id = 2, viewType = 1, bindingId = 10),
            TestItem(id = 3, viewType = 2, bindingId = 20)
        )

        verify(spyTested).notifyItemRangeRemoved(1, 1)
    }

    @Test
    fun `set items with test data 3 notifies adapter correctly`() {
        spyTested.items = listOf(
            TestItem(id = 0, viewType = 1, bindingId = 10),
            TestItem(id = 1, viewType = 1, bindingId = 10),
            TestItem(id = 2, viewType = 1, bindingId = 10),
            TestItem(id = 3, viewType = 1, bindingId = 10)
        )
        reset(spyTested)

        spyTested.items = listOf(
            TestItem(id = 0, viewType = 1, bindingId = 10),
            TestItem(id = 1, viewType = 1, bindingId = 10),
            TestItem(id = 2, viewType = 1, bindingId = 10),
            TestItem(id = 3, viewType = 1, bindingId = 10),
            TestItem(id = 4, viewType = 1, bindingId = 10),
            TestItem(id = 5, viewType = 1, bindingId = 10),
            TestItem(id = 6, viewType = 1, bindingId = 10)
        )

        verify(spyTested).notifyItemRangeInserted(4, 3)
    }

    @Test
    fun `set items with test data 4 notifies adapter correctly`() {
        spyTested.items = listOf(
            TestItem(id = 0, viewType = 1, bindingId = 10),
            TestItem(id = 1, viewType = 1, bindingId = 10),
            TestItem(id = 2, viewType = 1, bindingId = 10),
            TestItem(id = 6, viewType = 1, bindingId = 10)
        )
        reset(spyTested)

        spyTested.items = listOf(
            TestItem(id = 0, viewType = 1, bindingId = 10),
            TestItem(id = 1, viewType = 1, bindingId = 10),
            TestItem(id = 2, viewType = 1, bindingId = 10),
            TestItem(id = 3, viewType = 1, bindingId = 10),
            TestItem(id = 4, viewType = 1, bindingId = 10),
            TestItem(id = 5, viewType = 1, bindingId = 10),
            TestItem(id = 6, viewType = 1, bindingId = 10)
        )

        verify(spyTested).notifyItemRangeInserted(3, 3)
    }

    @Test
    fun `set items with test data 5 notifies adapter correctly`() {
        spyTested.items = listOf(
            TestItem(id = 0, viewType = 1, bindingId = 10),
            TestItem(id = 2, viewType = 1, bindingId = 10),
            TestItem(id = 4, viewType = 2, bindingId = 20)
        )
        reset(spyTested)

        spyTested.items = listOf(
            TestItem(id = 0, viewType = 1, bindingId = 10),
            TestItem(id = 1, viewType = 1, bindingId = 10),
            TestItem(id = 2, viewType = 1, bindingId = 10),
            TestItem(id = 3, viewType = 1, bindingId = 10),
            TestItem(id = 4, viewType = 2, bindingId = 20)
        )

        inOrder(spyTested).apply {
            verify(spyTested).notifyItemRangeInserted(2, 1)
            verify(spyTested).notifyItemRangeInserted(1, 1)
        }
    }
}