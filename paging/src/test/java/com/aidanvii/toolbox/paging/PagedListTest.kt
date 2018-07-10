package com.aidanvii.toolbox.paging

import com.aidanvii.toolbox.Consumer
import com.nhaarman.mockito_kotlin.*
import io.reactivex.observers.TestObserver
import org.amshove.kluent.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.util.*
import org.amshove.kluent.any as kluentAny

class PagedListTest {

    val dataSource = spy<ItemDataSource>()
    val nonNullDataSource = spy<NonNullItemDataSource>()

    @Test
    fun `created with size of DataSource`() {
        PagedList(dataSource, pageSize = 10).size `should be equal to` dataSource.initialDataCount
    }

    @Test
    fun `peek returns null initially for every position`() {
        PagedList(dataSource, pageSize = 10).verifyPageRange(startPage = 1, endPage = 10, verify = VerifyMode.PEEK_EMPTY)
    }

    @Test
    fun `peek returns non-null for elements in initial pages once the initial pages are populated`() {
        PagedList(dataSource, pageSize = 10, loadInitialPages = intArrayOf(1, 2, 3)).apply {
            dataSource.testScheduler.triggerActions()

            dataSource.testObserver.assertValueCount(3)
            verifyPageRange(startPage = 1, endPage = 3, verify = VerifyMode.PEEK_LOADED)
            verifyPageRange(startPage = 4, endPage = 10, verify = VerifyMode.PEEK_EMPTY)
        }
    }

    @Test
    fun `accessing arbitrary index non-incrementally triggers loadPage for pages in range with prefetch zero`() {
        PagedList(dataSource, pageSize = 10).apply {

            loadAround(15)
            dataSource.testScheduler.triggerActions()

            dataSource.testObserver.assertValueCount(1)
            verifyPageRange(startPage = 1, verify = VerifyMode.PEEK_EMPTY)
            verifyPageRange(startPage = 2, verify = VerifyMode.PEEK_LOADED)
            verifyPageRange(startPage = 3, verify = VerifyMode.PEEK_EMPTY)
        }
    }

    @Test
    fun `accessing arbitrary index non-incrementally triggers loadPage for pages in range with prefetch non-zero`() {
        PagedList(dataSource, pageSize = 10, prefetchDistance = 6).apply {

            loadAround(15)
            dataSource.testScheduler.triggerActions()

            dataSource.testObserver.assertValueCount(3)
            verifyPageRange(startPage = 1, endPage = 3, verify = VerifyMode.PEEK_LOADED)
            verifyPageRange(startPage = 4, endPage = 10, verify = VerifyMode.PEEK_EMPTY)
        }
    }

    @Test
    fun `calls loadPage on incrementally sequential access when a page boundary is crossed`() {
        PagedList(dataSource, pageSize = 10, prefetchDistance = 2).verifyPageRange(startPage = 1, endPage = 10, verify = VerifyMode.GET_LOADING)
    }

    @Test
    fun `calls loadPage on decrementally sequential access when a page boundary is crossed`() {
        PagedList(dataSource, pageSize = 10, prefetchDistance = 2).verifyPageRange(startPage = 10, endPage = 1, verify = VerifyMode.GET_LOADING)
    }

    @Test
    fun `does not call loadPage for pages that already have in-flight requests when element in page is accessed`() {
        PagedList(dataSource, pageSize = 10).apply {
            loadAround(0)

            loadAround(0)

            dataSource.testScheduler.triggerActions()
            dataSource.testObserver.assertValueCount(1)
        }
    }

    @Test
    fun `does not call loadPage for pages that have are already loaded when get is called`() {
        PagedList(dataSource, pageSize = 10).apply {
            loadAround(0)
            `complete in-flight requests`()

            loadAround(0)

            dataSource.testScheduler.triggerActions()
            dataSource.testObserver.assertValueCount(0)
        }
    }

    @Test
    @Disabled("problem spying PagedList when migrating from kotlin 1.2.31 -> 1.2.50")
    fun `get calls access then peek`() {
        spy(PagedList(dataSource, pageSize = 10)).apply {
            Random().nextInt(endIndexForPage(10)).let { randomIndex ->

                get(randomIndex)

                inOrder(this).run {
                    verify(this@apply).loadAround(randomIndex, growIfNecessary = false)
                    verify(this@apply).peek(randomIndex)
                    verifyNoMoreInteractions()
                }
            }
        }
    }

