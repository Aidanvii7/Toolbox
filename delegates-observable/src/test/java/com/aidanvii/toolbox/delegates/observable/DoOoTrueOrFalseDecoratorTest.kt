package com.aidanvii.toolbox.delegates.observable

import com.aidanvii.toolbox.Action
import com.aidanvii.toolbox.assignedButNeverAccessed
import com.aidanvii.toolbox.unusedValue
import com.aidanvii.toolbox.unusedVariable
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@Suppress(assignedButNeverAccessed, unusedValue, unusedVariable)
class DoOoTrueOrFalseDecoratorTest {

    @Nested
    @DisplayName("When property is initialised")
    inner class Initialised {
        val doOnTrue = mock<Action>()
        val doOnFalse = mock<Action>()

        var property by observable(false)
            .doOnTrue(doOnTrue)
            .doOnFalse(doOnFalse)

        @Test
        @DisplayName("Does not invoke doOnTrue or doOnFalse from initial value")
        fun noInvocations() {
            verifyZeroInteractions(doOnTrue, doOnFalse)
        }

        @Nested
        @DisplayName("When property set to true")
        inner class SetTrue {

            @BeforeEach
            fun givenWhen() {
                property = true
            }

            @Test
            @DisplayName("invokes doOnTrue")
            fun doOnTrue() {
                verify(doOnTrue)()
            }
        }

        @Nested
        @DisplayName("When property set to false")
        inner class SetFalse {

            @BeforeEach
            fun givenWhen() {
                property = false
            }

            @Test
            @DisplayName("invokes doOnFalse")
            fun doOnFalse() {
                verify(doOnFalse)()
            }
        }
    }
}