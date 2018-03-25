package com.aidanvii.toolbox.databinding

import android.arch.lifecycle.ViewModel
import com.aidanvii.toolbox.leakingThis

/**
 * A convenience [ViewModel] class that implements [NotifiableObservable]
 * and extends [ViewModel] from the architecture components library.
 *
 * Intended to be used with the [bindable] property delegate for data-binding.
 */
@Suppress(leakingThis)
abstract class ObservableArchViewModel : ViewModel(), NotifiableObservable by NotifiableObservable.delegate() {
    init {
        initDelegator(this)
    }
}