    @Test
    @Disabled("problem spying PagedList when migrating from kotlin 1.2.31 -> 1.2.50")
    fun `peek returns null value for not-loaded item without triggering loadPage`() {
        spy(PagedList(dataSource, pageSize = 10)).apply {
            Random().nextInt(endIndexForPage(10)).let { randomIndexInBound ->
                reset(this, dataSource)

                peek(randomIndexInBound).`should be null`()

                verifyZeroInteractions(dataSource)
                verify(this).peek(randomIndexInBound)
                verifyNoMoreInteractions(this)
            }
        }
    }

    @Test
    fun `peek returns non-null value for loaded item without triggering loadPage`() {
        PagedList(dataSource, pageSize = 10).apply {
            Random().nextInt(endIndexForPage(10)).let { randomIndexInBound ->
                get(randomIndexInBound)
                `complete in-flight requests`()
                get(randomIndexInBound).let { expectedItem ->
                    reset(dataSource)

                    peek(randomIndexInBound).also { actualItem ->

                        actualItem.`should not be null`()
                        actualItem `should equal` expectedItem
                        verifyZeroInteractions(dataSource)
                    }
                }
            }
        }
    }

    @Test
    fun `list grows when element above maximum size is accessed`() {
        PagedList(dataSource, pageSize = 10).apply {

            loadAround(122, growIfNecessary = true)

            size `should be equal to` 123
        }
    }

    @Test
    fun `list grows when element above maximum size is accessed, even when dataCount is zero`() {
        dataSource.dataCount.onNext(0)
        PagedList(dataSource, pageSize = 10).apply {

            loadAround(122, growIfNecessary = true)

            size `should be equal to` 123
        }
    }

    @Test
    fun `list grows when page above maximum size is accessed`() {
        PagedList(dataSource, pageSize = 10).apply {

            loadAroundPage(11, growIfNecessary = true)

            size `should be equal to` 110
        }
    }

    @Test
    fun `list grows when page above maximum size is accessed, even when dataCount is zero`() {
        dataSource.dataCount.onNext(0)
        PagedList(dataSource, pageSize = 10).apply {

            loadAroundPage(11, growIfNecessary = true)

            size `should be equal to` 110
        }
    }

    @Test
    fun `list grows to correct size from requested initial pages, even when dataCount is zero`() {
        dataSource.dataCount.onNext(0)
        PagedList(dataSource, pageSize = 10, loadInitialPages = intArrayOf(1, 3, 5)).apply {
            `complete in-flight requests`()

            size `should be equal to` 50
        }
    }

    @Test
    fun `list grows to correct size from requested initial pages that are valid`() {
        dataSource.dataCount.onNext(0)
        dataSource.invalidPageNumbers = intArrayOf(2, 3)
        PagedList(dataSource, pageSize = 10, loadInitialPages = intArrayOf(1, 2, 3)).apply {
            `complete in-flight requests`()

            size `should be equal to` 10
        }
    }

    @Test
    fun `list grows when DataSource emits size above the current maximum size`() {
        PagedList(dataSource, pageSize = 10).apply {

            dataSource.dataCount.onNext(101)

            size `should be equal to` 101
        }
    }

    @Test
    fun `list shrinks when DataSource emits size below the current maximum size`() {
        PagedList(dataSource, pageSize = 10).apply {

            dataSource.dataCount.onNext(99)

            size `should be equal to` 99
        }
    }

    @Test
    fun `list is populated with pages that are valid`() {
        dataSource.invalidPageNumbers = intArrayOf(2, 3)
        PagedList(dataSource, pageSize = 10, loadInitialPages = intArrayOf(1, 2, 3)).apply {
            `complete in-flight requests`()

            verifyPageRange(startPage = 1, endPage = 1, verify = VerifyMode.PEEK_LOADED)
            verifyPageRange(startPage = 2, endPage = 3, verify = VerifyMode.PEEK_EMPTY)
        }
    }

