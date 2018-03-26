package com.aidanvii.toolbox.delegates.observable.rxjava

import com.aidanvii.toolbox.delegates.observable.Functions
import io.reactivex.Observable
import kotlin.reflect.KProperty

/**
 * Returns an [RxObservableProperty] that will only forward items downstream
 * when a new item is set that is different from the previous item.
 * @param ST the base type of the source observable ([RxObservableProperty.SourceTransformer]).
 * @param TT the type on which this [RxObservableProperty] operates.
 */
fun <ST, TT> RxObservableProperty<ST, TT>.distinctUntilChanged() = RxDistinctUntilChangedDecorator(this, Functions.areEqual)

/**
 * Returns an [RxObservableProperty] that will only forward items downstream
 * when a new item is set that is different from the previous item.
 * @param areEqual a function that receives the previous item and the current item and is
 * expected to return true if the two are equal, thus skipping the current value.
 * @param ST the base type of the source observable ([RxObservableProperty.SourceTransformer]).
 * @param TT the type on which this [RxObservableProperty] operates.
 */
fun <ST, TT> RxObservableProperty<ST, TT>.distinctUntilChanged(
        areEqual: (oldValue: TT, newValue: TT) -> Boolean
) = RxDistinctUntilChangedDecorator(this, areEqual)

class RxDistinctUntilChangedDecorator<ST, TT>(
        private val decorated: RxObservableProperty<ST, TT>,
        private val areEqual: ((oldValue: TT, newValue: TT) -> Boolean)
) : RxObservableProperty<ST, TT> by decorated {

    override val observable: Observable<RxObservableProperty.ValueContainer<TT>>
        get() = decorated.observable.filter {
            it.run { oldValue == null || !areEqual(oldValue, newValue) }
        }

    operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): RxDistinctUntilChangedDecorator<ST, TT> {
        onProvideDelegate(thisRef, property)
        subscribe(observable)
        return this
    }
}