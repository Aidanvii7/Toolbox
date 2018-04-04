package com.aidanvii.toolbox.paging

import com.aidanvii.toolbox.checkAboveMin
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

/**
 * A base implementation of [PagedList.DataSource] with a mechanism for publishing changes to the [dataCount] via [publishDataCount]
 */
abstract class BaseDataSource<T>(initialDataCount: Int = 0) : PagedList.DataSource<T> {
    init {
        initialDataCount.checkAboveMin(-1, "initialDataCount must be 0 or above")
    }

    private val dataCountSubject = BehaviorSubject.createDefault(initialDataCount)

    final override val dataCount: Observable<Int> = dataCountSubject.distinctUntilChanged()

    protected fun publishDataCount(dataCount: Int) {
        dataCountSubject.onNext(dataCount)
    }
}