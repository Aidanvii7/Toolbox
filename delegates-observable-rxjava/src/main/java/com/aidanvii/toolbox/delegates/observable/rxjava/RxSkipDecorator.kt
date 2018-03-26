package com.aidanvii.toolbox.delegates.observable.rxjava

import io.reactivex.Observable
import kotlin.reflect.KProperty

/**
 * Returns an [RxObservableProperty] that skips the first [count] items emitted
 * by the receiver [RxObservableProperty] and emits the remainder
 * @param ST the base type of the source observable ([RxObservableProperty.SourceTransformer]).
 * @param TT the type on which this [RxObservableProperty] operates.
 */
fun <ST, TT> RxObservableProperty<ST, TT>.skip(count: Int) = RxSkipDecorator(this, count)

class RxSkipDecorator<ST, TT>(
        private val decorated: RxObservableProperty<ST, TT>,
        private val count: Int
) : RxObservableProperty<ST, TT> by decorated {

    override val observable: Observable<RxObservableProperty.ValueContainer<TT>>
        get() = decorated.observable.skip(count.toLong())

    operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): RxSkipDecorator<ST, TT> {
        onProvideDelegate(thisRef, property)
        subscribe(observable)
        return this
    }
}