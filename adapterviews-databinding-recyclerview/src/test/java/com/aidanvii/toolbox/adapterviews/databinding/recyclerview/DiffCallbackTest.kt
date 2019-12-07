package com.aidanvii.toolbox.adapterviews.databinding.recyclerview

import com.aidanvii.toolbox.adapterviews.databinding.defaultGetChangedProperties
import com.nhaarman.mockito_kotlin.inOrder
import com.nhaarman.mockito_kotlin.spy
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import org.amshove.kluent.`should be true`
import org.amshove.kluent.`should be equal to`
import org.junit.Test

class DiffCallbackTest {

    val spyAreItemsTheSame = spy<(old: String, new: String) -> Boolean>({ old, new -> old == new })
    val spyAreContentsTheSame = spy<(old: String, new: String) -> Boolean>({ old, new -> old == new })
    val spyOldItems = spy(listOf("Malcolm Reynolds", "Inara Serra"))
    val spyNewItems = spy(listOf("Inara Serra", "Malcolm Reynolds", "Kaylee Frye"))

    @Test
    fun `areItemsTheSame is forwarded to given function with items from given item lists`() {
        val diffCallback = diffCallback(
            oldItems = spyOldItems,
            newItems = spyNewItems,
            areItemsTheSame = spyAreItemsTheSame,
            areContentsTheSame = spyAreContentsTheSame,
            getChangedProperties = defaultGetChangedProperties
        )

        diffCallback.areItemsTheSame(0, 1).`should be true`()

        inOrder(spyOldItems, spyNewItems, spyAreItemsTheSame).apply {
            verify(spyOldItems)[0]
            verify(spyNewItems)[1]
            verify(spyAreItemsTheSame).invoke("Malcolm Reynolds", "Malcolm Reynolds")
            verifyNoMoreInteractions()
        }
        verifyZeroInteractions(spyAreContentsTheSame)
    }

    @Test
    fun `areContentsTheSame is forwarded to given function with items from given item lists`() {
        val diffCallback = diffCallback(
            oldItems = spyOldItems,
            newItems = spyNewItems,
            areItemsTheSame = spyAreItemsTheSame,
            areContentsTheSame = spyAreContentsTheSame,
            getChangedProperties = defaultGetChangedProperties
        )

        diffCallback.areContentsTheSame(1, 0).`should be true`()

        inOrder(spyOldItems, spyNewItems, spyAreContentsTheSame).apply {
            verify(spyOldItems)[1]
            verify(spyNewItems)[0]
            verify(spyAreContentsTheSame).invoke("Inara Serra", "Inara Serra")
            verifyNoMoreInteractions()
        }
        verifyZeroInteractions(spyAreItemsTheSame)
    }

    @Test
    fun `oldListSize is forwarded to oldItems size`() {
        val diffCallback = diffCallback(
            oldItems = spyOldItems,
            newItems = spyNewItems,
            areItemsTheSame = spyAreItemsTheSame,
            areContentsTheSame = spyAreContentsTheSame,
            getChangedProperties = defaultGetChangedProperties
        )

        diffCallback.oldListSize.`should be equal to`(2)
        verify(spyOldItems).size
    }

    @Test
    fun `newListSize is forwarded to oldItems size`() {
        val diffCallback = diffCallback(
            oldItems = spyOldItems,
            newItems = spyNewItems,
            areItemsTheSame = spyAreItemsTheSame,
            areContentsTheSame = spyAreContentsTheSame,
            getChangedProperties = defaultGetChangedProperties
        )

        diffCallback.newListSize.`should be equal to`(3)
        verify(spyNewItems).size
    }
}