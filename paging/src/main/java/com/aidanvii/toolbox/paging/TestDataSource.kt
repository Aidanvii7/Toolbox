package com.aidanvii.toolbox.paging

import io.reactivex.Maybe
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.TestScheduler
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

open class TestDataSource<T>(val initialDataCount: Int = 0, val itemProvider: (itemIndex: Int) -> T) : PagedList.DataSource<T> {
    val testScheduler = TestScheduler()
    val publishSubject = PublishSubject.create<PagedList.DataSource.Page<T>>()

    override val dataCount = BehaviorSubject.createDefault(initialDataCount)

    var invalidPageNumbers = intArrayOf()
    var errorOnPageNumbers = intArrayOf()
    var pageSizeWhenUndefined: Int = 0
    var testObserver = TestObserver<PagedList.DataSource.Page<T>>()

    init {
        reinitTestObserver()
    }

    fun reinitTestObserver() {
        testObserver = TestObserver()
        publishSubject.subscribe(testObserver)
    }

    private fun correctPageSize(pageSize: Int): Int =
            if (pageSize == PagedList.UNDEFINED) pageSizeWhenUndefined else pageSize

    override fun emptyAt(index: Int): T? = null

    override fun loadPage(pageBuilder: PagedList.DataSource.Page.Builder<T>): Maybe<PagedList.DataSource.Page<T>> {
        return pageBuilder.run {
            if (errorOnPageNumbers.contains(pageNumber)) {
                Maybe.error<PagedList.DataSource.Page<T>>(Throwable()).subscribeOn(testScheduler).doOnSuccess { publishSubject.onComplete() }
            } else if (invalidPageNumbers.contains(pageNumber)) {
                Maybe.empty<PagedList.DataSource.Page<T>>().subscribeOn(testScheduler).doOnSuccess { publishSubject.onComplete() }
            } else {
                (pageSize * (pageNumber - 1)).let { pageStartingIndex ->
                    pageBuilder.build(
                            pageData = (pageNumber until pageNumber + correctPageSize(pageSize))
                                    .mapIndexed { indexInPage, _ -> itemProvider(pageStartingIndex + indexInPage) })
                            .let {
                                Maybe.just(it).subscribeOn(testScheduler).doOnSuccess { publishSubject.onNext(it) }
                            }
                }
            }
        }
    }
}