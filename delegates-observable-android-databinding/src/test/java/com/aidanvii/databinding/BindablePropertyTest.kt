package com.aidanvii.databinding

import android.databinding.Bindable
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import org.junit.Test


class BindablePropertyTest {

    init {
        PropertyMapper.initBRClass(TestBR::class.java)
    }

    val mockDelegate = mock<NotifiableObservable>()

    @Test
    fun `notifyPropertyChanged not called when bindable property is assigned same value`() {
        val initial = 1
        val tested = TestNotifiableObservable(initialValue1 = initial)

        tested.property1 = initial

        verify(mockDelegate, never()).notifyPropertyChanged(any())
        verify(mockDelegate, never()).notifyChange()
    }

    @Test
    fun `notifyPropertyChanged called when bindable property is assigned different value`() {
        val initial = 1
        val next = 2
        val tested = TestNotifiableObservable(initialValue1 = initial)

        tested.property1 = next

        verify(mockDelegate).notifyPropertyChanged(TestBR.property1)
        verify(mockDelegate, never()).notifyChange()
        verifyNoMoreInteractions(mockDelegate)
    }

    @Test
    fun `notifyPropertyChanged called whenever bindableEvent property is assigned different value`() {
        val initial = 1
        val next = 2
        val tested = TestNotifiableObservable(initialValue2 = initial)

        tested.property2 = next

        verify(mockDelegate).notifyPropertyChanged(TestBR.property2)
        verify(mockDelegate, never()).notifyChange()
    }

    @Test
    fun `notifyPropertyChanged called whenever bindableEvent property is assigned same value`() {
        val initial = 1
        val next = 1
        val tested = TestNotifiableObservable(initialValue2 = initial)

        tested.property2 = next

        verify(mockDelegate).notifyPropertyChanged(TestBR.property2)
        verify(mockDelegate, never()).notifyChange()
    }

    inner class TestNotifiableObservable(
            initialValue1: Int = 1,
            initialValue2: Int = 2
    ) : NotifiableObservable by mockDelegate {

        @get:Bindable
        var property1 by bindable(initialValue1)

        @get:Bindable
        var property2 by bindableEvent(initialValue2)
    }
}