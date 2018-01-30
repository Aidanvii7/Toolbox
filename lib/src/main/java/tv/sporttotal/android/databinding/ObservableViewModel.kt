package tv.sporttotal.android.databinding

import android.support.annotation.RestrictTo

/**
 * A convenience view-model class that implements [NotifiableObservable].
 */
// TODO extend viewmodel from archcomponent
open class ObservableViewModel :  NotifiableObservable by NotifiableObservable.delegate() {

    init {
        initDelegator(this)
    }
}