    @Test
    fun `throws IndexOutOfBoundsException when loadAround invoked with out of bound element and growIfNecessary false`() {
        PagedList(dataSource, pageSize = 10).apply {
            val loadAround = { loadAround(100, growIfNecessary = false) }

            loadAround `should throw` IndexOutOfBoundsException::class
        }
    }

    @Test
    fun `throws IndexOutOfBoundsException when loadAroundPage invoked with out of bound page and growIfNecessary false`() {
        PagedList(dataSource, pageSize = 10).apply {
            val loadAroundPage = { loadAroundPage(11, growIfNecessary = false) }

            loadAroundPage `should throw` IndexOutOfBoundsException::class
        }
    }

    @Test
    fun `changes receives list of correct size of empty elements when no initial pages are loaded`() {
        PagedList(dataSource, pageSize = 10).apply {
            val testObserver = TestObserver<List<Item?>>()
            distinctObservableList.subscribe(testObserver)

            `complete in-flight requests`()

            testObserver.assertValueCount(1)
            testObserver.values()[0].apply {
                size `should be equal to` size
                all { it == null } `should be equal to` true
            }
        }
    }

    @Test
    fun `change observer receives list of correct size and elements when some initial pages are loaded`() {
        PagedList(dataSource, pageSize = 10, loadInitialPages = intArrayOf(1, 2)).apply {
            val testObserver = TestObserver<List<Item?>>()
            distinctObservableList.subscribe(testObserver)

            `complete in-flight requests`()

            testObserver.assertValueCount(3)
            testObserver.values()[0].apply {
                size `should be equal to` size
                all { it == null } `should be equal to` true
            }
            testObserver.values()[1].apply {
                size `should be equal to` size
                for (index in startIndexForPage(1)..endIndexForPage(1)) {
                    this[index].`should not be null`()
                }
                for (index in startIndexForPage(2)..endIndexForPage(10)) {
                    this[index].`should be null`()
                }
            }
            testObserver.values()[2].apply {
                size `should be equal to` size
                for (index in startIndexForPage(1)..endIndexForPage(2)) {
                    this[index].`should not be null`()
                }
                for (index in startIndexForPage(3)..endIndexForPage(10)) {
                    this[index].`should be null`()
                }
            }
        }
    }

    @Test
    fun `changePayloads receives list of correct size of empty elements when no initial pages are loaded`() {
        PagedList(nonNullDataSource, pageSize = 10).apply {
            val testObserver = TestObserver<PagedList.ChangePayload<Item>>()
            observableChangePayload.subscribe(testObserver)
            `complete in-flight requests`()

            testObserver.assertValueCount(2)
            testObserver.values()[0].apply {
                allItems.size `should be equal to` 0
                addedItems.size `should be equal to` 0
                removedItems.size `should be equal to` 0
            }

            testObserver.values()[1].apply {
                allItems.size `should be equal to` size
                addedItems == allItems
                removedItems.size `should be equal to` 0
            }
        }
    }

    @Test
    fun `changePayloads observer receives list of correct size and elements when some initial pages are loaded`() {
        PagedList(nonNullDataSource, pageSize = 10, loadInitialPages = intArrayOf(1, 2)).apply {
            val testObserver = TestObserver<PagedList.ChangePayload<Item>>()
            observableChangePayload.subscribe(testObserver)

            `complete in-flight requests`()

            testObserver.assertValueCount(4)
            testObserver.values()[0].apply {
                allItems.size `should be equal to` 0
                addedItems.size `should be equal to` 0
                removedItems.size `should be equal to` 0
            }
            testObserver.values()[1].apply {
                allItems.size `should be equal to` size
                addedItems.size `should be equal to` 1
                removedItems.size `should be equal to` 0
                addedItems.toList()[0] `should equal` nonNullDataSource.emptyItem
                for (index in startIndexForPage(1)..endIndexForPage(10)) {
                    allItems[index] `should equal` nonNullDataSource.emptyItem
                }
            }
            testObserver.values()[2].apply {
                allItems.size `should be equal to` size
                addedItems.size `should be equal to` pageSize
                removedItems.size `should be equal to` 0
                val addedItemsList = addedItems.toList()
                val startIndexOfAddedPage = startIndexForPage(1)
                for (index in startIndexOfAddedPage..endIndexForPage(1)) {
                    Item(index).let { expectedItem ->
                        allItems[index] `should equal` expectedItem
                        addedItemsList[index - startIndexOfAddedPage] `should equal` expectedItem
                    }
                }
                for (index in startIndexForPage(2)..endIndexForPage(10)) {
                    allItems[index] `should equal` nonNullDataSource.emptyItem
                }
            }
            testObserver.values()[3].apply {
                allItems.size `should be equal to` size
                addedItems.size `should be equal to` pageSize
                removedItems.size `should be equal to` 0
                for (index in startIndexForPage(1)..endIndexForPage(1)) {
                    allItems[index] `should equal` Item(index)
                }
                val addedItemsList = addedItems.toList()
                val startIndexOfAddedPage = startIndexForPage(2)
                for (index in startIndexOfAddedPage..endIndexForPage(2)) {
                    Item(index).let { expectedItem ->
                        allItems[index] `should equal` expectedItem
                        addedItemsList[index - startIndexOfAddedPage] `should equal` expectedItem
                    }
                }
                for (index in startIndexForPage(3)..endIndexForPage(10)) {
                    allItems[index] `should equal` nonNullDataSource.emptyItem
                }
            }
        }
    }

