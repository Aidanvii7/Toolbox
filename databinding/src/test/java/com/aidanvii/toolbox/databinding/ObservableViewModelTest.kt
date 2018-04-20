package com.aidanvii.toolbox.databinding

import com.aidanvii.toolbox.spied
import com.nhaarman.mockito_kotlin.verify
import org.junit.Test

class ObservableViewModelTest {

    val spiedTested = TestObservableViewModel().spied()

    @Test
    fun `dispose calls onCleared`() {
        spiedTested.dispose()

        verify(spiedTested).onDisposed()
    }

    @Test
    fun `dispose is idempotent`() {
        spiedTested.dispose()
        spiedTested.dispose()

        verify(spiedTested).onDisposed()
    }

    class TestObservableViewModel : ObservableViewModel() {

        override fun onDisposed() {
            print("")
        }
    }
}