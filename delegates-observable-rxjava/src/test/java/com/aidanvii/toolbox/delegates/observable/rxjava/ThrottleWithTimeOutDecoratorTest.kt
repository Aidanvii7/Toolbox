package com.aidanvii.toolbox.delegates.observable.rxjava

import com.aidanvii.toolbox.delegates.observable.eager
import com.aidanvii.toolbox.delegates.observable.observable
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import io.reactivex.schedulers.TestScheduler
import org.amshove.kluent.mock
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit

class ThrottleWithTimeOutDecoratorTest {

    val timeout = 1500L
    val timeUnit = TimeUnit.MILLISECONDS
    val testScheduler = TestScheduler()
    val initialValue = 1337

    val mockDoOnNext = mock<(propertyEvent: Int) -> Unit>()

    @Nested
    inner class `when given initial value` {

        var property by observable(initialValue)
            .eager()
            .toRx()
            .throttleWithTimeOut(
                timeout = timeout,
                timeUnit = timeUnit,
                scheduler = testScheduler
            )
            .doOnNext { mockDoOnNext(it) }

        @Test
        fun `value is not propagated downstream yet`() {
            verifyZeroInteractions(mockDoOnNext)
        }

        @Nested
        inner class `when time window has elapsed` {

            init {
                testScheduler.advanceTimeBy(timeout, timeUnit)
            }

            @Test
            fun `value is propagated downstream`() {
                verify(mockDoOnNext).invoke(initialValue)
            }
        }
    }
}