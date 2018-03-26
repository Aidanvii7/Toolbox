package com.aidanvii.toolbox.delegates.observable.rxjava

import io.reactivex.Observable
import kotlin.reflect.KProperty

/**
 * Returns an [RxObservableProperty] that will only forward change events to subsequent property delegate
 * decorators when the given [predicate] returns true.
 * @param predicate a function that evaluates each item emitted by the receiver [RxObservableProperty],
 * returning true if it passes the filter
 * @param ST the base type of the source observable ([RxObservableProperty.SourceTransformer]).
 * @param TT the type on which this [RxObservableProperty] operates.
 */
infix fun <ST, TT> RxObservableProperty<ST, TT>.filter(
        predicate: (TT) -> Boolean
) = RxFilterDecorator(this, predicate)

class RxFilterDecorator<ST, TT>(
        private val decorated: RxObservableProperty<ST, TT>,
        private val predicate: (TT) -> Boolean
) : RxObservableProperty<ST, TT> by decorated {

    override val observable: Observable<RxObservableProperty.ValueContainer<TT>>
        get() = decorated.observable.filter { predicate.invoke(it.newValue) }

    operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): RxFilterDecorator<ST, TT> {
        onProvideDelegate(thisRef, property)
        subscribe(observable)
        return this
    }
}