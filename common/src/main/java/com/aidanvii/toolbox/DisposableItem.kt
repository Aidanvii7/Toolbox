package com.aidanvii.toolbox

import androidx.annotation.RestrictTo
import android.util.Log
import java.util.concurrent.atomic.AtomicBoolean

interface DisposableItem {

    @get:RestrictTo(RestrictTo.Scope.LIBRARY)
    val _disposed: AtomicBoolean

    val disposed: Boolean get() = _disposed.get()

    /**
     * Dispose the [ObservableViewModel], this operation is idempotent
     */
    fun dispose() {
        if (!_disposed.getAndSet(true)) {
            onDisposed()
        }
    }

    /**
     * Called when the [DisposableItem] is disposed (when [dispose] is called for the first time).
     *
     * You should not call this directly as it should be idempotent.
     * Instead override it if needed and call [disposed], ensuring idempotent behavior.
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    fun onDisposed() {
    }
}