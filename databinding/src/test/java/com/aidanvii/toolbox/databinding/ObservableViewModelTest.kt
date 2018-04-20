package com.aidanvii.toolbox.databinding

import com.aidanvii.toolbox.spied
import com.nhaarman.mockito_kotlin.verify
import org.junit.Test

class ObservableViewModelTest {

    val spiedTested = TestObservableViewModel().spied()

    @Test
    fun `clear calls onCleared`() {
        spiedTested.clear()

        verify(spiedTested).onCleared()
    }

    @Test
    fun `clear is idempotent`() {
        spiedTested.clear()
        spiedTested.clear()

        verify(spiedTested).onCleared()
    }

    class TestObservableViewModel : ObservableViewModel() {

        public override fun onCleared() {

        }
    }
}