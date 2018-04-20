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

    private val cleared = AtomicBoolean(false)

    init {
        initDelegator(this)
    }

    fun clear() {
        if (!cleared.getAndSet(true)) {
            onCleared()
        }
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    protected open fun onCleared() {
    }
}