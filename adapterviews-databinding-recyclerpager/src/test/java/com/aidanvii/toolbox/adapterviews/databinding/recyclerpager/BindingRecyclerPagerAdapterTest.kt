package com.aidanvii.toolbox.adapterviews.databinding.recyclerpager

import android.content.Context
import android.content.res.Resources
import android.databinding.ViewDataBinding
import android.support.v7.widget.makeNotifyNotCrash
import android.view.View
import android.view.ViewGroup
import com.aidanvii.toolbox.adapterviews.databinding.BindableAdapter
import com.aidanvii.toolbox.adapterviews.databinding.BindableAdapterDelegate
import com.aidanvii.toolbox.adapterviews.databinding.BindingInflater
import com.aidanvii.toolbox.adapterviews.databinding.TestItem
import com.aidanvii.toolbox.adapterviews.recyclerpager.RecyclerPagerAdapter
import com.nhaarman.mockito_kotlin.argumentCaptor
import com.nhaarman.mockito_kotlin.reset
import com.nhaarman.mockito_kotlin.spy
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import de.jodamob.reflect.SuperReflect
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should equal`
import org.amshove.kluent.`should not throw`
import org.amshove.kluent.`should throw`
import org.amshove.kluent.any
import org.amshove.kluent.mock
import org.junit.Test
import java.util.*

internal class BindingRecyclerPagerAdapterTest {

    companion object {
        val random = Random()
        val VIEW_TYPE = random.nextInt()
        val BINDING_ID = random.nextInt()
        val ADAPTER_POSITION = random.nextInt()
    }

    val mockContainer = mock<ViewGroup>()

    val mockResources = mock<Resources>().apply {
        whenever(getString(any())).thenReturn("hello world")
    }
    val mockContext = mock<Context>().apply {
        whenever(applicationContext).thenReturn(this)
        whenever(resources).thenReturn(mockResources)
    }
    val mockRootView = mock<View>().apply {
        whenever(context).thenReturn(mockContext)
    }
    val mockBinding = mock<ViewDataBinding>().apply {
        whenever(root).thenReturn(mockRootView)
    }
    val mockViewHolder = mock<BindingRecyclerPagerItemViewHolder<*, TestItem>>().apply {
        whenever(viewDataBinding).thenReturn(mockBinding)
    }
    val mockViewTypeHandler = mock<BindableAdapter.ViewTypeHandler<TestItem>>().apply {
        whenever(getItemViewType(ADAPTER_POSITION)).thenReturn(VIEW_TYPE)
    }
    val mockBindingInflater = mock<BindingInflater>()
    val spyAreItemAndContentsTheSame = spy<(old: TestItem, new: TestItem) -> Boolean>({ old, new -> old == new })
    val mockDelegate = mock<BindableAdapterDelegate<TestItem, BindingRecyclerPagerItemViewHolder<*, TestItem>>>().apply {
        whenever(onCreate(mockContainer, VIEW_TYPE)).thenReturn(mockViewHolder)
    }

    val spyTested = spy(
        BindingRecyclerPagerAdapter(
            BindingRecyclerPagerAdapter.Builder(
                delegate = mockDelegate,
                areItemAndContentsTheSame = spyAreItemAndContentsTheSame,
                viewTypeHandler = mockViewTypeHandler,
                bindingInflater = mockBindingInflater,
                applicationContext = mockContext
            )
        )
    ).apply { makeNotifyNotCrash() }

    @Test
    fun `getItemViewType is forwarded to ViewTypeHandler`() {
        spyTested.getItemViewType(ADAPTER_POSITION).let { itemViewType ->
            verify(mockViewTypeHandler).getItemViewType(ADAPTER_POSITION)
            itemViewType `should be equal to` VIEW_TYPE
        }
    }

    @Test
    fun `onCreateViewHolderFor forwards to delegate`() {
        SuperReflect.on(spyTested).call("onCreateViewHolder", VIEW_TYPE, ADAPTER_POSITION, mockContainer)
            .get<BindingRecyclerPagerItemViewHolder<*, TestItem>>().let { viewHolder ->
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
    fun `onBindViewHolder forwards to delegate onBind`() {
        SuperReflect.on(spyTested).call("onBindViewHolder", mockViewHolder, ADAPTER_POSITION)

        verify(mockDelegate).onBind(mockViewHolder, ADAPTER_POSITION, null)
    }

    @Test
    fun `onUnbindViewHolder forwards to delegate onUnbind`() {
        SuperReflect.on(spyTested).call("onUnbindViewHolder", mockViewHolder, ADAPTER_POSITION)

        verify(mockDelegate).onUnbind(mockViewHolder, ADAPTER_POSITION, null)
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

        verify(expectedNewItems = newItems)
    }

    @Test
    fun `set items with test data 2 notifies adapter correctly`() {
        val oldItems = listOf(
            TestItem(id = 0, viewType = 1, bindingId = 10),
            TestItem(id = 1, viewType = 1, bindingId = 10),
            TestItem(id = 2, viewType = 1, bindingId = 10),
            TestItem(id = 3, viewType = 2, bindingId = 20)
        ).also {
            spyTested.items = it
        }

        reset(spyTested)

        val newItems = listOf(
            TestItem(id = 0, viewType = 1, bindingId = 10),
            TestItem(id = 2, viewType = 1, bindingId = 10),
            TestItem(id = 3, viewType = 2, bindingId = 20)
        ).also {
            spyTested.items = it
        }

        verify(expectedOldItems = oldItems, expectedNewItems = newItems)
    }

    @Test
    fun `set items with test data 3 notifies adapter correctly`() {
        val oldItems = listOf(
            TestItem(id = 0, viewType = 1, bindingId = 10),
            TestItem(id = 1, viewType = 1, bindingId = 10),
            TestItem(id = 2, viewType = 1, bindingId = 10),
            TestItem(id = 3, viewType = 1, bindingId = 10)
        ).also {
            spyTested.items = it
        }
        reset(spyTested)

        val newItems = listOf(
            TestItem(id = 0, viewType = 1, bindingId = 10),
            TestItem(id = 1, viewType = 1, bindingId = 10),
            TestItem(id = 2, viewType = 1, bindingId = 10),
            TestItem(id = 3, viewType = 1, bindingId = 10),
            TestItem(id = 4, viewType = 1, bindingId = 10),
            TestItem(id = 5, viewType = 1, bindingId = 10),
            TestItem(id = 6, viewType = 1, bindingId = 10)
        ).also {
            spyTested.items = it
        }

        verify(expectedOldItems = oldItems, expectedNewItems = newItems)
    }

    @Test
    fun `set items with test data 4 notifies adapter correctly`() {
        val oldItems = listOf(
            TestItem(id = 0, viewType = 1, bindingId = 10),
            TestItem(id = 1, viewType = 1, bindingId = 10),
            TestItem(id = 2, viewType = 1, bindingId = 10),
            TestItem(id = 6, viewType = 1, bindingId = 10)
        ).also {
            spyTested.items = it
        }
        reset(spyTested)

        val newItems = listOf(
            TestItem(id = 0, viewType = 1, bindingId = 10),
            TestItem(id = 1, viewType = 1, bindingId = 10),
            TestItem(id = 2, viewType = 1, bindingId = 10),
            TestItem(id = 3, viewType = 1, bindingId = 10),
            TestItem(id = 4, viewType = 1, bindingId = 10),
            TestItem(id = 5, viewType = 1, bindingId = 10),
            TestItem(id = 6, viewType = 1, bindingId = 10)
        ).also {
            spyTested.items = it
        }

        verify(expectedOldItems = oldItems, expectedNewItems = newItems)
    }

    @Test
    fun `set items with test data 5 notifies adapter correctly`() {
        val oldItems = listOf(
            TestItem(id = 0, viewType = 1, bindingId = 10),
            TestItem(id = 2, viewType = 1, bindingId = 10),
            TestItem(id = 4, viewType = 2, bindingId = 20)
        ).also {
            spyTested.items = it
        }
        reset(spyTested)

        val newItems = listOf(
            TestItem(id = 0, viewType = 1, bindingId = 10),
            TestItem(id = 1, viewType = 1, bindingId = 10),
            TestItem(id = 2, viewType = 1, bindingId = 10),
            TestItem(id = 3, viewType = 1, bindingId = 10),
            TestItem(id = 4, viewType = 2, bindingId = 20)
        ).also {
            spyTested.items = it
        }

        verify(expectedOldItems = oldItems, expectedNewItems = newItems)

    }

    fun verify(expectedNewItems: List<TestItem>, expectedOldItems: List<TestItem> = emptyList()) {
        argumentCaptor<RecyclerPagerAdapter.OnDataSetChangedCallback<TestItem>>().apply {
            verify(spyTested).notifyDataSetChanged(capture())
            firstValue.verify(expectedOldItems = expectedOldItems, expectedNewItems = expectedNewItems)
            for (oldItem in expectedOldItems) {
                firstValue.getNewAdapterPositionOfItem(oldItem) `should be equal to` expectedNewItems.indexOf(oldItem)
            }
        }
    }

    fun RecyclerPagerAdapter.OnDataSetChangedCallback<TestItem>.verify(expectedOldItems: List<TestItem>, expectedNewItems: List<TestItem>) {
        this `old size is` expectedOldItems.size
        this `new size is` expectedNewItems.size

        expectedOldItems.forEachIndexed { index, testItem ->
            getOldItemAt(index) `should be` testItem
        }

        expectedNewItems.forEachIndexed { index, testItem ->
            getNewItemAt(index) `should be` testItem
        }

    }

    infix fun RecyclerPagerAdapter.OnDataSetChangedCallback<TestItem>.`old size is`(size: Int) {
        if (size == 0) {
            { getOldItemAt(0) } `should throw` IndexOutOfBoundsException::class
        }
        for (index in 0 until size) {
            { getOldItemAt(index) } `should not throw` IndexOutOfBoundsException::class
        }
        { getOldItemAt(size) } `should throw` IndexOutOfBoundsException::class
    }

    infix fun RecyclerPagerAdapter.OnDataSetChangedCallback<TestItem>.`new size is`(size: Int) {
        if (size == 0) {
            { getNewItemAt(0) } `should throw` IndexOutOfBoundsException::class
        }
        for (index in 0 until size) {
            { getNewItemAt(index) } `should not throw` IndexOutOfBoundsException::class
        }
        { getNewItemAt(size) } `should throw` IndexOutOfBoundsException::class
    }
}