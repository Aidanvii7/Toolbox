package com.aidanvii.toolbox.paging

import com.aidanvii.toolbox.Action
import com.aidanvii.toolbox.Consumer
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.BehaviorSubject

internal class Elements<T>(
        publishChangesOnInit: Boolean,
        private val synchroniseAccess: Boolean,
        onError: Consumer<Throwable>,
        private val dataSource: PagedList.DataSource<T>,
        private val changeSubject: BehaviorSubject<List<T?>>
) {
    private val elementList: MutableList<T?>
    private val stateList: MutableList<PagedList.ElementState>
    private var changed = false

    init {
        elementList = mutableListOf()
        stateList = mutableListOf()
        dataSource.dataCount.subscribeBy(onError = onError, onNext = this::resizeIfNecessary)
        if (publishChangesOnInit) {
            changed = true
            publishIfChanged()
        }
    }

    val size: Int get() = elementList.size

    /**
     * Returns the index of the last item in the list or -1 if the list is empty.
     */
    val lastIndex: Int get() = elementList.lastIndex

    fun get(index: Int): T? = elementList.get(index)

    fun isEmpty(index: Int): Boolean = stateList[index] == PagedList.ElementState.EMPTY

    fun isEmptyOrDirty(index: Int): Boolean = when (stateList[index]) {
        PagedList.ElementState.EMPTY -> true
        PagedList.ElementState.DIRTY -> true
        else -> false
    }

    fun setEmpty(index: Int) {
        setElementAndState(index, PagedList.ElementState.EMPTY) {
            setElementEmpty(index)
        }
    }

    fun setLoading(index: Int) {
        setElementAndState(index, PagedList.ElementState.LOADING) {
            setElementEmpty(index)
        }
    }

    fun setDirtyIfLoaded(index: Int) {
        if (elementStateFor(index) == PagedList.ElementState.LOADED) {
            setElementAndState(index, PagedList.ElementState.DIRTY) { }
        }
    }

    fun setRefreshingIfDirty(index: Int) {
        if (elementStateFor(index) == PagedList.ElementState.DIRTY) {
            setElementAndState(index, PagedList.ElementState.REFRESHING) { }
        }
    }

    fun setLoaded(index: Int, element: T) {
        setElementAndState(index, PagedList.ElementState.LOADED) {
            setElement(index, element)
        }
    }

    fun invalidateAsEmpty(startIndex: Int, endIndex: Int) {
        for (index in startIndex..endIndex) {
            setEmpty(index)
        }
    }

    fun invalidateLoadedAsDirty(startIndex: Int, endIndex: Int) {
        for (index in startIndex..endIndex) {
            setDirtyIfLoaded(index)
        }
    }

    fun resizeIfNecessary(size: Int) {
        if (size != PagedList.UNDEFINED) {
            if (shouldGrow(size)) grow(size) else if (shouldShrink(size)) shrink(size)
        }
    }

    fun growIfNecessary(size: Int) {
        if (shouldGrow(size)) grow(size)
    }

    fun publishIfChanged() {
        syncWhen(synchroniseAccess, this) {
            if (changed) {
                changed = false
                elementList.toList()
            } else null
        }?.let {
            changeSubject.onNext(it)
        }
    }

    fun asList(): List<T?> = elementList.toList()

    fun indexInBounds(index: Int): Boolean = (index < size)

    fun elementStateFor(index: Int): PagedList.ElementState = stateList[index]

    inline fun <R> runSynchronised(block: Elements<T>.() -> R): R {
        return syncWhen(synchroniseAccess, this, { block() }).also {
            publishIfChanged()
        }
    }

    private inline fun setElementAndState(index: Int, elementState: PagedList.ElementState, setElement: Action) {
        if (stateList[index] != elementState) {
            changed = true
            stateList[index] = elementState
            setElement()
        }
    }

    private fun setElementEmpty(index: Int) {
        dataSource.emptyAt(index).let { empty ->
            setElement(index, empty)
        }
    }

    private fun setElement(index: Int, element: T?) {
        if (elementList[index] != element) {
            elementList[index] = element
            changed = true
        }
    }

    private fun shouldGrow(size: Int) = this.size < size

    private fun shouldShrink(size: Int) = this.size > size

    private fun grow(newSize: Int) {
        (elementList.size).let { startingIndex ->
            (startingIndex until newSize).forEach {
                elementList.add(dataSource.emptyAt(it))
                stateList.add(PagedList.ElementState.EMPTY)
            }
        }
        changed = true
    }

    private fun shrink(newSize: Int) {
        (newSize until size).forEach {
            elementList.removeAt(newSize)
            stateList.removeAt(newSize)
        }
        changed = true
    }

    private inline fun <R> syncWhen(predicate: Boolean, lock: Any, block: () -> R): R = if (predicate) synchronized(lock, block) else block()
}