package com.aidanvii.toolbox.redux

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import com.aidanvii.toolbox.redux.ExampleAction.Foo1
import com.aidanvii.toolbox.redux.ExampleAction.Foo2

class BufferedActionDispatcherTest {

    val bufferCount = 2
    val store = mock<ExampleStore>()
    val tested = BufferedActionDispatcher(store, bufferCount)

    @Nested
    inner class `When dispatch called while bufferCount has not been reached` {

        init {
            tested.dispatch(Foo1)
        }

        @Test
        fun `does not forward action to store`() {
            verifyZeroInteractions(store)
        }

        @Nested
        inner class `When dispatch called that causes bufferCount to be reached` {

            init {
                tested.dispatch(Foo2)
            }

            @Test
            fun `does not forward action to store`() {
                verify(store).dispatch(listOf(Foo1, Foo2))
            }
        }
    }
}