package com.aidanvii.delegates.observable

import kotlin.reflect.KProperty

/**
 * Returns an [ObservableProperty] that will only forward change events to subsequent property delegate
 * decorators when the given [predicate] returns true.
 * @param predicate a function that evaluates each item emitted by the receiver [ObservableProperty],
 * returning true if it passes the filter
 * @param ST the base type of the source observable ([ObservableProperty.Source]).
 * @param TT the type on which this [ObservableProperty] operates.
 */
infix fun <ST, TT> ObservableProperty<ST, TT>.filter(
        predicate: (TT) -> Boolean
) = FilterDecorator(this, predicate)

class FilterDecorator<ST, TT>(
        private val decorated: ObservableProperty<ST, TT>,
        private val predicate: (TT) -> Boolean
) : ObservableProperty<ST, TT> by decorated {

    init {
        decorated.afterChangeObservers += { property, oldValue, newValue ->
            if (predicate(newValue)) {
                afterChangeObservers.forEach { it(property, oldValue, newValue) }
            }
        }
    }

    override val afterChangeObservers = mutableSetOf<AfterChange<TT>>()

    operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): FilterDecorator<ST, TT> {
        onProvideDelegate(thisRef, property)
        return this
    }
}