    @Test
    fun `dispose cancels outstanding page requests`() {
        PagedList(dataSource, pageSize = 10, loadInitialPages = intArrayOf(1, 2)).apply {

            dispose()
            `complete in-flight requests`()

            verifyPageRange(startPage = 1, endPage = 10, verify = VerifyMode.PEEK_EMPTY)
        }
    }

    @Test
    fun `errors for page requests are forwarded to onError, page is reset`() {
        val onError = mock<Consumer<Throwable>>()
        dataSource.errorOnPageNumbers = intArrayOf(2, 3)
        PagedList(dataSource, pageSize = 10, loadInitialPages = intArrayOf(1, 2, 3), onError = onError).apply {

            `complete in-flight requests`()

            verifyPageRange(startPage = 1, verify = VerifyMode.PEEK_LOADED)
            verifyPageRange(startPage = 2, endPage = 10, verify = VerifyMode.PEEK_EMPTY)
        }
    }

    @Test
    fun `loads last page when last page has single element`() {
        dataSource.dataCount.onNext(21)
        PagedList(dataSource, pageSize = 10).apply {

            loadAround(20)
            dataSource.testScheduler.triggerActions()

            verifyPageRange(startPage = 1, endPage = 2, verify = VerifyMode.PEEK_EMPTY)
            verifyPageRange(startPage = 3, verify = VerifyMode.PEEK_LOADED)
        }
    }

    @Test
    fun `invalidateAsEmpty resets loaded elements to null with ElementState EMPTY`() {
        PagedList(dataSource, pageSize = 10, loadInitialPages = intArrayOf(1, 3, 5)).apply {
            loadAroundPage(7)
            loadAroundPage(9)
            dataSource.testScheduler.triggerActions()
            verifyPageRange(startPage = 1, verify = VerifyMode.PEEK_LOADED)
            verifyPageRange(startPage = 2, verify = VerifyMode.PEEK_EMPTY)
            verifyPageRange(startPage = 3, verify = VerifyMode.PEEK_LOADED)
            verifyPageRange(startPage = 4, verify = VerifyMode.PEEK_EMPTY)
            verifyPageRange(startPage = 5, verify = VerifyMode.PEEK_LOADED)
            verifyPageRange(startPage = 6, verify = VerifyMode.PEEK_EMPTY)
            verifyPageRange(startPage = 7, verify = VerifyMode.PEEK_LOADED)
            verifyPageRange(startPage = 8, verify = VerifyMode.PEEK_EMPTY)
            verifyPageRange(startPage = 9, verify = VerifyMode.PEEK_LOADED)
            verifyPageRange(startPage = 10, verify = VerifyMode.PEEK_EMPTY)

            invalidateAsEmpty(refreshElementsInRange = false)

            verifyPageRange(startPage = 1, endPage = 10, verify = VerifyMode.PEEK_EMPTY)
        }
    }

