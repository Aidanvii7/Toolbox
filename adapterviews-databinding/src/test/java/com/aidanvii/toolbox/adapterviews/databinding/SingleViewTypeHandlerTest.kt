package com.aidanvii.toolbox.adapterviews.databinding

import com.nhaarman.mockito_kotlin.whenever
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.mock
import org.junit.Test
import java.util.*

internal class SingleViewTypeHandlerTest {

    val rand = Random()

    val inputViewType = rand.nextInt()
    val inputLayoutId = rand.nextInt()

    val expectedLayoutId = rand.nextInt()
    val expectedBindingId = rand.nextInt()

    val mockItem = mock<BindableAdapterItem>().apply {
        whenever(layoutId).thenReturn(expectedLayoutId)
        whenever(bindingId).thenReturn(expectedBindingId)
    }
    val mockAdapter = mock<BindableAdapter<BindableAdapterItem, *>>().apply {
        whenever(getItem(0)).thenReturn(mockItem)
    }

    val tested = SingleViewTypeHandler<BindableAdapterItem>().apply {
        initBindableAdapter(mockAdapter)
    }

    @Test
    fun `getItemViewType returns zero`() {
        tested.getItemViewType(rand.nextInt()) `should be equal to` 0
    }

    @Test
    fun `getBindingId returns expectedBindingId`() {
        tested.getItemViewType(rand.nextInt())

        tested.getBindingId(inputLayoutId) `should be equal to` expectedBindingId
    }

    @Test
    fun `getlayoutId returns expectedLayoutId`() {
        tested.getItemViewType(rand.nextInt())

        tested.getLayoutId(inputViewType) `should be equal to` expectedLayoutId
    }
}