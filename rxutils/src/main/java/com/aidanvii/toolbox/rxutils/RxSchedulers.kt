package com.aidanvii.toolbox.rxutils

import androidx.annotation.RestrictTo
import androidx.annotation.VisibleForTesting
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.schedulers.TestScheduler
import java.util.concurrent.TimeUnit

sealed class RxSchedulers(
    val subscribeOn: Scheduler,
    val observeOn: Scheduler,
    val single: Scheduler,
    val io: Scheduler,
    val computation: Scheduler,
    val main: Scheduler
) {

    class DefaultSchedulers : RxSchedulers(
        subscribeOn = Schedulers.io(),
        observeOn = AndroidSchedulers.mainThread(),
        io = Schedulers.io(),
        single = Schedulers.single(),
        computation = Schedulers.computation(),
        main = AndroidSchedulers.mainThread()
    )

    @VisibleForTesting
    class TrampolineSchedulers : RxSchedulers(
        subscribeOn = Schedulers.trampoline(),
        observeOn = Schedulers.trampoline(),
        single = Schedulers.trampoline(),
        io = Schedulers.trampoline(),
        computation = Schedulers.trampoline(),
        main = Schedulers.trampoline()
    )

    @VisibleForTesting
    class TestSchedulers : RxSchedulers(
        subscribeOn = TestScheduler(),
        observeOn = TestScheduler(),
        single = TestScheduler(),
        io = TestScheduler(),
        computation = TestScheduler(),
        main = TestScheduler()
    )
}

@RestrictTo(RestrictTo.Scope.TESTS)
fun RxSchedulers.TestSchedulers.flush() {
    (subscribeOn as TestScheduler).triggerActions()
    (observeOn as TestScheduler).triggerActions()
    (single as TestScheduler).triggerActions()
    (io as TestScheduler).triggerActions()
    (computation as TestScheduler).triggerActions()
    (main as TestScheduler).triggerActions()
}

@RestrictTo(RestrictTo.Scope.TESTS)
fun Scheduler.flush() {
    (this as TestScheduler).triggerActions()
}

@RestrictTo(RestrictTo.Scope.TESTS)
fun Scheduler.advanceTimeBy(delayTime: Long, timeUnit: TimeUnit) {
    (this as TestScheduler).advanceTimeBy(delayTime, timeUnit)
}