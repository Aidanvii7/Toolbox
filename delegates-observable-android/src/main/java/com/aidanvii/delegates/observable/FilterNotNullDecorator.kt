package com.aidanvii.delegates.observable

import kotlin.reflect.KProperty

/**
 * Returns an [ObservableProperty] that will only forward change events to subsequent property delegate
 * decorators when the value is not null.
 * @param ST the base type of the source observable ([ObservableProperty.Source]).
 * @param TT the type on which this [ObservableProperty] operates.
 */
fun <ST, TT> ObservableProperty<ST, TT?>.filterNotNull() = FilterNotNullDecorator(this, Functions.alwaysTrue)

/**
 * Returns an [ObservableProperty] that will only forward change events to subsequent property delegate
 * decorators when the value is not null and the given [predicate] is returns true.
 * @param predicate a function that evaluates each item emitted by the receiver [ObservableProperty],
 * returning true if it passes the filter
 * @param ST the base type of the source observable ([ObservableProperty.Source]).
 * @param TT the type on which this [ObservableProperty] operates.
 */
fun <ST, TT> ObservableProperty<ST, TT?>.filterNotNullWith(
        predicate: (TT) -> Boolean
) = FilterNotNullDecorator(this, Functions.forceUnwrap(predicate))

class FilterNotNullDecorator<ST, TT>(
        private val decorated: ObservableProperty<ST, TT?>,
        private val predicate: (TT?) -> Boolean
) : ObservableProperty<ST, TT> {

    init {
        decorated.afterChangeObservers += { property, oldValue, newValue ->
            if (newValue != null && predicate(newValue)) {
                afterChangeObservers.forEach { it(property, oldValue, newValue) }
            }
        }
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>) =
            decorated.getValue(thisRef, property)

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: ST) {
        decorated.setValue(thisRef, property, value)
    }

    override val sourceValue: ST
        get() = decorated.sourceValue

    override val source: ObservableProperty.Source<ST>
        get() = decorated.source

    override val afterChangeObservers = mutableSetOf<AfterChange<TT>>()

    operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): FilterNotNullDecorator<ST, TT> {
        onProvideDelegate(thisRef, property)
        return this
    }
}