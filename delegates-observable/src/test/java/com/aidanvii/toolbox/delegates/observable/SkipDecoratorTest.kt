package com.aidanvii.toolbox.delegates.observable

import com.aidanvii.toolbox.assignedButNeverAccessed
import com.aidanvii.toolbox.unusedValue
import com.aidanvii.toolbox.unusedVariable
import com.nhaarman.mockito_kotlin.inOrder
import org.amshove.kluent.mock
import org.junit.Test

@Suppress(assignedButNeverAccessed, unusedValue, unusedVariable)
class SkipDecoratorTest {

    val mockDoOnNext = mock<(propertyEvent: Int) -> Unit>()

    @Test
    fun `only propagates values downstream after skipCount has been met`() {
        val skipCount = 5
        val givenValues = intArrayOf(1, 2, 3, 4, 5, 6, 7)
        val expectedValues = givenValues.takeLast(givenValues.size - skipCount)
        var property by observable(0)
                .skip(skipCount)
                .doOnNext { mockDoOnNext(it) }

        for (givenValue in givenValues) {
            property = givenValue
        }

        inOrder(mockDoOnNext).apply {
            for (expectedValue in expectedValues) {
                verify(mockDoOnNext).invoke(expectedValue)
            }
        }
    }
}