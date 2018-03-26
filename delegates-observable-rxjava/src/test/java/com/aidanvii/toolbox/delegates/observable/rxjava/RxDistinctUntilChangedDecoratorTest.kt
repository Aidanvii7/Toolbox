package com.aidanvii.toolbox.delegates.observable.rxjava

import com.aidanvii.toolbox.assignedButNeverAccessed
import com.aidanvii.toolbox.delegates.observable.distinctUntilChanged
import com.aidanvii.toolbox.delegates.observable.doOnNext
import com.aidanvii.toolbox.delegates.observable.observable
import com.aidanvii.toolbox.unusedValue
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.argumentCaptor
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import com.nhaarman.mockito_kotlin.whenever
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.mock
import org.junit.Test

@Suppress(assignedButNeverAccessed, unusedValue)
class RxDistinctUntilChangedDecoratorTest {

    val mockAreEqual = mock<(oldValue: Int, newValue: Int) -> Boolean>()
    val mockDoOnNext = mock<(propertyEvent: Int) -> Unit>()

    @Test
    fun `propagates values downstream when tested is given different values`() {
        val given = 1
        var tested by observable(0)
                .toRx()
                .distinctUntilChanged()
                .doOnNext(mockDoOnNext)

        tested = given

        argumentCaptor<Int>().apply {
            verify(mockDoOnNext).invoke(capture())
            firstValue `should be equal to` given
        }
        verifyNoMoreInteractions(mockDoOnNext)
    }

    @Test
    fun `does not propagate values downstream when tested has been given same value`() {
        val given = 0
        var tested by observable(given)
                .toRx()
                .distinctUntilChanged()
                .doOnNext(mockDoOnNext)

        tested = given

        verifyZeroInteractions(mockDoOnNext)
    }

    @Test
    fun `propagates values downstream when areEqual returns false`() {
        val givenInitial = 0
        val givenNext = 1
        whenever(mockAreEqual(any(), any())).thenReturn(false)
        var tested by observable(givenInitial)
                .toRx()
                .distinctUntilChanged(mockAreEqual)
                .doOnNext(mockDoOnNext)

        tested = givenNext

        verify(mockAreEqual).invoke(givenInitial, givenNext)
        argumentCaptor<Int>().apply {
            verify(mockDoOnNext).invoke(capture())
            firstValue `should be equal to` givenNext
        }
        verifyNoMoreInteractions(mockDoOnNext)
    }

    @Test
    fun `does not propagate values downstream when areEqual returns true`() {
        val given = 0
        whenever(mockAreEqual(any(), any())).thenReturn(true)
        var tested by observable(given)
                .toRx()
                .distinctUntilChanged(mockAreEqual)
                .doOnNext(mockDoOnNext)

        tested = given

        verify(mockAreEqual).invoke(given, given)
        verifyZeroInteractions(mockDoOnNext)
    }
}