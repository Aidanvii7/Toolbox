package com.aidanvii.toolbox.adapterviews.databinding

import com.aidanvii.toolbox.testableSparseIntArray
import com.nhaarman.mockito_kotlin.whenever
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.mock
import org.junit.Test
import java.util.*
import kotlin.test.assertFailsWith

internal class MultiViewTypeHandlerTest {

    data class Param(val layoutId: Int, val bindingId: Int, val position: Int)

    val param1 = Param(layoutId = 3, bindingId = 4, position = 5)
    val param2 = Param(layoutId = 6, bindingId = 9, position = 8)

    val mockItem1 = mock<BindableAdapterItem>().apply {
        whenever(layoutId).thenReturn(param1.layoutId)
        whenever(bindingId).thenReturn(param1.bindingId)
    }
    val mockItem2 = mock<BindableAdapterItem>().apply {
        whenever(layoutId).thenReturn(param2.layoutId)
        whenever(bindingId).thenReturn(param2.bindingId)
    }
    val mockAdapter = mock<BindableAdapter<BindableAdapterItem, *>>().apply {
        whenever(getItem(param1.position)).thenReturn(mockItem1)
        whenever(getItem(param2.position)).thenReturn(mockItem2)
    }
    val tested = MultiViewTypeHandler<BindableAdapterItem>().apply {
        testableSparseIntArray(variableName = "cachedBindingIds")
        initBindableAdapter(mockAdapter)
    }

    @Test
    fun `getItemViewType returns layoutId of ResourceIdProvider`() {
        tested.getItemViewType(param1.position) `should be equal to` param1.layoutId
    }

    @Test
    fun `getLayoutId returns given viewType of ResourceIdProvider`() {
        Random().nextInt().let { someViewType ->
            tested.getLayoutId(someViewType) `should be equal to` someViewType
        }
    }

    @Test
    fun `getBindingId fails when internal sparseIntArray is empty`() {
        assertFailsWith<KotlinNullPointerException> {
            tested.getBindingId(param1.layoutId)
        }
    }

    @Test
    fun `getBindingId returns correct bindingId when internal sparseIntArray contains entry for key`() {
        tested.getItemViewType(param1.position)
        tested.getItemViewType(param2.position)

        tested.getBindingId(param1.layoutId) `should be equal to` param1.bindingId
        tested.getBindingId(param2.layoutId) `should be equal to` param2.bindingId
    }
}