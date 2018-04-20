package com.aidanvii.toolbox.databinding

import android.support.annotation.RestrictTo
import com.aidanvii.toolbox.leakingThis
import java.util.concurrent.atomic.AtomicBoolean

/**
 * A convenience [ViewModel] class that implements [NotifiableObservable]
 *
 * Intended to be used with the [bindable] property delegate for data-binding.
 */
@Suppress(leakingThis)
abstract class ObservableViewModel : NotifiableObservable by NotifiableObservable.delegate() {

    private val disposed = AtomicBoolean(false)

    val isDisposed get() = disposed.get()

    init {
        initDelegator(this)
    }

    /**
     * Dispose the [ObservableViewModel], this operation is idempotent
     */
    fun dispose() {
        if (!disposed.getAndSet(true)) {
            onDisposed()
        }
    }

    /**
     * Called when the [ObservableViewModel] is disposed (when [dispose] is called for the first time).
     *
     * You should not call this directly as it should be idempotent.
     * Instead override it if needed and call [disposed], ensuring idempotent behavior.
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    protected open fun onDisposed() {
    }
}