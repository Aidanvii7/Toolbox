package com.aidanvii.toolbox.adapterviews.databinding.recyclerview

import com.aidanvii.toolbox.spied
import com.nhaarman.mockito_kotlin.verify
import org.junit.Test

class ObservableItemViewModelTest {

    val spiedTested = TestObservableItemViewModel().spied()

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

    class TestObservableItemViewModel : ObservableItemViewModel()
}