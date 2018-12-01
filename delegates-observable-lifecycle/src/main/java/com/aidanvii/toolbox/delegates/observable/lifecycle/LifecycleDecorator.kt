package com.aidanvii.toolbox.delegates.observable.lifecycle

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.aidanvii.toolbox.arch.lifecycle.DefaultLifecycleObserver
import com.aidanvii.toolbox.delegates.observable.AfterChange
import com.aidanvii.toolbox.delegates.observable.ObservableProperty

abstract class LifecycleDecorator<ST, TT>(
    private val decorated: ObservableProperty<ST, TT>,
    lifecycle: Lifecycle
) : ObservableProperty<ST, TT> by decorated, DefaultLifecycleObserver {

    protected var latestValue: TT? = null

    init {
        lifecycle.addObserver(this)
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