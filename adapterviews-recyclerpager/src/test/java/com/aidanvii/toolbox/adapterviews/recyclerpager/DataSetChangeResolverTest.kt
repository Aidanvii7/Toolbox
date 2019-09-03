package com.aidanvii.toolbox.adapterviews.recyclerpager

import androidx.viewpager.widget.PagerAdapter
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals

@RunWith(Parameterized::class)
internal class DataSetChangeResolverTest(val param: Parameter) {

    val callback = ItemChangeResolver(param)

    val tested = DataSetChangeResolver<Item, RecyclerPagerAdapter.ViewHolder>(callback, param.newItems.maxElement)
    @Mock lateinit var mockAdapter: RecyclerPagerAdapter<Item, RecyclerPagerAdapter.ViewHolder>

    @Before
    fun before() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun `returns expected adapter positions for items`() {
        param.apply {
            oldItems.forEach {
                val itemIndex = oldItems.indexOf(it)
                val expectedPosition = param.expectedPositions[itemIndex]
                val pageItem = PageItem<RecyclerPagerAdapter.ViewHolder>(0, itemIndex)

                val actualPosition = tested.resolvePageItemPosition(pageItem)

                assertEquals(actualPosition, expectedPosition)
            }
        }
    }

    companion object {
        const val UNCHANGED = PagerAdapter.POSITION_UNCHANGED
        const val NONE = PagerAdapter.POSITION_NONE

        private fun items(vararg ids: Int): List<Item> = ids.map { Item(it) }

        private val <E> List<E>.maxElement: Int get() = size - 1

        @JvmStatic
        @Parameterized.Parameters
        fun getParameters(): List<Parameter> {
            return listOf(
                    Parameter(
                            oldItems = items(0, 1, 2, 3, 4),
                            newItems = items(0, 1, 2),
                            expectedPositions = listOf(
                                    UNCHANGED, UNCHANGED, UNCHANGED, NONE, NONE
                            )
                    ),
                    Parameter(
                            oldItems = items(0, 1, 2, 3, 4),
                            newItems = items(2, 3, 4),
                            expectedPositions = listOf(
                                    NONE, NONE, 0, 1, 2
                            )
                    ),
                    Parameter(
                            oldItems = items(2, 3, 4),
                            newItems = items(0, 1, 2),
                            expectedPositions = listOf(
                                    2, NONE, NONE
                            )
                    ),
                    Parameter(
                            oldItems = items(0),
                            newItems = items(0),
                            expectedPositions = listOf(
                                    UNCHANGED
                            )
                    ),
                    Parameter(
                            oldItems = items(0),
                            newItems = items(),
                            expectedPositions = listOf(
                                    NONE
                            )
                    ),
                    Parameter(
                            oldItems = items(0, 1, 2),
                            newItems = items(2, 1, 0),
                            expectedPositions = listOf(
                                    2, UNCHANGED, 0
                            )
                    ),
                    Parameter(
                            oldItems = items(0, 1, 2),
                            newItems = items(0, 1, 2),
                            expectedPositions = listOf(
                                    NONE, NONE, NONE
                            ),
                            sameIdsChangedContent = true
                    ),
                    Parameter(
                            oldItems = items(0, 1, 2),
                            newItems = items(2, 1, 0),
                            expectedPositions = listOf(
                                    NONE, NONE, NONE
                            ),
                            sameIdsChangedContent = true
                    )
            )
        }
    }

    class Parameter(val oldItems: List<Item>,
                    val newItems: List<Item>,
                    val expectedPositions: List<Int>,
                    val sameIdsChangedContent: Boolean = false)

    data class Item(val id: Int)

    class ItemChangeResolver(
            val param: Parameter
    )
        : RecyclerPagerAdapter.OnDataSetChangedCallback<Item> {
        override fun getNewAdapterPositionOfItem(item: Item) = param.newItems.indexOf(item)
        override fun getOldItemAt(oldAdapterPosition: Int) = param.oldItems[oldAdapterPosition]
        override fun getNewItemAt(newAdapterPosition: Int) = param.newItems[newAdapterPosition]
        override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean {
            return if (oldItem == newItem) {
                !param.sameIdsChangedContent
            } else {
                false
            }
        }
    }
}

