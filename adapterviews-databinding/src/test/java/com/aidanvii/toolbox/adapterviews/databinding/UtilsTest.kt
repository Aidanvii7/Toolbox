package com.aidanvii.toolbox.adapterviews.databinding

import com.aidanvii.toolbox.DisposableItem
import org.amshove.kluent.`should be true`
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicBoolean

internal class UtilsTest {

    val disposableAdapterItemNotInitialised1 = DisposableAdapterItem()
    val disposableAdapterItemNotInitialised2 = DisposableAdapterItem()
    val disposableAdapterItemInitialised1 = DisposableAdapterItem().apply {
        lazyBindableItem.value
    }
    val disposableAdapterItemInitialised2 = DisposableAdapterItem().apply {
        lazyBindableItem.value
    }
    val nonDisposableAdapterItem1 = NonDisposableAdapterItem()
    val nonDisposableAdapterItem2 = NonDisposableAdapterItem()

    val adapterItems: List<BindableAdapterItem> = listOf(
        disposableAdapterItemNotInitialised1,
        nonDisposableAdapterItem1,
        disposableAdapterItemInitialised1,
        disposableAdapterItemNotInitialised2,
        nonDisposableAdapterItem2,
        disposableAdapterItemInitialised2
    )

    class NonDisposableAdapterItem : BindableAdapterItem {
        override val layoutId: Int get() = 0
    }

    class DisposableAdapterItem : BindableAdapterItem, DisposableItem {
        override val _disposed = AtomicBoolean(false)
        override val layoutId: Int get() = 0
        override val lazyBindableItem = lazy(LazyThreadSafetyMode.NONE) { this }
    }

    @Nested
    @DisplayName("When disposeAll is called on list of BindableAdapterItems")
    inner class DisposeAll {

        @BeforeEach
        fun givenWhen() {
            adapterItems.disposeAll()
        }

        @Test
        @DisplayName("Disposes all BindableAdapterItems whose lazyBindableItem is initialised and is a DisposableItem")
        fun disposesInitialisedDisposableItems() {
            disposableAdapterItemInitialised1.disposed.`should be true`()
            disposableAdapterItemInitialised2.disposed.`should be true`()
        }

        @Test
        @DisplayName("Doesn't dispose BindableAdapterItems whose lazyBindableItem is not initialised, even if it's a DisposableItem")
        fun doesntDisposesNotInitialisedDisposableItems() {
            disposableAdapterItemInitialised1.disposed.`should be true`()
            disposableAdapterItemInitialised2.disposed.`should be true`()
        }
    }
}