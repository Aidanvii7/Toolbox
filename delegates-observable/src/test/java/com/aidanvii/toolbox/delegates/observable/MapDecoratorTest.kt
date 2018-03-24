package com.aidanvii.toolbox.delegates.observable

import com.aidanvii.toolbox.assignedButNeverAccessed
import com.aidanvii.toolbox.unusedValue
import com.aidanvii.toolbox.unusedVariable
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.inOrder
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import com.nhaarman.mockito_kotlin.whenever
import org.amshove.kluent.mock
import org.junit.Test

@Suppress(assignedButNeverAccessed, unusedValue, unusedVariable)
class MapDecoratorTest {

    val mockTransform = mock<(Int) -> Boolean>()
    val mockDoOnNext = mock<(value: Boolean) -> Unit>()

    @Test
    fun `propagated values is true`() {
        whenever(mockTransform(any())).thenReturn(true)
        val givenInitial = 0
        val givenNext = 1
        var property by observable(givenInitial)
                .map(mockTransform)
                .doOnNext(mockDoOnNext)

        property = givenNext

        inOrder(mockTransform).apply {
            verify(mockTransform).invoke(givenInitial)
            verify(mockTransform).invoke(givenNext)
        }
        verify(mockDoOnNext).invoke(true)
        verifyNoMoreInteractions(mockDoOnNext)
    }

    @Test
    fun `propagated values is false`() {
        whenever(mockTransform(any())).thenReturn(false)
        val givenInitial = 0
        val givenNext = 1
        var property by observable(givenInitial)
                .map(mockTransform)
                .doOnNext(mockDoOnNext)

        property = givenNext

        inOrder(mockTransform).apply {
            verify(mockTransform).invoke(givenInitial)
            verify(mockTransform).invoke(givenNext)
        }
        verify(mockDoOnNext).invoke(false)
        verifyNoMoreInteractions(mockDoOnNext)
    }
}