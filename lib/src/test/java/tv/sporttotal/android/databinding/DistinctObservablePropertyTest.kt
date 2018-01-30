package tv.sporttotal.android.databinding

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import org.junit.jupiter.api.Test

@Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE")
class DistinctObservablePropertyTest {

    val onNewValue = mock<Consumer<Int>>()

    @Test
    fun `onNewValue not invoked when same value assigned`() {
        var tested by distinctObservable(1, onNewValue)

        tested = 1

        verifyZeroInteractions(onNewValue)
    }

    @Test
    fun `onNewValue invoked when different value assigned`() {
        var tested by distinctObservable(1, onNewValue)

        tested = 2

        verify(onNewValue).invoke(2)
        verifyNoMoreInteractions(onNewValue)
    }
}