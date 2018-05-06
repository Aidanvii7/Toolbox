@file:Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE", "UNUSED_VALUE")

package com.aidanvii.toolbox.rxutils

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import io.reactivex.disposables.Disposable
import org.junit.jupiter.api.Test

class DisposablePropertyTest {

    val mockDisposable1 = mock<Disposable>()
    val mockDisposable2 = mock<Disposable>()

    @Test
    fun `does nothing to internal disposable on re-assignment when current disposable is null`() {
        var disposable by disposable(null)

        disposable = mockDisposable1

        verifyZeroInteractions(mockDisposable1)
    }

    @Test
    fun `disposes internal disposable on re-assignment with non-null value when internal disposable is non-null`() {
        var disposable by disposable(mockDisposable1)

        disposable = mockDisposable2

        verify(mockDisposable1).dispose()
        verifyZeroInteractions(mockDisposable2)
    }

    @Test
    fun `disposes internal disposable on re-assignment with null value when internal disposable is non-null`() {
        var disposable by disposable(mockDisposable1)

        disposable = null

        verify(mockDisposable1).dispose()
    }
}