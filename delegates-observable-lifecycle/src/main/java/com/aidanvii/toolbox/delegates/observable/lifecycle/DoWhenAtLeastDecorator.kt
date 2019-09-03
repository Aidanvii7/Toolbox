package com.aidanvii.toolbox.delegates.observable.lifecycle

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.aidanvii.toolbox.delegates.observable.ObservableProperty
import kotlin.reflect.KProperty

fun <ST, TT> ObservableProperty<ST, TT>.doWhenAtLeast(
    lifecycleState: Lifecycle.State,
    lifecycle: Lifecycle,
    doWhenAtLeast: (value: TT) -> Unit
) = DoWhenAtLeastDecorator(
    decorated = this,
    lifecycleState = lifecycleState,
    lifecycle = lifecycle,
    doWhenAtLeast = doWhenAtLeast
)

class DoWhenAtLeastDecorator<ST, TT>(
    decorated: ObservableProperty<ST, TT>,
    private val lifecycleState: Lifecycle.State,
    lifecycle: Lifecycle,
    private val doWhenAtLeast: (value: TT) -> Unit
) : LifecycleDecorator<ST, TT>(decorated, lifecycle) {

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

