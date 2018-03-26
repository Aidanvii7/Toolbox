package com.aidanvii.toolbox.delegates.observable.rxjava

import com.aidanvii.toolbox.assignedButNeverAccessed
import com.aidanvii.toolbox.unusedValue
import com.aidanvii.toolbox.unusedVariable
import com.nhaarman.mockito_kotlin.any as kAny
import com.nhaarman.mockito_kotlin.spy
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Observable
import io.reactivex.Scheduler
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should equal`
import org.amshove.kluent.mock
import org.junit.Test

@Suppress(assignedButNeverAccessed, unusedValue, unusedVariable)
class RxObserveOnDecoratorTest {

    @Test
    fun `observes on the given scheduler`() {
        val expectedScheduler = mock<Scheduler>()
        val expectedObserveOnObservable = mock<Observable<RxObservableProperty.ValueContainer<Int>>>()
        val mockSourceObservable = mock<Observable<RxObservableProperty.ValueContainer<Int>>>().apply {
            whenever(observeOn(kAny())).thenReturn(expectedObserveOnObservable)
        }
        val mockSourceDelegate = mock<RxObservableProperty<Int, Int>>().apply {
            whenever(observable).thenReturn(mockSourceObservable)
        }
        val spyObserveDelegate = spy(mockSourceDelegate.observeOn(expectedScheduler))

        val actualObserveOnObservable = spyObserveDelegate.observable

        verify(mockSourceObservable).observeOn(expectedScheduler)
        actualObserveOnObservable `should be` expectedObserveOnObservable
    }
}