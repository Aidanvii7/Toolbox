package com.aidanvii.toolbox.delegates.observable

import com.aidanvii.toolbox.assignedButNeverAccessed
import com.aidanvii.toolbox.unusedValue
import com.aidanvii.toolbox.unusedVariable
import com.nhaarman.mockito_kotlin.inOrder
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import org.amshove.kluent.mock
import org.junit.Test

@Suppress(assignedButNeverAccessed, unusedValue, unusedVariable)
class EagerDecoratorTest {

    val mockDoOnNext = mock<(value: Int) -> Unit>()


    @Test
    fun `propagates initial value of source observable downstream on initialisation`() {
        val given = 0

        var property by observable(given)
                .eager()
                .doOnNext(mockDoOnNext)

        verify(mockDoOnNext).invoke(given)
    }

    @Test
    fun `propagates subsequent values downstream`() {
        val givenInitial = 0
        val givenNext = 1
        var property by observable(givenInitial)
                .eager()
                .doOnNext(mockDoOnNext)

        property = givenNext

        inOrder(mockDoOnNext).apply {
            verify(mockDoOnNext).invoke(givenInitial)
            verify(mockDoOnNext).invoke(givenNext)
        }
        verifyNoMoreInteractions(mockDoOnNext)
    }
}