package com.aidanvii.toolbox.adapterviews.recyclerpager

import android.view.View
import android.view.ViewGroup
import com.nhaarman.mockito_kotlin.whenever
import org.amshove.kluent.mock
import kotlin.test.assertFalse
import kotlin.test.assertTrue

fun Boolean.assertTrue() = assertTrue(this)
fun Boolean.assertFalse() = assertFalse(this)

inline fun lateInitPropertyInitialised(lateInitInvoker: () -> Unit): Boolean {
    var isInitialised = false
    try {
        lateInitInvoker()
        isInitialised = true
    } catch (exception: UninitializedPropertyAccessException) {
        print(exception.message)
    }
    return isInitialised
}

fun mockView(hasParent: Boolean = false): View {
    return mock<View>().also {
        if (hasParent) {
            whenever(it.parent).thenReturn(mockContainer())
        }
    }
}

fun mockContainer(): ViewGroup = mock()

fun mockViewHolder(): TestViewHolder = mock()

internal fun mockPageItem(): PageItem<TestViewHolder> {
    return mock<PageItem<TestViewHolder>>().also {
        val mockViewHolderWrapper = mockViewHolderWrapper()
        whenever(it.viewHolderWrapper).thenReturn(mockViewHolderWrapper)
    }
}

internal fun mockViewHolderWrapper(viewHolder: TestViewHolder = mockViewHolder(),
                                   viewType: Int = 0): ViewHolderWrapper<TestViewHolder> {
    return mock<ViewHolderWrapper<TestViewHolder>>().also {
        whenever(it.itemType).thenReturn(viewType)
        whenever(it.viewHolder).thenReturn(viewHolder)
    }
}

internal fun mockViewHolderWrappers(size: Int,
                                    viewHolder: TestViewHolder = mockViewHolder(),
                                    viewType: Int = 0): List<ViewHolderWrapper<TestViewHolder>> {

    return mutableListOf<ViewHolderWrapper<TestViewHolder>>().also {
        for (i in 1..size) {
            it.add(mockViewHolderWrapper(viewHolder, viewType))
        }
    }
}

inline fun runFor(count: Int, action: () -> Unit) {
    for (i in 1..count) {
        action()
    }
}