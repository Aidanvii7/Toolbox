package com.aidanvii.delegates.observable

import kotlin.reflect.KProperty

/**
 * Returns an [ObservableProperty] that calls the given [doOnNext] action
 * with the item emitted by the receiver [ObservableProperty].
 * @param doOnNext an action that is invoked with each item emitted by the receiver [ObservableProperty].
 * @param ST the base type of the source observable ([ObservableProperty.Source]).
 * @param TT the type on which this [ObservableProperty] operates.
 */
infix fun <ST, TT> ObservableProperty<ST, TT>.doOnNext(
        doOnNext: (value: TT) -> Unit
) = DoOnNextDecorator(this, { _, newValue -> doOnNext(newValue) })

/**
 * Returns an [ObservableProperty] that calls the given [doOnNext] action
 * with the item emitted by the receiver [ObservableProperty].
 * @param doOnNext an action that is invoked with each item emitted by the receiver [ObservableProperty].
 * @param ST the base type of the source observable ([ObservableProperty.Source]).
 * @param TT the type on which this [ObservableProperty] operates.
 */
infix fun <ST, TT> ObservableProperty<ST, TT>.doOnNextWithPrevious(
        doOnNext: (oldValue: TT?, newValue: TT) -> Unit
) = DoOnNextDecorator(this, doOnNext)

class DoOnNextDecorator<ST, TT>(
        private val decorated: ObservableProperty<ST, TT>,
        private val doOnNext: (oldValue: TT?, newValue: TT) -> Unit
) : ObservableProperty<ST, TT> by decorated {

    init {
        decorated.afterChangeObservers += { property, oldValue, newValue ->
            doOnNext.invoke(oldValue, newValue)
            afterChangeObservers.forEach { it(property, oldValue, newValue) }
        }
    }

    override val afterChangeObservers = mutableSetOf<AfterChange<TT>>()

    operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): DoOnNextDecorator<ST, TT> {
        onProvideDelegate(thisRef, property)
        return this
    }
}