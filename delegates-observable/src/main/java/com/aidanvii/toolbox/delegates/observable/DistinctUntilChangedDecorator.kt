package com.aidanvii.toolbox.delegates.observable

import kotlin.reflect.KProperty

/**
 * Returns an [ObservableProperty] that will only forward items downstream
 * when a new item is set that is different from the previous item.
 * @param ST the base type of the source observable ([ObservableProperty.Source]).
 * @param TT the type on which this [ObservableProperty] operates.
 */
fun <ST, TT> ObservableProperty<ST, TT>.distinctUntilChanged() = DistinctUntilChangedDecorator(this, Functions.areEqual)

/**
 * Returns an [ObservableProperty] that will only forward items downstream
 * when a new item is set that is different from the previous item.
 * @param areEqual a function that receives the previous item and the current item and is
 * expected to return true if the two are equal, thus skipping the current value.
 * @param ST the base type of the source observable ([ObservableProperty.Source]).
 * @param TT the type on which this [ObservableProperty] operates.
 */
fun <ST, TT> ObservableProperty<ST, TT>.distinctUntilChanged(
        areEqual: (oldValue: TT, newValue: TT) -> Boolean
) = DistinctUntilChangedDecorator(this, areEqual)

class DistinctUntilChangedDecorator<ST, TT>(
        private val decorated: ObservableProperty<ST, TT>,
        private val areEqual: ((oldValue: TT, newValue: TT) -> Boolean)
) : ObservableProperty<ST, TT> by decorated {

    override val afterChangeObservers = mutableSetOf<AfterChange<TT>>()

    init {
        decorated.afterChangeObservers += { property, oldValue, newValue ->
            if (oldValue == null || !areEqual(oldValue, newValue)) {
                afterChangeObservers.forEach { it(property, oldValue, newValue) }
            }
        }
    }

    operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): DistinctUntilChangedDecorator<ST, TT> {
        onProvideDelegate(thisRef, property)
        return this
    }
}