    @Test
    fun `invalidateAsEmpty resets loading elements to null with ElementState EMPTY and cancels outstanding page requests`() {
        PagedList(dataSource, pageSize = 10).apply {
            verifyPageRange(startPage = 1, verify = VerifyMode.GET_LOADING)
            verifyPageRange(startPage = 2, verify = VerifyMode.PEEK_EMPTY)
            verifyPageRange(startPage = 3, verify = VerifyMode.GET_LOADING)
            verifyPageRange(startPage = 4, verify = VerifyMode.PEEK_EMPTY)
            verifyPageRange(startPage = 5, verify = VerifyMode.GET_LOADING)
            verifyPageRange(startPage = 6, verify = VerifyMode.PEEK_EMPTY)
            verifyPageRange(startPage = 7, verify = VerifyMode.GET_LOADING)
            verifyPageRange(startPage = 8, verify = VerifyMode.PEEK_EMPTY)
            verifyPageRange(startPage = 9, verify = VerifyMode.GET_LOADING)
            verifyPageRange(startPage = 10, verify = VerifyMode.PEEK_EMPTY)

            invalidateAsEmpty(refreshElementsInRange = false)
            dataSource.testScheduler.triggerActions()

            dataSource.testObserver.assertValueCount(0)
            verifyPageRange(startPage = 1, endPage = 10, verify = VerifyMode.PEEK_EMPTY)
        }
    }

    @Test
    fun `invalidateAsEmpty with refreshElementsInRange true resets elements to null with ElementState EMPTY and requests last page to be reloaded`() {
        PagedList(dataSource, pageSize = 10).apply {
            verifyPageRange(startPage = 1, verify = VerifyMode.GET_LOADING)
            verifyPageRange(startPage = 2, verify = VerifyMode.PEEK_EMPTY)
            verifyPageRange(startPage = 3, verify = VerifyMode.GET_LOADING)
            verifyPageRange(startPage = 4, verify = VerifyMode.PEEK_EMPTY)
            verifyPageRange(startPage = 5, verify = VerifyMode.GET_LOADING)
            verifyPageRange(startPage = 6, verify = VerifyMode.PEEK_EMPTY)
            verifyPageRange(startPage = 7, verify = VerifyMode.GET_LOADING)
            verifyPageRange(startPage = 8, verify = VerifyMode.PEEK_EMPTY)
            verifyPageRange(startPage = 9, verify = VerifyMode.GET_LOADING)
            verifyPageRange(startPage = 10, verify = VerifyMode.PEEK_EMPTY)
            `complete in-flight requests`()

            loadAroundPage(5)
            invalidateAsEmpty(refreshElementsInRange = true)
            dataSource.testScheduler.triggerActions()

            dataSource.testObserver.assertValueCount(1)
            verifyPageRange(startPage = 1, endPage = 4, verify = VerifyMode.PEEK_EMPTY)
            verifyPageRange(startPage = 5, verify = VerifyMode.PEEK_LOADED)
            verifyPageRange(startPage = 6, endPage = 10, verify = VerifyMode.PEEK_EMPTY)
        }
    }

    @Test
    fun `invalidateLoadedAsDirty flags LOADED elements as DIRTY`() {
        PagedList(dataSource, pageSize = 10).apply {
            verifyPageRange(startPage = 1, verify = VerifyMode.GET_LOADING)
            verifyPageRange(startPage = 3, verify = VerifyMode.GET_LOADING)
            verifyPageRange(startPage = 5, verify = VerifyMode.GET_LOADING)
            verifyPageRange(startPage = 7, verify = VerifyMode.GET_LOADING)
            verifyPageRange(startPage = 9, verify = VerifyMode.GET_LOADING)
            `complete in-flight requests`()

            invalidateLoadedAsDirty(refreshElementsInRange = false)

            verifyPageRange(startPage = 1, verify = VerifyMode.PEEK_DIRTY)
            verifyPageRange(startPage = 2, verify = VerifyMode.PEEK_EMPTY)
            verifyPageRange(startPage = 3, verify = VerifyMode.PEEK_DIRTY)
            verifyPageRange(startPage = 4, verify = VerifyMode.PEEK_EMPTY)
            verifyPageRange(startPage = 5, verify = VerifyMode.PEEK_DIRTY)
            verifyPageRange(startPage = 6, verify = VerifyMode.PEEK_EMPTY)
            verifyPageRange(startPage = 7, verify = VerifyMode.PEEK_DIRTY)
            verifyPageRange(startPage = 8, verify = VerifyMode.PEEK_EMPTY)
            verifyPageRange(startPage = 9, verify = VerifyMode.PEEK_DIRTY)
            verifyPageRange(startPage = 10, verify = VerifyMode.PEEK_EMPTY)
        }
    }

