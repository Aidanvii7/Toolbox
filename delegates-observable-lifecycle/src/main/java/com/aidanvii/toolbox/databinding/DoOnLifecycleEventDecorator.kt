package com.aidanvii.toolbox.databinding

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleOwner
import com.aidanvii.toolbox.delegates.observable.ObservableProperty
import com.aidanvii.toolbox.delegates.observable.doOnNext
import kotlin.reflect.KProperty

fun <ST, TT> ObservableProperty<ST, TT>.doOnLifecycleEvent(
    lifecycleEvent: Lifecycle.Event,
    lifecycleOwner: LifecycleOwner,
    doOnLifecycleEvent: (value: TT) -> Unit
) = DoOnLifecycleEventDecorator(
    lifecycleEvent = lifecycleEvent,
    lifecycleOwner = lifecycleOwner,
    decorated = this,
    doOnLifecycleEvent = doOnLifecycleEvent
)

class DoOnLifecycleEventDecorator<ST, TT>(
    private val lifecycleEvent: Lifecycle.Event,
    lifecycleOwner: LifecycleOwner,
    decorated: ObservableProperty<ST, TT>,
    private val doOnLifecycleEvent: (value: TT) -> Unit
) : LifecycleDecorator<ST, TT>(lifecycleOwner, decorated) {

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

