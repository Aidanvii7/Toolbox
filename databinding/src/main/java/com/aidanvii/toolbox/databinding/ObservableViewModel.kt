package com.aidanvii.toolbox.databinding

import com.aidanvii.toolbox.DisposableItem
import com.aidanvii.toolbox.leakingThis
import java.util.concurrent.atomic.AtomicBoolean

/**
 * A convenience [ViewModel] class that implements [NotifiableObservable]
 *
 * Intended to be used with the [bindable] property delegate for data-binding.
 */
@Suppress(leakingThis)
abstract class ObservableViewModel: NotifiableObservable by NotifiableObservable.delegate(), DisposableItem {

    final override val disposed = AtomicBoolean(false)

    init {
        initDelegator(this)
    }

    final override val isDisposed: Boolean
        get() = super.isDisposed

    final override fun dispose() {
        super.dispose()
    }
}