    @Test
    fun `invalidateLoadedAsDirty with refreshElementsInRange true flags LOADED elements as DIRTY and requests last page to be reloaded`() {
        PagedList(dataSource, pageSize = 10).apply {
            verifyPageRange(startPage = 1, verify = VerifyMode.GET_LOADING)
            verifyPageRange(startPage = 3, verify = VerifyMode.GET_LOADING)
            verifyPageRange(startPage = 5, verify = VerifyMode.GET_LOADING)
            verifyPageRange(startPage = 7, verify = VerifyMode.GET_LOADING)
            verifyPageRange(startPage = 9, verify = VerifyMode.GET_LOADING)
            dataSource.testScheduler.triggerActions()

            loadAroundPage(5)
            invalidateLoadedAsDirty(refreshElementsInRange = true)

            verifyPageRange(startPage = 1, verify = VerifyMode.PEEK_DIRTY)
            verifyPageRange(startPage = 2, verify = VerifyMode.PEEK_EMPTY)
            verifyPageRange(startPage = 3, verify = VerifyMode.PEEK_DIRTY)
            verifyPageRange(startPage = 4, verify = VerifyMode.PEEK_EMPTY)
            verifyPageRange(startPage = 5, verify = VerifyMode.PEEK_REFRESHING)
            verifyPageRange(startPage = 6, verify = VerifyMode.PEEK_EMPTY)
            verifyPageRange(startPage = 7, verify = VerifyMode.PEEK_DIRTY)
            verifyPageRange(startPage = 8, verify = VerifyMode.PEEK_EMPTY)
            verifyPageRange(startPage = 9, verify = VerifyMode.PEEK_DIRTY)
            verifyPageRange(startPage = 10, verify = VerifyMode.PEEK_EMPTY)
        }
    }

    @Test
    fun `accessing DIRTY elements flags elements as REFRESHING, which are LOADED when DataSource provides data`() {
        PagedList(dataSource, pageSize = 10).apply {
            verifyPageRange(startPage = 1, verify = VerifyMode.GET_LOADING)
            verifyPageRange(startPage = 3, verify = VerifyMode.GET_LOADING)
            verifyPageRange(startPage = 5, verify = VerifyMode.GET_LOADING)
            verifyPageRange(startPage = 7, verify = VerifyMode.GET_LOADING)
            verifyPageRange(startPage = 9, verify = VerifyMode.GET_LOADING)

            dataSource.testScheduler.triggerActions()

            invalidateLoadedAsDirty(refreshElementsInRange = false)

            verifyPageRange(startPage = 1, verify = VerifyMode.GET_REFRESHING)
            verifyPageRange(startPage = 2, verify = VerifyMode.PEEK_EMPTY)
            verifyPageRange(startPage = 3, verify = VerifyMode.GET_REFRESHING)
            verifyPageRange(startPage = 4, verify = VerifyMode.PEEK_EMPTY)
            verifyPageRange(startPage = 5, verify = VerifyMode.GET_REFRESHING)
            verifyPageRange(startPage = 6, verify = VerifyMode.PEEK_EMPTY)
            verifyPageRange(startPage = 7, verify = VerifyMode.GET_REFRESHING)
            verifyPageRange(startPage = 8, verify = VerifyMode.PEEK_EMPTY)
            verifyPageRange(startPage = 9, verify = VerifyMode.GET_REFRESHING)
            verifyPageRange(startPage = 10, verify = VerifyMode.PEEK_EMPTY)

            dataSource.testScheduler.triggerActions()

            verifyPageRange(startPage = 1, verify = VerifyMode.PEEK_LOADED)
            verifyPageRange(startPage = 3, verify = VerifyMode.PEEK_LOADED)
            verifyPageRange(startPage = 5, verify = VerifyMode.PEEK_LOADED)
            verifyPageRange(startPage = 7, verify = VerifyMode.PEEK_LOADED)
            verifyPageRange(startPage = 9, verify = VerifyMode.PEEK_LOADED)
        }
    }

