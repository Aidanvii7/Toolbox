package com.aidanvii.toolbox.adapterviews.recyclerview

import com.aidanvii.toolbox.boundInt
import com.aidanvii.toolbox.databinding.NotifiableObservable
import com.nhaarman.mockito_kotlin.argumentCaptor
import com.nhaarman.mockito_kotlin.inOrder
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be`
import org.amshove.kluent.any
import org.amshove.kluent.mock
import org.junit.Test
import java.util.*

class AdapterNotifierDelegateTest {

    val random = Random()

    val expectedAdapterPosition = random.nextInt()

    val mockNotifiable = mock<NotifiableObservable>()
    val mockAdapter = mock<BindingRecyclerViewAdapter<*>>().apply {
        whenever(getItemPositionFromBindableItem(any())).thenReturn(expectedAdapterPosition)
    }

    val tested = AdapterNotifier.delegate().apply {
        initAdapterNotifierDelegator(mockNotifiable)
    }

    @Test
    fun `given the adapter is bound and outwith bind phase, when notifyAdapterPropertyChanged is called with fullRebind false, notifyItemChanged is called on adapter with correct data`() {
        val expectedPropertyIds = nextRandomIds()
        tested.bindAdapter(mockAdapter)

        notifyAdapterPropertiesChanged(fullRebind = false, expectedPropertyIds = expectedPropertyIds)

        verifyAdapterNotifiedWithCorrectPayload(expectedPropertyIds = *expectedPropertyIds)
    }

    @Test
    fun `given the adapter is bound and outwith bind phase, when notifyAdapterPropertyChanged is called with fullRebind true, notifyItemChanged is called on adapter with correct data`() {
        val expectedPropertyIds = nextRandomIds()
        tested.bindAdapter(mockAdapter)

        notifyAdapterPropertiesChanged(fullRebind = true, expectedPropertyIds = expectedPropertyIds)

        verifyAdapterNotifiedWithoutPayload(expectedPropertyIds = *expectedPropertyIds)
    }

    @Test
    fun `given the adapter is bound and during bind phase, when notifyAdapterPropertyChanged is called with fullRebind false, notifyPropertyChanged is called on given NotifiableObservable with correct data`() {
        val expectedPropertyIds = nextRandomIds()
        tested.bindAdapter(mockAdapter)
        tested.adapterBindStart(mockAdapter)

        notifyAdapterPropertiesChanged(fullRebind = false, expectedPropertyIds = expectedPropertyIds)

        verifyAdapterNeverNotified()
        verifyObservableNotifiedWithCorrectPayload(expectedPropertyIds = *expectedPropertyIds)
    }

    @Test
    fun `given the adapter is bound and during bind phase, when notifyAdapterPropertyChanged is called with fullRebind true, notifyPropertyChanged is called on given NotifiableObservable with correct data`() {
        val expectedPropertyIds = nextRandomIds()
        tested.bindAdapter(mockAdapter)
        tested.adapterBindStart(mockAdapter)

        notifyAdapterPropertiesChanged(fullRebind = true, expectedPropertyIds = expectedPropertyIds)

        verifyAdapterNeverNotified()
        verifyObservableNotified(times = expectedPropertyIds.size)
    }

    @Test
    fun `given the adapter is bound, outwith bind phase and paused, when notifyAdapterPropertyChanged is called with fullRebind false, no notifications are fired`() {
        val expectedPropertyIds = nextRandomIds()
        tested.bindAdapter(mockAdapter)
        tested.beginBatchedUpdates()

        notifyAdapterPropertiesChanged(fullRebind = false, expectedPropertyIds = expectedPropertyIds)

        verifyAdapterNeverNotified()
        verifyObservableNeverNotified()
    }

    @Test
    fun `given the adapter is bound, outwith bind phase and paused, when notifyAdapterPropertyChanged is called with fullRebind true, no notifications are fired`() {
        val expectedPropertyIds = nextRandomIds()
        tested.bindAdapter(mockAdapter)
        tested.beginBatchedUpdates()

        notifyAdapterPropertiesChanged(fullRebind = true, expectedPropertyIds = expectedPropertyIds)

        verifyAdapterNeverNotified()
        verifyObservableNeverNotified()
    }

    @Test
    fun `given the adapter is bound, outwith bind phase, paused and has pending property changes with fullRebind false, when endBatchedUpdates is called, notifyItemChanged is called on adapter with correct data`() {
        val expectedPropertyIds = nextRandomIds()
        tested.bindAdapter(mockAdapter)
        tested.beginBatchedUpdates()
        notifyAdapterPropertiesChanged(fullRebind = false, expectedPropertyIds = expectedPropertyIds)

        tested.endBatchedUpdates()

        verifyAdapterNotifiedWithCorrectPayloadWhileBatched(*expectedPropertyIds)
    }

    @Test
    fun `given the adapter is bound, outwith bind phase, paused and has pending property changes with fullRebind true, when endBatchedUpdates is called, notifyItemChanged is called on adapter with correct data`() {
        val expectedPropertyIds = nextRandomIds()
        tested.bindAdapter(mockAdapter)
        tested.beginBatchedUpdates()
        notifyAdapterPropertiesChanged(fullRebind = true, expectedPropertyIds = expectedPropertyIds)

        tested.endBatchedUpdates()

        verifyAdapterNotifiedWithoutPayload()
    }

    @Test
    fun `given the adapter is bound, during bind phase and paused, when notifyAdapterPropertyChanged is called with fullRebind false, no notifications are fired`() {
        val expectedPropertyIds = nextRandomIds()
        tested.bindAdapter(mockAdapter)
        tested.adapterBindStart(mockAdapter)
        tested.beginBatchedUpdates()

        notifyAdapterPropertiesChanged(fullRebind = false, expectedPropertyIds = expectedPropertyIds)

        verifyAdapterNeverNotified()
        verifyObservableNeverNotified()
    }

    @Test
    fun `given the adapter is bound, during bind phase and paused, when notifyAdapterPropertyChanged is called with fullRebind true, no notifications are fired`() {
        val expectedPropertyIds = nextRandomIds()
        tested.bindAdapter(mockAdapter)
        tested.adapterBindStart(mockAdapter)
        tested.beginBatchedUpdates()

        notifyAdapterPropertiesChanged(fullRebind = true, expectedPropertyIds = expectedPropertyIds)

        verifyAdapterNeverNotified()
        verifyObservableNeverNotified()
    }

    @Test
    fun `given the adapter is bound, during bind phase, paused and has pending property changes with fullRebind false, when endBatchedUpdates is called, notifyPropertyChanged is called on given NotifiableObservable with correct data`() {
        val expectedPropertyIds = nextRandomIds()
        tested.bindAdapter(mockAdapter)
        tested.adapterBindStart(mockAdapter)
        tested.beginBatchedUpdates()
        notifyAdapterPropertiesChanged(fullRebind = false, expectedPropertyIds = expectedPropertyIds)

        tested.endBatchedUpdates()

        verifyAdapterNeverNotified()
        verifyObservableNotifiedWithCorrectPayload(expectedPropertyIds = *expectedPropertyIds)
    }

    @Test
    fun `given the adapter is bound, during bind phase, paused and has pending property changes with fullRebind true, when endBatchedUpdates is called, notifyPropertyChanged is called on given NotifiableObservable with correct data`() {
        val expectedPropertyIds = nextRandomIds()
        tested.bindAdapter(mockAdapter)
        tested.adapterBindStart(mockAdapter)
        tested.beginBatchedUpdates()
        notifyAdapterPropertiesChanged(fullRebind = true, expectedPropertyIds = expectedPropertyIds)

        tested.endBatchedUpdates()

        verifyAdapterNeverNotified()
        verifyObservableNotified(times = 1)
    }

    fun nextRandomIds(): IntArray = (0..random.boundInt(1, 20)).distinct().toIntArray()

    fun notifyAdapterPropertiesChanged(fullRebind: Boolean, expectedPropertyIds: IntArray) {
        expectedPropertyIds.forEach {
            tested.notifyAdapterPropertyChanged(it, fullRebind)
        }
    }

    fun verifyObservableNeverNotified() {
        verify(mockNotifiable, never()).notifyPropertyChanged(any())
    }

    fun verifyAdapterNeverNotified() {
        verify(mockAdapter, never()).notifyItemChanged(any(), any())
    }

    fun verifyAdapterNotifiedWithCorrectPayload(vararg expectedPropertyIds: Int) {
        inOrder(mockAdapter).apply {
            for (expectedPropertyId in expectedPropertyIds) {
                argumentCaptor<Int>().apply {
                    verify(mockAdapter).notifyItemChanged(capture(), any())
                    firstValue `should be equal to` expectedAdapterPosition
                }
            }
        }
        inOrder(mockAdapter).apply {
            for (expectedPropertyId in expectedPropertyIds) {
                argumentCaptor<AdapterNotifier.ChangePayload>().apply {
                    verify(mockAdapter).notifyItemChanged(any(), capture())
                    firstValue.apply {
                        sender `should be` tested
                        changedProperties.size `should be equal to` 1
                        changedProperties[0] `should be equal to` expectedPropertyId
                    }
                }
            }
        }
    }

    fun verifyAdapterNotifiedWithoutPayload(vararg expectedPropertyIds: Int) {
        inOrder(mockAdapter).apply {
            for (expectedPropertyId in expectedPropertyIds) {
                argumentCaptor<Int>().apply {
                    verify(mockAdapter).notifyItemChanged(capture())
                    firstValue `should be equal to` expectedAdapterPosition
                }
            }
        }
    }

    fun verifyAdapterNotifiedWithCorrectPayloadWhileBatched(vararg expectedPropertyIds: Int) {
        argumentCaptor<Int>().apply {
            verify(mockAdapter).notifyItemChanged(capture(), any())
            firstValue `should be equal to` expectedAdapterPosition
        }
        argumentCaptor<AdapterNotifier.ChangePayload>().apply {
            verify(mockAdapter).notifyItemChanged(any(), capture())
            firstValue.apply {
                sender `should be` tested
                changedProperties.size `should be equal to` expectedPropertyIds.size
                expectedPropertyIds.forEachIndexed { index, expectedPropertyIds ->
                    changedProperties[index] `should be equal to` expectedPropertyIds
                }
            }
        }
    }

    fun verifyObservableNotifiedWithCorrectPayload(vararg expectedPropertyIds: Int) {
        for (expectedPropertyId in expectedPropertyIds) {
            verify(mockNotifiable).notifyPropertyChanged(expectedPropertyId)
        }
    }

    fun verifyObservableNotified(times: Int) {
        verify(mockNotifiable, times(times)).notifyChange()
    }
}