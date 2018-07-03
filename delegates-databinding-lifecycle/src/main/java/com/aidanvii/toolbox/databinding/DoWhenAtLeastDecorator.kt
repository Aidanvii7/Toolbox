package com.aidanvii.toolbox.databinding

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleOwner
import com.aidanvii.toolbox.delegates.observable.ObservableProperty
import com.aidanvii.toolbox.delegates.observable.doOnNext
import kotlin.reflect.KProperty

fun <ST, TT> ObservableProperty<ST, TT>.doWhenAtLeast(
    lifecycleState: Lifecycle.State,
    lifecycleOwner: LifecycleOwner,
    doWhenAtLeast: (value: TT) -> Unit
) = DoWhenAtLeastDecorator(
    lifecycleState = lifecycleState,
    lifecycleOwner = lifecycleOwner,
    decorated = this,
    doWhenAtLeast = doWhenAtLeast
)

class DoWhenAtLeastDecorator<ST, TT>(
    private val lifecycleState: Lifecycle.State,
    lifecycleOwner: LifecycleOwner,
    decorated: ObservableProperty<ST, TT>,
    private val doWhenAtLeast: (value: TT) -> Unit
) : LifecycleDecorator<ST, TT>(lifecycleOwner, decorated) {

    override fun onLifeCycleEvent(state: Lifecycle.State, event: Lifecycle.Event) {
        latestValue?.let { latestValue ->
            if (state.isAtLeast(lifecycleState)) {
                doWhenAtLeast(latestValue)
            }
        }
    }

    operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): DoWhenAtLeastDecorator<ST, TT> {
        onProvideDelegate(thisRef, property)
        return this
    }
}

