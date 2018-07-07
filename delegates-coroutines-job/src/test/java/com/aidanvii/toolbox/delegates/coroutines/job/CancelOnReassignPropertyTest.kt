package com.aidanvii.toolbox.delegates.coroutines.job

import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import kotlinx.coroutines.experimental.Job
import org.amshove.kluent.mock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class CancelOnReassignPropertyTest {

    var tested by cancelOnReassign(null)

    @Nested
    inner class `When given non-null Job` {

        val mockJob1 = mock<Job>()

        @BeforeEach
        fun before() {
            tested = mockJob1
        }

        @Test
        fun `does nothing with given value`() {
            verifyZeroInteractions(mockJob1)
        }

        @Nested
        inner class `When given subsequent non-null Job` {

            @BeforeEach
            fun before() {
                tested = mock()
            }

            @Test
            fun `cancels previous Job`() {
                verify(mockJob1).cancel()
            }
        }

        @Nested
        inner class `When given subsequent null Job` {


            @BeforeEach
            fun before() {
                tested = null
            }

            @Test
            fun `cancels previous Job`() {
                verify(mockJob1).cancel()
            }
        }
    }
}