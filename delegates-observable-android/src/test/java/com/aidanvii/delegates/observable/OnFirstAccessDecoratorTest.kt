package com.aidanvii.delegates.observable

import com.aidanvii.common.canBeVal
import com.aidanvii.common.Action
import com.aidanvii.common.unusedExpression
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import org.junit.Test

@Suppress(unusedExpression, canBeVal)
class OnFirstAccessDecoratorTest {

    val mockOnFirstAccess = mock<Action>()

    @Test
    fun `onFirstAccess invoked only on first access`() {
        var tested by observable(1)
                .onFirstAccess(LazyThreadSafetyMode.NONE, mockOnFirstAccess)

        tested
        tested

        verify(mockOnFirstAccess).invoke()
        verifyNoMoreInteractions(mockOnFirstAccess)
    }
}