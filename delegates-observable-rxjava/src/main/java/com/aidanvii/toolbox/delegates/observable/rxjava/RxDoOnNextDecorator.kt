package com.aidanvii.toolbox.delegates.observable.rxjava

import io.reactivex.Observable
import kotlin.reflect.KProperty

/**
 * Returns an [RxObservableProperty] that calls the given [doOnNext] action
 * with the item emitted by the receiver [RxObservableProperty].
 * @param doOnNext an action that is invoked with each item emitted by the receiver [RxObservableProperty].
 * @param ST the base type of the source observable ([RxObservableProperty.SourceTransformer]).
 * @param TT the type on which this [RxObservableProperty] operates.
 */
infix fun <ST, TT> RxObservableProperty<ST, TT>.doOnNext(
        doOnNext: (value: TT) -> Unit
) = RxDoOnNextDecorator(this, { _, newValue -> doOnNext(newValue) })

/**
 * Returns an [RxObservableProperty] that calls the given [doOnNext] action
 * with the item emitted by the receiver [RxObservableProperty].
 * @param doOnNext an action that is invoked with each item emitted by the receiver [RxObservableProperty].
 * @param ST the base type of the source observable ([RxObservableProperty.SourceTransformer]).
 * @param TT the type on which this [RxObservableProperty] operates.
 */
infix fun <ST, TT> RxObservableProperty<ST, TT>.doOnNextWithPrevious(
        doOnNext: (oldValue: TT?, newValue: TT) -> Unit
) = RxDoOnNextDecorator(this, doOnNext)

class RxDoOnNextDecorator<ST, TT>(
        private val decorated: RxObservableProperty<ST, TT>,
        private val doOnNext: (oldValue: TT?, newValue: TT) -> Unit
) : RxObservableProperty<ST, TT> by decorated {

    override val observable: Observable<RxObservableProperty.ValueContainer<TT>>
        get() = decorated.observable.doOnNext { doOnNext(it.oldValue, it.newValue) }

    operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): RxDoOnNextDecorator<ST, TT> {
        onProvideDelegate(thisRef, property)
        subscribe(observable)
        return this
    }
}