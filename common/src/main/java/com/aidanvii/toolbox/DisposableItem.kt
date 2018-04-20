package com.aidanvii.toolbox

import android.support.annotation.RestrictTo
import java.util.concurrent.atomic.AtomicBoolean

interface DisposableItem {

    @get:RestrictTo(RestrictTo.Scope.LIBRARY)
    val disposed: AtomicBoolean

    val isDisposed: Boolean get() = disposed.get()

    /**
     * Dispose the [ObservableViewModel], this operation is idempotent
     */
    fun dispose() {
        if (!disposed.getAndSet(true)) {
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
    fun onDisposed() {}
}