    @Test
    fun `subscribing to loadPage before page is retrieved with an invalid pageNumber completes with no value`() {
        PagedList(dataSource, pageSize = 10).apply {
            val testObserver = TestObserver<PagedList.DataSource.Page<Item>>()
            dataSource.invalidPageNumbers = intArrayOf(1)

            loadPage(1).subscribe(testObserver)
            dataSource.testScheduler.triggerActions()

            testObserver.assertNoValues()
            testObserver.assertComplete()
        }
    }

    @Test
    fun `subscribing to loadPage after page is retrieved with an invalid pageNumber completes with no value`() {
        PagedList(dataSource, pageSize = 10).apply {
            val testObserver = TestObserver<PagedList.DataSource.Page<Item>>()
            dataSource.invalidPageNumbers = intArrayOf(1)

            loadPage(1).apply {
                dataSource.testScheduler.triggerActions()
                subscribe(testObserver)
            }

            testObserver.assertNoValues()
            testObserver.assertComplete()
        }
    }

    @Test
    fun `subscribing to loadPage before page is retrieved with an valid pageNumber completes with value`() {
        PagedList(dataSource, pageSize = 10).apply {
            val testObserver = TestObserver<PagedList.DataSource.Page<Item>>()

            loadPage(1).subscribe(testObserver)
            dataSource.testScheduler.triggerActions()

            testObserver.assertValueCount(1)
            testObserver.assertComplete()
        }
    }

    @Test
    fun `subscribing to loadPage after page is retrieved with an valid pageNumber completes with value`() {
        PagedList(dataSource, pageSize = 10).apply {
            val testObserver = TestObserver<PagedList.DataSource.Page<Item>>()

            loadPage(1).apply {
                dataSource.testScheduler.triggerActions()
                subscribe(testObserver)
            }

            testObserver.assertValueCount(1)
            testObserver.assertComplete()
        }
    }

    @Test
    fun `pages of any length can be loaded when PagedList is initialised with an UNDEFINED pageSize`() {
        dataSource.apply {
            dataCount.onNext(PagedList.UNDEFINED)
            pageSizeWhenUndefined = 100
        }
        PagedList(dataSource, pageSize = PagedList.UNDEFINED).apply {
            val testObserver = TestObserver<PagedList.DataSource.Page<Item>>()

            loadPage(1).apply {
                subscribe(testObserver)
                dataSource.testScheduler.triggerActions()
            }

            verifyIndexRange(startIndex = 0, endIndex = 99, verify = VerifyMode.PEEK_LOADED)
            size `should be equal to` 100
            testObserver.assertValueCount(1)
            testObserver.assertComplete()
        }
    }

    fun `complete in-flight requests`() {
        dataSource.apply {
            testScheduler.triggerActions()
            reinitTestObserver()
        }
        nonNullDataSource.apply {
            testScheduler.triggerActions()
            reinitTestObserver()
        }
    }

    enum class IncrementType {
        INCREMENTAL, DECREMENTAL, NONE
    }

    private fun PagedList<Item>.pageForIndex(index: Int, incrementType: IncrementType): Int {
        val finalIndex = when(incrementType) {
            IncrementType.INCREMENTAL -> clamped(index + prefetchDistance)
            IncrementType.DECREMENTAL -> clamped(index - prefetchDistance)
            else -> index
        }
        return (finalIndex / pageSize) + 1
    }

    private fun PagedList<Item>.startIndexForPage(pageNumber: Int): Int = (pageSize * (pageNumber - 1))

    private fun PagedList<Item>.endIndexForPage(pageNumber: Int): Int = clamped(pageSize * (pageNumber - 1) + pageSize - 1)

    private fun PagedList<Item>.clamped(index: Int) = index.let { if (it >= size) size - 1 else it }

    private fun PagedList<Item>.verifyPageRange(startPage: Int, endPage: Int = startPage, verify: VerifyMode) {
        reset(dataSource)
        if (endPage >= startPage) {
            verifyPageRangeForward(startPage, endPage, verify)
        } else {
            verifyPageRangeReverse(startPage, endPage, verify)
        }
    }

