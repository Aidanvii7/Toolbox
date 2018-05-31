package com.aidanvii.toolbox.delegates.observable.rxjava

import com.aidanvii.toolbox.Action
import com.aidanvii.toolbox.Consumer
import com.aidanvii.toolbox.delegates.observable.doOnNext
import com.aidanvii.toolbox.delegates.observable.observable
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.spy
import com.nhaarman.mockito_kotlin.verify
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import org.amshove.kluent.any
import org.amshove.kluent.mock
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SubscribeDecoratorTest {

    @Nested
    inner class `When initialised` {

        val initialValue = 1337
        val mockDoOnNext = mock<Consumer<Int>>()
        val mockOnError = mock<Consumer<Throwable>>()
        val mockOnComplete = mock<Action>()
        val mockCompositeDisposable = mock<CompositeDisposable>()
        val spySubject = spy(BehaviorSubject.createDefault(initialValue))

        val property by observable(initialValue)
            .subscribeTo(
                observable = spySubject,
                compositeDisposable = mockCompositeDisposable,
                onError = mockOnError,
                onComplete = mockOnComplete
            )
            .doOnNext(mockDoOnNext)

        @Test
        fun `subscribes to given Observable`() {
            // TODO use argument captor here (kotlin magic going on under the hood that wraps kotlin functions in other SAM interfaces)
            verify(spySubject).subscribe(any(), any(), any())
        }

        @Test
        fun `doOnNext is invoked with initialValue`() {
            verify(mockDoOnNext).invoke(initialValue)
        }

        @Test
        fun `disposable is added to given CompositeDisposable`() {
            verify(mockCompositeDisposable).add(any())
        }

        @Nested
        inner class `When given Observable completes` {

            init {
                spySubject.onComplete()
            }

            @Test
            fun `given onComplete function is invoked`() {
                verify(mockOnComplete).invoke()
            }

            @Test
            fun `new values are not observed`() {
                val givenNewValue = 7331

                spySubject.onNext(givenNewValue)

                verify(mockDoOnNext, never()).invoke(givenNewValue)
            }
        }

        @Nested
        inner class `When given Observable errors` {

            val givenError = object : Throwable() {}

            init {
                spySubject.onError(givenError)
            }

            @Test
            fun `given onError function is invoked with expected error`() {
                verify(mockOnError).invoke(givenError)
            }

            @Test
            fun `new values are not observed`() {
                val givenNewValue = 7331

                spySubject.onNext(givenNewValue)

                verify(mockDoOnNext, never()).invoke(givenNewValue)
            }
        }
    }
}