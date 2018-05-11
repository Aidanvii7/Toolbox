package com.aidanvii.toolbox.delegates.observable.rxjava

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import kotlin.reflect.KProperty

/**
 * Returns an [RxObservableProperty] that only emits those items emitted by the source [RxObservableProperty]
 * that are not followed by another emitted item within a specified time window, where the time window is governed by a specified Scheduler.
 * see [Observable.throttleWithTimeout] for more information.
 * @param ST the base type of the source observable ([RxObservableProperty.SourceTransformer]).
 * @param TT the type on which this [RxObservableProperty] operates.
 */
fun <ST, TT> RxObservableProperty<ST, TT>.throttleWithTimeOut(
    timeout: Long,
    timeUnit: TimeUnit,
    scheduler: Scheduler = Schedulers.computation()
) = ThrottleWithTimeOutDecorator(this, timeout, timeUnit, scheduler)

class ThrottleWithTimeOutDecorator<ST, TT>(
    private val decorated: RxObservableProperty<ST, TT>,
    private val timeout: Long,
    private val timeUnit: TimeUnit,
    private val scheduler: Scheduler
) : RxObservableProperty<ST, TT> by decorated {

    override val observable: Observable<RxObservableProperty.ValueContainer<TT>>
        get() = decorated.observable.throttleWithTimeout(timeout, timeUnit, scheduler)

    operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): ThrottleWithTimeOutDecorator<ST, TT> {
        onProvideDelegate(thisRef, property)
        subscribe(observable)
        return this
    }
}