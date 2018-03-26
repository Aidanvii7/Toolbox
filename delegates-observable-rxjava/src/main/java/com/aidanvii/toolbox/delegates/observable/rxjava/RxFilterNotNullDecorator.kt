package com.aidanvii.toolbox.delegates.observable.rxjava

import android.support.annotation.RestrictTo
import com.aidanvii.toolbox.delegates.observable.Functions
import io.reactivex.Observable
import kotlin.reflect.KProperty

/**
 * Returns an [RxObservableProperty] that will only forward change events to subsequent property delegate
 * decorators when the value is not null.
 * @param ST the base type of the source observable ([RxObservableProperty.SourceTransformer]).
 * @param TT the type on which this [RxObservableProperty] operates.
 */
fun <ST, TT> RxObservableProperty<ST, TT?>.filterNotNull() = RxFilterNotNullDecorator(this, Functions.alwaysTrue)

/**
 * Returns an [RxObservableProperty] that will only forward change events to subsequent property delegate
 * decorators when the value is not null and the given [predicate] is returns true.
 * @param predicate a function that evaluates each item emitted by the receiver [RxObservableProperty],
 * returning true if it passes the filter
 * @param ST the base type of the source observable ([RxObservableProperty.SourceTransformer]).
 * @param TT the type on which this [RxObservableProperty] operates.
 */
fun <ST, TT> RxObservableProperty<ST, TT?>.filterNotNullWith(
        predicate: (TT) -> Boolean
) = RxFilterNotNullDecorator(this, Functions.forceUnwrap(predicate))

class RxFilterNotNullDecorator<ST, TT>(
        private val decorated: RxObservableProperty<ST, TT?>,
        private val predicate: (TT) -> Boolean
) : RxObservableProperty<ST, TT> {

    override val observable: Observable<RxObservableProperty.ValueContainer<TT>>
        get() = decorated.observable
                .filter { it.newValue != null && predicate(it.newValue) }
                .map { RxObservableProperty.ValueContainer(it.property, it.oldValue, it.newValue!!) }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    override fun subscribe(observable: Observable<*>) {
        decorated.subscribe(observable)
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>) =
            decorated.getValue(thisRef, property)

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: ST) {
        decorated.setValue(thisRef, property, value)
    }

    operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): RxFilterNotNullDecorator<ST, TT> {
        onProvideDelegate(thisRef, property)
        subscribe(observable)
        return this
    }
}