    private fun PagedList<Item>.verifyIndexRange(startIndex: Int, endIndex: Int = startIndex, verify: VerifyMode) {
        reset(dataSource)
        if (endIndex >= startIndex) {
            verifyIndexRangeForward(startIndex, endIndex, verify)
        } else {
            verifyIndexRangeReverse(startIndex, endIndex, verify)
        }
    }

    private fun PagedList<Item>.verifyPageRangeForward(startPage: Int, endPage: Int, verify: VerifyMode) {
        verifyIndexRangeForward(startIndexForPage(startPage), endIndexForPage(endPage), verify)
    }

    private fun PagedList<Item>.verifyPageRangeReverse(startPage: Int, endPage: Int, verify: VerifyMode) {
        verifyIndexRangeReverse(endIndexForPage(startPage), startIndexForPage(endPage), verify)
    }

    private fun PagedList<Item>.verifyIndexRangeForward(startIndex: Int, endIndex: Int, verify: VerifyMode) {
        var lastQueriedPage: Int = -1
        for (index in startIndex until endIndex) {
            lastQueriedPage = verifyIndex(index, lastQueriedPage, verify, IncrementType.INCREMENTAL)
        }
    }

    private fun PagedList<Item>.verifyIndexRangeReverse(startIndex: Int, endIndex: Int, verify: VerifyMode) {
        var lastQueriedPage: Int = -1
        for (index in startIndex downTo endIndex) {
            lastQueriedPage = verifyIndex(index, lastQueriedPage, verify, IncrementType.DECREMENTAL)
        }
    }

    private fun PagedList<Item>.verifyIndex(index: Int, lastQueriedPage: Int, verify: VerifyMode, incrementType: IncrementType): Int {
        var newLastQueriedPage = lastQueriedPage
        when (verify) {
            VerifyMode.PEEK_EMPTY -> {
                peek(index).`should be null`()
                elementStateFor(index) `should equal` PagedList.ElementState.EMPTY
            }
            VerifyMode.GET_LOADING -> {
                get(index).`should be null`()
                elementStateFor(index) `should equal` PagedList.ElementState.LOADING
            }
            VerifyMode.PEEK_LOADED -> {
                peek(index).`should not be null`()
                elementStateFor(index) `should equal` PagedList.ElementState.LOADED
            }
            VerifyMode.GET_LOADED -> {
                get(index).`should not be null`()
                elementStateFor(index) `should equal` PagedList.ElementState.LOADED
            }
            VerifyMode.PEEK_DIRTY -> {
                peek(index).`should not be null`()
                elementStateFor(index) `should equal` PagedList.ElementState.DIRTY
            }
            VerifyMode.PEEK_REFRESHING -> {
                peek(index).`should not be null`()
                elementStateFor(index) `should equal` PagedList.ElementState.REFRESHING
            }
            VerifyMode.GET_REFRESHING -> {
                get(index).`should not be null`()
                elementStateFor(index) `should equal` PagedList.ElementState.REFRESHING
            }
        }

        pageForIndex(index = index, incrementType = incrementType).let { currentPage ->
            if (currentPage != lastQueriedPage && (verify == VerifyMode.GET_LOADING || verify == VerifyMode.GET_REFRESHING)) {
                argumentCaptor<PagedList.DataSource.Page.Builder<Item>>().apply {
                    verify(dataSource, times(1)).loadPage(capture())
                    firstValue.pageNumber `should be equal to` currentPage
                    firstValue.pageSize `should be equal to` pageSize
                }
                newLastQueriedPage = currentPage
            } else {
                verify(dataSource, times(0)).loadPage(any())
            }
            reset(dataSource)
        }
        return newLastQueriedPage
    }

    enum class VerifyMode {
        PEEK_EMPTY,
        GET_LOADING,
        PEEK_LOADED,
        GET_LOADED,
        PEEK_DIRTY,
        PEEK_REFRESHING,
        GET_REFRESHING
    }

    data class Item(val id: Int)

    class ItemDataSource() : TestDataSource<Item>(100, { Item(it) })
    class NonNullItemDataSource() : TestDataSource<Item>(100, { Item(it) }) {

        val emptyItem = Item(id = -1)

        override fun emptyAt(index: Int) = emptyItem
    }
}