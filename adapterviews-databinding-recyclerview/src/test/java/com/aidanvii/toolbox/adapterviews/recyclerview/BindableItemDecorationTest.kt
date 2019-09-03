package com.aidanvii.toolbox.adapterviews.recyclerview

import android.graphics.Canvas
import android.graphics.Rect
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import com.aidanvii.toolbox.adapterviews.databinding.BindableAdapterItem
import com.aidanvii.toolbox.spied
import com.nhaarman.mockito_kotlin.inOrder
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.amshove.kluent.mock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BindableItemDecorationTest {

    val spyTested = ExampleBindableItemDecoration().spied()

    val mockItem1 = mock<BindableAdapterItem>()
    val mockItem2 = mock<BindableAdapterItem>()
    val mockAdapter = mock<BindingRecyclerViewAdapter<BindableAdapterItem>>().apply {
        whenever(items).thenReturn(listOf(mockItem1, mockItem2))
    }
    val mockLayoutParams = mock<RecyclerView.LayoutParams>()
    val mockChild1 = mock<View>().apply {
        whenever(layoutParams).thenReturn(mockLayoutParams)
    }
    val mockChild2 = mock<View>().apply {
        whenever(layoutParams).thenReturn(mockLayoutParams)
    }
    val mockRecyclerView = mock<RecyclerView>().apply {
        whenever(adapter).thenReturn(mockAdapter)
        whenever(childCount).thenReturn(2)
        whenever(getChildAdapterPosition(mockChild1)).thenReturn(0)
        whenever(getChildAdapterPosition(mockChild2)).thenReturn(1)
        whenever(getChildAt(0)).thenReturn(mockChild1)
        whenever(getChildAt(1)).thenReturn(mockChild2)
    }
    val mockCanvas = mock<Canvas>()
    val mockOutRect = mock<Rect>()
    val mockRecyclerViewState = mock<RecyclerView.State>()

    @Nested
    inner class `When onDraw called` {

        @BeforeEach
        fun before() {
            spyTested.onDraw(mockCanvas, mockRecyclerView, mockRecyclerViewState)
        }

        @Test
        fun `forwards invocation to onDraw with mockItem1 then mockItem2`() {
            inOrder(spyTested).apply {
                verify(spyTested).onDraw(mockItem1, mockCanvas, mockRecyclerViewState)
                verify(spyTested).onDraw(mockItem2, mockCanvas, mockRecyclerViewState)
            }
        }
    }

    @Nested
    inner class `When getItemOffsets called with mockChild1` {

        @BeforeEach
        fun before() {
            spyTested.getItemOffsets(mockOutRect, mockChild1, mockRecyclerView, mockRecyclerViewState)
        }

        @Test
        fun `forwards invocation to getItemOffsets with mockItem1`() {
            verify(spyTested).getItemOffsets(mockItem1, mockOutRect, mockLayoutParams, mockRecyclerViewState)
        }
    }

    @Nested
    inner class `When getItemOffsets called with mockChild2` {

        @BeforeEach
        fun before() {
            spyTested.getItemOffsets(mockOutRect, mockChild2, mockRecyclerView, mockRecyclerViewState)
        }

        @Test
        fun `forwards invocation to getItemOffsets with mockItem2`() {
            verify(spyTested).getItemOffsets(mockItem2, mockOutRect, mockLayoutParams, mockRecyclerViewState)
        }
    }

    class ExampleBindableItemDecoration : BindableItemDecoration<BindableAdapterItem>() {
        public override fun onDraw(adapterItem: BindableAdapterItem, canvas: Canvas, state: RecyclerView.State) {}
        public override fun getItemOffsets(
            adapterItem: BindableAdapterItem,
            outRect: Rect,
            layoutParams: RecyclerView.LayoutParams,
            state: RecyclerView.State
        ) {
        }
    }
}