package com.aidanvii.toolbox.delegates.observable

import com.aidanvii.toolbox.Action
import kotlin.reflect.KProperty

/**
 * Returns an [ObservableProperty] that invokes the given [onFirstAccess] function lazily
 * when the the property backed by this [ObservableProperty] (or final [ObservableProperty] in the chain)
 * is accessed/read from for the first time.
 * @param threadSafetyMode optional parameter to determine how the [onFirstAccess] function
 * is accessed among multiple threads, default it [LazyThreadSafetyMode.NONE]
 * @param onFirstAccess an action that is invoked the first time the property is accessed/read from.
 * @param ST the base type of the source observable ([ObservableProperty.Source]).
 * @param TT the type on which this [ObservableProperty] operates.
 */
fun <ST, TT> ObservableProperty<ST, TT>.onFirstAccess(
        threadSafetyMode: LazyThreadSafetyMode = LazyThreadSafetyMode.NONE,
        onFirstAccess: Action
) = OnFirstAccessDecorator(this, threadSafetyMode, onFirstAccess)

class OnFirstAccessDecorator<ST, TT>(
        private val decorated: ObservableProperty<ST, TT>,
        threadSafetyMode: LazyThreadSafetyMode,
        onFirstAccess: Action
) : ObservableProperty<ST, TT> by decorated {

    init {
        decorated.afterChangeObservers += { property, oldValue, newValue ->
            afterChangeObservers.forEach { it(property, oldValue, newValue) }
        }
    }

    private val onFirstAccessLazy by kotlin.lazy(threadSafetyMode, onFirstAccess)

    override val afterChangeObservers = mutableSetOf<AfterChange<TT>>()

    override fun getValue(thisRef: Any?, property: KProperty<*>): ST {
        onFirstAccessLazy
        return decorated.getValue(thisRef, property)
    }

    operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): OnFirstAccessDecorator<ST, TT> {
        onProvideDelegate(thisRef, property)
        return this
    }
}