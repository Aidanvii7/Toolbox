package com.aidanvii.toolbox.databinding

import android.databinding.Observable
import android.databinding.PropertyChangeRegistry
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.spy
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import org.junit.Test

const val EXPECTED_PROP_ID = 1

class NotifiableObservableDelegateTest {

    val ALL_PROPERTIES = 0

    val spyPropertyChangedRegistry = spy<PropertyChangeRegistry>()
    val mockPropertyChangeCallback = mock<Observable.OnPropertyChangedCallback>()
    val mockDelegator = mock<NotifiableObservable>()

    val tested = NotifiableObservable.delegate(
            LazyThreadSafetyMode.NONE,
            { spyPropertyChangedRegistry }
    ).apply {
        initDelegator(mockDelegator)
    }

    @Test
    fun `addOnPropertyChangedCallback adds callback to PropertyChangeRegistry`() {
        tested.addOnPropertyChangedCallback(mockPropertyChangeCallback)

        verify(spyPropertyChangedRegistry).add(mockPropertyChangeCallback)
    }

    @Test
    fun `With callback added, removeOnPropertyChangedCallback removes callback from PropertyChangeRegistry`() {
        tested.addOnPropertyChangedCallback(mockPropertyChangeCallback)

        tested.removeOnPropertyChangedCallback(mockPropertyChangeCallback)

        verify(spyPropertyChangedRegistry).remove(mockPropertyChangeCallback)
    }

    @Test
    fun `With no callback added, removeOnPropertyChangedCallback doesn't remove callback from PropertyChangeRegistry`() {
        tested.removeOnPropertyChangedCallback(mockPropertyChangeCallback)

        verifyZeroInteractions(spyPropertyChangedRegistry)
    }

    @Test
    fun `notifyPropertyChanged notifies PropertyChangeRegistry with given property ID`() {
        tested.addOnPropertyChangedCallback(mockPropertyChangeCallback)

        tested.notifyPropertyChanged(EXPECTED_PROP_ID)

        verify(spyPropertyChangedRegistry).notifyCallbacks(mockDelegator, EXPECTED_PROP_ID, null)
    }

    @Test
    fun `notifyChanged notifies PropertyChangeRegistry with ALL_PROPERTIES`() {
        tested.addOnPropertyChangedCallback(mockPropertyChangeCallback)

        tested.notifyChange()

        verify(spyPropertyChangedRegistry).notifyCallbacks(mockDelegator, ALL_PROPERTIES, null)
    }
}