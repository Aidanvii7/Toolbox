package com.aidanvii.toolbox.paging

import com.aidanvii.toolbox.Action
import com.aidanvii.toolbox.Consumer
import io.reactivex.Maybe
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.MaybeSubject

sealed class PageLoader<T> : Disposable {
    internal lateinit var dataSource: PagedList.DataSource<T>
    protected val disposables = CompositeDisposable()

    abstract internal fun loadPage(
            pageBuilder: PagedList.DataSource.Page.Builder<T>,
            onDisposeOrErrorOrComplete: Action,
            onError: Consumer<Throwable>,
            onSuccess: Consumer<PagedList.DataSource.Page<T>>
    ): Maybe<PagedList.DataSource.Page<T>>

    open internal fun accessed(page: Int) {}

    override fun dispose() {
        disposables.clear()
    }

    override fun isDisposed(): Boolean = disposables.isDisposed

    class Default<T> : PageLoader<T>() {
        override fun loadPage(
                pageBuilder: PagedList.DataSource.Page.Builder<T>,
                onDisposeOrErrorOrComplete: Action,
                onError: Consumer<Throwable>,
                onSuccess: Consumer<PagedList.DataSource.Page<T>>
        ): Maybe<PagedList.DataSource.Page<T>> =
                MaybeSubject.create<PagedList.DataSource.Page<T>>().also { pageSubject ->
                    disposables += pageBuilder.run {
                        dataSource.loadPage(PagedList.DataSource.Page.Builder(pageNumber, pageSize))
                                .doOnDispose({ onDisposeOrErrorOrComplete() })
                                .doOnSuccess(pageSubject::onSuccess)
                                .doOnComplete(pageSubject::onComplete)
                                .subscribeBy(
                                        onError = { throwable ->
                                            onError.invoke(throwable)
                                            onDisposeOrErrorOrComplete()
                                        },
                                        onComplete = onDisposeOrErrorOrComplete,
                                        onSuccess = onSuccess)
                    }
                }
    }
}