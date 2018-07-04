package com.aidanvii.toolbox.redux

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.spy
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import com.aidanvii.toolbox.redux.ExampleAction.Bar1
import com.aidanvii.toolbox.redux.ExampleAction.Bar2
import com.aidanvii.toolbox.redux.ExampleAction.Foo1
import com.aidanvii.toolbox.redux.ExampleAction.Foo2
import com.aidanvii.toolbox.rxutils.RxSchedulers
import com.aidanvii.toolbox.rxutils.flush
import com.aidanvii.toolbox.spied
import org.amshove.kluent.`should be false`
import org.amshove.kluent.`should be true`

class SideEffectsTest {

    @Nested
    inner class `when initialised` {

        val reducer = spy(ExampleReducer())
        val rxSchedulers = RxSchedulers.TestSchedulers()
        val store = Store.Base(
            reducer = reducer,
            rxSchedulers = rxSchedulers,
            state = ExampleState.DEFAULT
        ).spied()

        val tested = ExampleSideEffects(
            rxSchedulers = rxSchedulers,
            store = store
        ).apply { init() }

        init {
            rxSchedulers.flush()
        }

        @Test
        fun `subscribes to store`() {
            store.hasActionStateObservers.`should be true`()
        }

        @Nested
        inner class `when dispose is called` {

            init {
                tested.dispose()
            }

            @Test
            fun `unsubscribes from store`() {
                store.hasActionStateObservers.`should be false`()
            }
        }

        @Nested
        inner class `when single Foo1 action dispatched to store` {

            init {
                store.dispatch(Foo1)
                rxSchedulers.flush()
            }

            @Test
            fun `passes single Foo1 action to reducer`() {
                verify(reducer).reduce(ExampleState.DEFAULT, Foo1)
            }
        }

        @Nested
        inner class `when multiple Foo1 actions dispatched to store` {

            init {
                store.dispatch(Foo1, Foo1, Foo1)
                rxSchedulers.flush()
            }

            @Test
            fun `passes multiple Foo1 actions to reducer`() {
                verify(reducer, times(3)).reduce(ExampleState.DEFAULT, Foo1)
            }
        }

        @Nested
        inner class `when both Foo1 and Foo2 actions dispatched to store` {

            init {
                store.dispatch(Foo1, Foo2)
                rxSchedulers.flush()
            }

            @Test
            fun `passes Foo1 then Foo2 action to reducer`() {
                verify(reducer).reduce(any(), eq(Foo1))
                verify(reducer).reduce(any(), eq(Foo2))
            }

            @Test
            fun `sends both Bar1 and Bar2 actions together to store`() {
                verify(store).dispatch(listOf(Bar1, Bar2))
            }
        }
    }
}