package com.aidanvii.toolbox.adapterviews.recyclerview

import com.aidanvii.toolbox.DisposableItem
import com.aidanvii.toolbox.adapterviews.databinding.BindableAdapterItem
import com.aidanvii.toolbox.spied
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicBoolean

internal class DataObserverDisposalPluginTest {

    val tested = DataObserverDisposalPlugin<TestItem>()

    @Nested
    inner class `when isChanging is false` {

        val isChanging = false

        val testItem = TestItem().spied()

        @Nested
        inner class `when onItemBound is called` {

            init {
                tested.onItemBound(testItem, mock(), isChanging)
            }

            @Test
            fun `nothing happens`() {
                verifyZeroInteractions(testItem)
            }
        }

        @Nested
        inner class `when onItemUnBound is called` {

            init {
                tested.onItemUnBound(testItem, mock(), isChanging)
            }

            @Test
            fun `item is disposed`() {
                verify(testItem).dispose()
            }
        }
    }

    @Nested
    inner class `when isChanging is true` {

        val isChanging = true

        val testItem = TestItem().spied()

        @Nested
        inner class `when onItemBound is called` {

            init {
                tested.onItemBound(testItem, mock(), isChanging)
            }

            @Test
            fun `nothing happens`() {
                verifyZeroInteractions(testItem)
            }
        }

        @Nested
        inner class `when onItemUnBound is called` {

            init {
                tested.onItemUnBound(testItem, mock(), isChanging)
            }

            @Test
            fun `nothing happens`() {
                verifyZeroInteractions(testItem)
            }
        }
    }

    class TestItem : BindableAdapterItem, DisposableItem {

        override val _disposed = AtomicBoolean(false)

        override val bindingId: Int get() = 1
        override val layoutId: Int get() = 1
    }
}