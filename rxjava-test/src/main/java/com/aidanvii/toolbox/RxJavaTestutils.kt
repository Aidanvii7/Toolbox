package com.aidanvii.toolbox


import com.nhaarman.mockito_kotlin.mock
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.functions.Action
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers.trampoline
import io.reactivex.subjects.PublishSubject
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

fun beginSchedulerPluginPreparation(): RxPluginSchedulerHelper = RxPluginSchedulerHelper()

fun resetSchedulerPlugins() {
    RxAndroidPlugins.reset()
    RxJavaPlugins.reset()
}

fun mockRxAction() = mock<Action>()

fun <T> unfinishedObservable(doOnDispose: Action): Observable<T> = PublishSubject.create<T>().doOnDispose(doOnDispose)

class RxPluginSchedulerHelper {

    fun prepareAll() = prepareMain().prepareComputation().prepareIO().prepareSingle()

    fun prepareMain(mockScheduler: Boolean = false) = prepareMainWith(if (mockScheduler) mock() else trampoline())
    fun prepareComputation(mockScheduler: Boolean = false) = prepareComputationWith(if (mockScheduler) mock() else trampoline())
    fun prepareIO(mockScheduler: Boolean = false) = prepareIoWith(if (mockScheduler) mock() else trampoline())
    fun prepareSingle(mockScheduler: Boolean = false) = prepareSingleWith(if (mockScheduler) mock() else trampoline())

    fun prepareMainWith(scheduler: Scheduler) = this.also {
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { trampoline() }
        RxAndroidPlugins.setMainThreadSchedulerHandler { scheduler }
    }

    fun prepareComputationWith(scheduler: Scheduler) = this.also { RxJavaPlugins.setComputationSchedulerHandler { scheduler } }
    fun prepareIoWith(scheduler: Scheduler) = this.also { RxJavaPlugins.setIoSchedulerHandler { scheduler } }
    fun prepareSingleWith(scheduler: Scheduler) = this.also { RxJavaPlugins.setSingleSchedulerHandler { scheduler } }
}

private class RxPluginRule(private val function: RxPluginSchedulerHelper.() -> Unit) : TestRule {

    override fun apply(base: Statement, description: Description?) =
            object : Statement() {
                @Throws(Throwable::class)
                override fun evaluate() {
                    function.invoke(RxPluginSchedulerHelper())
                    try {
                        base.evaluate()
                    } finally {
                        resetSchedulerPlugins()
                    }
                }
            }
}

fun rxSchedulers(function: RxPluginSchedulerHelper.() -> Unit): TestRule = RxPluginRule(function)