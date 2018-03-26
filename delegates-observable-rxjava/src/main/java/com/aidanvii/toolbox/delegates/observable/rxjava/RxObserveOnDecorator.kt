package com.aidanvii.toolbox.delegates.observable.rxjava

import com.aidanvii.toolbox.delegates.observable.AfterChange
import com.aidanvii.toolbox.delegates.observable.ObservableProperty
import io.reactivex.Observable
import io.reactivex.Scheduler
import kotlin.jvm.Volatile
import kotlin.reflect.KProperty

/**
 * Returns an [RxObservableProperty] that skips the first [count] items emitted
 * by the receiver [RxObservableProperty] and emits the remainder
 * @param ST the base type of the source observable ([RxObservableProperty.SourceTransformer]).
 * @param TT the type on which this [RxObservableProperty] operates.
 */
fun <ST, TT> RxObservableProperty<ST, TT>.observeOn(scheduler: Scheduler) = ObserveOnDecorator(this, scheduler)

class ObserveOnDecorator<ST, TT>(
        private val decorated: RxObservableProperty<ST, TT>,
        private val scheduler: Scheduler
) : RxObservableProperty<ST, TT> by decorated {

    override val observable: Observable<RxObservableProperty.ValueContainer<TT>>
        get() = decorated.observable.observeOn(scheduler)

    operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): ObserveOnDecorator<ST, TT> {
        onProvideDelegate(thisRef, property)
        subscribe(observable)
        return this
    }
}