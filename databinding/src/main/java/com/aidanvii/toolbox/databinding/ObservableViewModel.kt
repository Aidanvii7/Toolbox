package com.aidanvii.toolbox.databinding

import com.aidanvii.toolbox.leakingThis

/**
 * A convenience [ViewModel] class that implements [NotifiableObservable]
 *
 * Intended to be used with the [bindable] property delegate for data-binding.
 */
@Suppress(leakingThis)
abstract class ObservableViewModel : NotifiableObservable by NotifiableObservable.delegate() {
    init {
        initDelegator(this)
    }
}