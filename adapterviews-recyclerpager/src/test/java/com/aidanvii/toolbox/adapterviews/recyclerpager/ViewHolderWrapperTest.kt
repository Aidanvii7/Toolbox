package com.aidanvii.toolbox.adapterviews.recyclerpager

import com.nhaarman.mockito_kotlin.inOrder
import com.nhaarman.mockito_kotlin.spy
import com.nhaarman.mockito_kotlin.verify
import com.aidanvii.toolbox.adapterviews.recyclerpager.TestViewHolder.Companion.VIEW_TYPE_0
import org.amshove.kluent.mock
import org.junit.Test
import kotlin.test.assertEquals

internal class ViewHolderWrapperTest() {

    @Test(expected = IllegalStateException::class)
    fun `constructor does not accept ViewHolders with view that has a parent`() {
        val viewHolder = TestViewHolder(mockView(hasParent = true))

        ViewHolderWrapper(viewHolder, VIEW_TYPE_0)
    }

    @Test(expected = NullPointerException::class)
    fun `constructor does not accept ViewHolders with null view`() {
        val viewHolder = JavaTestViewHolder()

        ViewHolderWrapper(viewHolder, VIEW_TYPE_0)
    }

    @Test
    fun `constructor accepts ViewHolders with orphan view`() {
        val viewHolder = TestViewHolder(mockView(hasParent = false))

        ViewHolderWrapper(viewHolder, VIEW_TYPE_0)
    }

    @Test
    fun `destroy calls onDestroyed on ViewHolder`() {
        val viewHolder = spy(TestViewHolder(mockView(hasParent = false)))
        val tested = ViewHolderWrapper(viewHolder, VIEW_TYPE_0)

        tested.destroy()

        verify(viewHolder).onDestroyed()
    }

    @Test
    fun `getViewHolder returns ViewHolder when not destroyed`() {
        val expected = TestViewHolder(mockView(hasParent = false))
        val tested = ViewHolderWrapper(expected, VIEW_TYPE_0)

        val actual = tested.viewHolder

        assertEquals(expected, actual)
    }

    @Test
    fun `getViewType returns viewType when not destroyed`() {
        val viewHolder = TestViewHolder(mockView(hasParent = false))
        val expected = VIEW_TYPE_0
        val tested = ViewHolderWrapper(viewHolder, expected)

        val actual = tested.itemType

        assertEquals(expected, actual)
    }

    @Test
    fun `addViewToContainer calls addViewToContainer on ViewHolder`() {
        val viewHolder = spy(TestViewHolder(mockView(hasParent = false)))
        val tested = ViewHolderWrapper(viewHolder, VIEW_TYPE_0)
        val mockContainer = mockContainer()

        tested.addViewToContainer(mockContainer)

        verify(viewHolder).addViewToContainer(mockContainer)
    }

    @Test
    fun `addViewToContainer twice calls addViewToContainer, removeViewFromContainer then addViewToContainer`() {
        val viewHolder = spy(TestViewHolder(mockView(hasParent = false)))
        val tested = ViewHolderWrapper(viewHolder, VIEW_TYPE_0)
        val mockContainer = mockContainer()

        tested.addViewToContainer(mockContainer)
        tested.addViewToContainer(mockContainer)

        inOrder(viewHolder).apply {
            verify(viewHolder).addViewToContainer(mockContainer)
            verify(viewHolder).removeViewFromContainer(mockContainer)
            verify(viewHolder).addViewToContainer(mockContainer)
        }
    }

    @Test(expected = IllegalStateException::class)
    fun `addViewToContainer fails when addViewToContainer on ViewHolder does not add view to container`() {
        val viewHolder = spy(TestViewHolder(mockView(hasParent = false), onAddViewToContainer = mock()))
        val tested = ViewHolderWrapper(viewHolder, VIEW_TYPE_0)
        val mockContainer = mockContainer()

        tested.addViewToContainer(mockContainer)
    }

    @Test
    fun `removeViewFromContainer calls removeViewFromContainer on ViewHolder`() {
        val viewHolder = spy(TestViewHolder(mockView(hasParent = false)))
        val tested = ViewHolderWrapper(viewHolder, VIEW_TYPE_0)
        val mockContainer = mockContainer()

        tested.removeViewFromContainer(mockContainer)

        verify(viewHolder).removeViewFromContainer(mockContainer)
    }

    @Test(expected = IllegalStateException::class)
    fun `removeViewFromContainer fails when addViewToContainer on ViewHolder does not add view to container`() {
        val viewHolder = spy(TestViewHolder(mockView(hasParent = false), onAddViewToContainer = mock()))
        val tested = ViewHolderWrapper(viewHolder, VIEW_TYPE_0)
        val mockContainer = mockContainer()

        tested.addViewToContainer(mockContainer)
    }
}

