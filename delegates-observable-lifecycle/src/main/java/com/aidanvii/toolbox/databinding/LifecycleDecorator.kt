package com.aidanvii.toolbox.databinding

import android.arch.lifecycle.DefaultLifecycleObserver
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleOwner
import com.aidanvii.toolbox.delegates.observable.AfterChange
import com.aidanvii.toolbox.delegates.observable.ObservableProperty

abstract class LifecycleDecorator<ST, TT>(
    lifecycleOwner: LifecycleOwner,
    private val decorated: ObservableProperty<ST, TT>
) : ObservableProperty<ST, TT> by decorated, DefaultLifecycleObserver {

    protected var latestValue: TT? = null

    init {
        lifecycleOwner.lifecycle.addObserver(this)
        decorated.afterChangeObservers += { property, oldValue, newValue ->
            latestValue = newValue
            afterChangeObservers.forEach { it(property, oldValue, newValue) }
        }
    }

    override val afterChangeObservers = mutableSetOf<AfterChange<TT>>()

    override fun onCreate(owner: LifecycleOwner) = onLifeCycleEvent(
        state = owner.lifecycle.currentState,
        event = Lifecycle.Event.ON_CREATE
    )

    override fun onResume(owner: LifecycleOwner) = onLifeCycleEvent(
        state = owner.lifecycle.currentState,
        event = Lifecycle.Event.ON_RESUME
    )

    override fun onStart(owner: LifecycleOwner) = onLifeCycleEvent(
        state = owner.lifecycle.currentState,
        event = Lifecycle.Event.ON_START
    )

    override fun onPause(owner: LifecycleOwner) = onLifeCycleEvent(
        state = owner.lifecycle.currentState,
        event = Lifecycle.Event.ON_PAUSE
    )

    override fun onStop(owner: LifecycleOwner) = onLifeCycleEvent(
        state = owner.lifecycle.currentState,
        event = Lifecycle.Event.ON_STOP
    )

    override fun onDestroy(owner: LifecycleOwner) = onLifeCycleEvent(
        state = owner.lifecycle.currentState,
        event = Lifecycle.Event.ON_DESTROY
    ).also {
        owner.lifecycle.removeObserver(this)
    }

    abstract fun onLifeCycleEvent(state: Lifecycle.State, event: Lifecycle.Event)
}