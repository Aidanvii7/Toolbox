package com.aidanvii.delegates.observable

import com.aidanvii.common.assignedButNeverAccessed
import com.aidanvii.common.unusedValue
import com.aidanvii.common.unusedVariable
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import com.nhaarman.mockito_kotlin.whenever
import org.amshove.kluent.mock
import org.junit.Test

@Suppress(assignedButNeverAccessed, unusedValue, unusedVariable)
class FilterDecoratorTest {

    val mockPredicate = mock<(Int) -> Boolean>()
    val mockDoOnNext = mock<(propertyEvent: Int) -> Unit>()

    @Test
    fun `propagates values downstream when predicate returns true`() {
        whenever(mockPredicate(any())).thenReturn(true)
        val givenInitial = 1
        val givenNext = 2
        var property by observable(givenInitial)
                .filter(mockPredicate)
                .doOnNext(mockDoOnNext)


        property = givenNext

        verify(mockDoOnNext).invoke(givenNext)
        verifyNoMoreInteractions(mockDoOnNext)
    }

    @Test
    fun `does not propagate values downstream when predicate returns false`() {
        whenever(mockPredicate(any())).thenReturn(false)
        val given = 1
        var property by observable(given)
                .filter(mockPredicate)
                .doOnNext(mockDoOnNext)

        property = given

        verifyZeroInteractions(mockDoOnNext)
    }

    @Test
    fun `propagates values downstream when given value is not null`() {
        val givenNull = null
        val givenNotNull = 2
        var property by observable<Int?>(givenNull)
                .filterNotNull()
                .doOnNext(mockDoOnNext)

        property = givenNotNull

        verify(mockDoOnNext).invoke(givenNotNull)
        verifyNoMoreInteractions(mockDoOnNext)
    }

    @Test
    fun `does not propagate values downstream when next given value is null`() {
        val givenNull = null
        var property by observable<Int?>(givenNull)
                .filterNotNull()
                .doOnNext(mockDoOnNext)

        property = givenNull

        verifyZeroInteractions(mockDoOnNext)
    }

    @Test
    fun `propagates values downstream when given value is not null and predicate returns true`() {
        whenever(mockPredicate(any())).thenReturn(true)
        val givenNull = null
        val givenNotNull = 2
        var property by observable<Int?>(givenNull)
                .filterNotNullWith(mockPredicate)
                .doOnNext(mockDoOnNext)

        property = givenNotNull

        verify(mockPredicate).invoke(givenNotNull)
        verify(mockDoOnNext).invoke(givenNotNull)
        verifyNoMoreInteractions(mockDoOnNext)
    }

    @Test
    fun `does not propagate values downstream when given value is not null and predicate returns false`() {
        whenever(mockPredicate(any())).thenReturn(false)
        val givenNull = null
        val givenNotNull = 2
        var property by observable<Int?>(givenNull)
                .filterNotNullWith(mockPredicate)
                .doOnNext(mockDoOnNext)

        property = givenNotNull

        verify(mockPredicate).invoke(givenNotNull)
        verifyZeroInteractions(mockDoOnNext)
    }

    @Test
    fun `does not propagate values downstream when given value is null`() {
        whenever(mockPredicate(any())).thenReturn(true)
        val givenNull = null
        var property by observable<Int?>(givenNull)
                .filterNotNullWith(mockPredicate)
                .doOnNext(mockDoOnNext)

        property = givenNull

        verifyZeroInteractions(mockDoOnNext, mockPredicate)
    }
}