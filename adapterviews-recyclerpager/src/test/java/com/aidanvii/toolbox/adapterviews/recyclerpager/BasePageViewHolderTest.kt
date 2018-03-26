package com.aidanvii.toolbox.adapterviews.recyclerpager

import android.view.View
import android.view.ViewGroup
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations.initMocks
import kotlin.test.assertEquals

internal class BasePageViewHolderTest() {

    lateinit var tested: BasePageViewHolder

    @Mock lateinit var mockView: View
    @Mock lateinit var mockContainer: ViewGroup

    @Before
    fun before() {
        initMocks(this)
        tested = BasePageViewHolder(mockView)
    }

    @Test
    fun `getView returns given view in constructor`() {
        val actualView = tested.view

        assertEquals(mockView, actualView)
    }

    @Test
    fun `onDestroyed does nothing with view`() {
        tested.onDestroyed()

        verifyZeroInteractions(mockView)
    }

    @Test
    fun `addViewToContainer adds view to container`() {
        tested.addViewToContainer(mockContainer)

        verify(mockContainer).addView(mockView)
        verifyNoMoreInteractions(mockView, mockContainer)
    }

    @Test
    fun `removeViewFromContainer removes view from container`() {
        tested.removeViewFromContainer(mockContainer)

        verify(mockContainer).removeView(mockView)
        verifyNoMoreInteractions(mockView, mockContainer)
    }
}

