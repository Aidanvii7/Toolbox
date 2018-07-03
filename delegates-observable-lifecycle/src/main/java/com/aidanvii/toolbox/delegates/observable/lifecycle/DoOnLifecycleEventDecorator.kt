package com.aidanvii.toolbox.delegates.observable.lifecycle

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleOwner
import com.aidanvii.toolbox.delegates.observable.ObservableProperty
import kotlin.reflect.KProperty

fun <ST, TT> ObservableProperty<ST, TT>.doOnLifecycleEvent(
    lifecycleEvent: Lifecycle.Event,
    lifecycle: Lifecycle,
    doOnLifecycleEvent: (value: TT) -> Unit
) = DoOnLifecycleEventDecorator(
    decorated = this,
    lifecycleEvent = lifecycleEvent,
    lifecycle = lifecycle,
    doOnLifecycleEvent = doOnLifecycleEvent
)

class DoOnLifecycleEventDecorator<ST, TT>(
    decorated: ObservableProperty<ST, TT>,
    private val lifecycleEvent: Lifecycle.Event,
    lifecycle: Lifecycle,
    private val doOnLifecycleEvent: (value: TT) -> Unit
) : LifecycleDecorator<ST, TT>(decorated, lifecycle) {

    override fun onLifeCycleEvent(state: Lifecycle.State, event: Lifecycle.Event) {
        latestValue?.let { latestValue ->
            if (event == lifecycleEvent) {
                doOnLifecycleEvent(latestValue)
            }
        }
    }

    operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): DoOnLifecycleEventDecorator<ST, TT> {
        onProvideDelegate(thisRef, property)
        return this
    }
}

