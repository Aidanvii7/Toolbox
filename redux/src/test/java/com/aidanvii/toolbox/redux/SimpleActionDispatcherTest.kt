package com.aidanvii.toolbox.redux

import com.nhaarman.mockito_kotlin.mock
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import com.aidanvii.toolbox.redux.ExampleAction.Foo1
import com.nhaarman.mockito_kotlin.verify

class SimpleActionDispatcherTest {

    val store = mock<ExampleStore>()
    val tested = SimpleActionDispatcher(store)

    @Nested
    inner class `When dispatch called` {

        init {
            tested.dispatch(Foo1)
        }

        @Test
        fun `forwards action to store`() {
            verify(store).dispatch(Foo1)
        }
    }
}