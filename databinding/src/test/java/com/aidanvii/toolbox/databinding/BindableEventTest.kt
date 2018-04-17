package com.aidanvii.toolbox.databinding

import com.aidanvii.toolbox.Consumer
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.junit.Test

class BindableEventTest {

    val mockBlock = mock<Consumer<Int>>()

    val givenValue = 1
    val tested = givenValue.toBindableEvent()

    @Test
    fun `processEvent is idempotent`() {
        tested.processEvent(mockBlock)
        tested.processEvent(mockBlock)

        verify(mockBlock).invoke(givenValue)
    }
}