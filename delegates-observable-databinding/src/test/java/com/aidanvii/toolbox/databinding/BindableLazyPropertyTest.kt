package com.aidanvii.toolbox.databinding

import android.databinding.Bindable
import com.aidanvii.toolbox.Provider
import com.aidanvii.toolbox.spied
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test


class BindableLazyPropertyTest {

    init {
        PropertyMapper.initBRClass(TestBR::class.java)
    }

    val mockDelegate = mock<NotifiableObservable>()

    @Test
    fun `notifyPropertyChanged not called when bindable property is assigned same value`() {
        val initial = 1
        val tested = TestNotifiableObservable(initialValueProvider = { initial })

        tested.property1 = initial

        verify(mockDelegate, never()).notifyPropertyChanged(any())
        verify(mockDelegate, never()).notifyChange()
    }

    @Test
    fun `notifyPropertyChanged called when bindable property is assigned different value`() {
        val initial = 1
        val next = 2
        val tested = TestNotifiableObservable(initialValueProvider = { initial })

        tested.property1 = next

        verify(mockDelegate).notifyPropertyChanged(TestBR.property1)
        verify(mockDelegate, never()).notifyChange()
        verifyNoMoreInteractions(mockDelegate)
    }

    @Test
    fun `initialValueProvider not accessed before property is read from`() {
        val spiedInitialValueProvider = { 1 }.spied()

        val tested = TestNotifiableObservable(spiedInitialValueProvider)

        verifyZeroInteractions(spiedInitialValueProvider)
    }

    @Test
    @Disabled
    fun `initialValueProvider accessed after property is read from for first time`() {
        val spiedInitialValueProvider = { 1 }.spied()
        val tested = TestNotifiableObservable(spiedInitialValueProvider)

        tested.property1

        verify(spiedInitialValueProvider).invoke()
    }

    @Test
    @Disabled
    fun `initialValueProvider not invoked again after property is read from for second time`() {
        val spiedInitialValueProvider = { 1 }.spied()
        val tested = TestNotifiableObservable(spiedInitialValueProvider)

        tested.property1
        tested.property1

        verify(spiedInitialValueProvider).invoke()
    }

    inner class TestNotifiableObservable(
        initialValueProvider: Provider<Int>
    ) : NotifiableObservable by mockDelegate {

        @get:Bindable
        var property1 by bindableLazy(initialValueProvider)
    }
}