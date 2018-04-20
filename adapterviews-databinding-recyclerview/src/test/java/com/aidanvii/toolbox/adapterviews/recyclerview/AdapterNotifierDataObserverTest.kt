package com.aidanvii.toolbox.adapterviews.recyclerview

import com.aidanvii.toolbox.DisposableItem
import com.aidanvii.toolbox.adapterviews.databinding.BindableAdapterItem
import com.aidanvii.toolbox.boundInt
import com.aidanvii.toolbox.spied
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.inOrder
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import de.jodamob.reflect.SuperReflect
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

@RunWith(Parameterized::class)
internal class AdapterNotifierDataObserverTest(val parameter: Parameter) {

    val mockAdapter = mock<BindingRecyclerViewAdapter<TestItem>>().apply {
        SuperReflect.on(this).set("_items", parameter.items)
        whenever(items).thenCallRealMethod()
        whenever(getItem(any())).thenCallRealMethod()
        whenever(tempPreviousItems).thenReturn(parameter.items)
    }

    val tested = AdapterNotifierDataObserver(mockAdapter)

    @Test
    fun `onItemRangeInserted`() {
        parameter.apply {

            tested.onItemRangeInserted(positionStart, itemsFromStart.size)

            itemsFromStart.forEach { testItem ->
                testItem.adapterNotifierItem?.let {
                    verify(it).bindAdapter(mockAdapter)
                }
            }
        }
    }

    @Test
    fun `onItemRangeRemoved`() {
        parameter.apply {

            tested.onItemRangeRemoved(positionStart, itemsFromStart.size)

            itemsFromStart.forEach { testItem ->
                testItem.adapterNotifierItem?.let {
                    inOrder(testItem, it).apply {
                        verify(testItem).dispose()
                        verify(it).unbindAdapter(mockAdapter)
                    }
                }
            }
        }
    }

    @Test
    fun `onItemRangeChanged`() {
        parameter.apply {

            tested.onItemRangeChanged(positionStart, itemsFromStart.size, null)

            itemsFromStart.forEach { testItem ->
                testItem.adapterNotifierItem?.let {
                    inOrder(testItem, it).apply {
                        verify(testItem).dispose()
                        verify(it).unbindAdapter(mockAdapter)
                        verify(it).bindAdapter(mockAdapter)
                    }
                }
            }
        }
    }

    class Parameter(
        val positionStart: Int,
        val items: List<TestItem>
    ) {
        val itemsFromStart = items.subList(positionStart, items.size).also {
            it.forEach { it.lazyBindableItem.value }
        }
    }

    class TestItem(
        hasAdapterNotifierItem: Boolean
    ) : BindableAdapterItem, DisposableItem {

        override val disposed = AtomicBoolean(false)

        override val bindingId: Int get() = 1
        override val layoutId: Int get() = 1

        override val lazyBindableItem = lazy(LazyThreadSafetyMode.NONE) {
            adapterNotifierItem ?: this
        }
        val adapterNotifierItem by lazy(LazyThreadSafetyMode.NONE) {
            if (hasAdapterNotifierItem) mock<AdapterNotifier>() else null
        }
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun parameters(): List<Parameter> {
            val random = Random()
            return (0..20).map {
                val itemMax = random.boundInt(5, 20)
                val positionStart = random.boundInt(5, itemMax)
                Parameter(
                    positionStart = positionStart,
                    items = (0..itemMax).map { TestItem(random.nextBoolean()).spied() }
                )
            }
        }
    }
}