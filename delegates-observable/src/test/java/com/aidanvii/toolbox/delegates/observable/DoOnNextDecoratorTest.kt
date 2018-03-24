package com.aidanvii.toolbox.delegates.observable

import com.aidanvii.toolbox.assignedButNeverAccessed
import com.aidanvii.toolbox.unusedValue
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import org.amshove.kluent.mock
import org.junit.Test

@Suppress(assignedButNeverAccessed, unusedValue)
class DoOnNextDecoratorTest {

    val mockDoOnNext = mock<(newValue: Int) -> Unit>()
    val mockDoOnNextWithPrevious = mock<(oldValue: Int?, newValue: Int?) -> Unit>()

    @Test
    fun `doOnNext invoked when tested is given a value`() {
        val given = 0
        var property by observable(given)
                .doOnNext(mockDoOnNext)

        property = given

        verify(mockDoOnNext).invoke(given)
        verifyNoMoreInteractions(mockDoOnNext)

    }

    @Test
    fun `doOnNextWithPrevious invoked when tested is given a value`() {
        val givenInitial = 0
        val givenNext = 1
        var property by observable(givenInitial)
                .doOnNextWithPrevious(mockDoOnNextWithPrevious)

        property = givenNext

        verify(mockDoOnNextWithPrevious).invoke(givenInitial, givenNext)
        verifyNoMoreInteractions(mockDoOnNextWithPrevious)
    }
}