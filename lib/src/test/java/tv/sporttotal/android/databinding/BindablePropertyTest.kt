package tv.sporttotal.android.databinding

import android.databinding.Bindable
import com.nhaarman.mockito_kotlin.*
import org.junit.jupiter.api.Test


class BindablePropertyTest {

    companion object {
        init {
            PropertyMapper.initBRClass(TestBR::class.java)
        }
    }

    val mockDelegate = mock<NotifiableObservable>()
    val mockOnFirstGet = mock<Action>()

    @Test
    fun `notifyPropertyChanged not called when property is assigned same value`() {
        val initial = 1
        val tested = TestNotifiableObservable(initial)

        tested.property1 = initial

        verify(mockDelegate, never()).notifyPropertyChanged(any())
        verify(mockDelegate, never()).notifyChange()
    }

    @Test
    fun `notifyPropertyChanged called when property is assigned different value`() {
        val initial = 1
        val expected = 2
        val tested = TestNotifiableObservable(initial)

        tested.property1 = expected

        verify(mockDelegate).notifyPropertyChanged(TestBR.property1)
        verify(mockDelegate, never()).notifyChange()
        verifyNoMoreInteractions(mockDelegate)
    }

    @Test
    fun `onFirstGet invoked on first access to property2`() {
        val tested = TestNotifiableObservable()

        tested.property2
        tested.property2

        verify(mockOnFirstGet).invoke()
        verifyNoMoreInteractions(mockDelegate)
    }

    inner class TestNotifiableObservable(initialValue1: Int = 1,
                                         initialValue2: Int = 2) : NotifiableObservable by mockDelegate {

        @get:Bindable
        var property1 by bindable(initialValue1)

        @get:Bindable
        var property2 by bindableLazy(initialValue2) {
            mockOnFirstGet()
        }
    }
}