package com.aidanvii.toolbox

val Collection<*>?.isNullOrEmpty: Boolean get() = this?.isEmpty() ?: true

val Collection<*>?.isNotEmpty: Boolean get() = this?.isNotEmpty() ?: false

fun <E> Collection<E>.getItem(index: Int): E? {
    return this.takeIf { it.isNotEmpty && index < it.size }?.elementAt(index)
}

/**
 * Returns index of the first element matching the given [predicate], or 0 if the list does not contain such element.
 */
inline fun <T> Collection<T>.indexOfFirstOrZero(predicate: (T) -> Boolean): Int {
    for ((index, item) in this.withIndex()) {
        if (predicate(item))
            return index
    }
    return 0
}

fun <T> arrayListOfSize(size: Int) = ArrayList<T>(size)

inline fun <T> arrayListOfSize(size: Int, block: ArrayList<T>.() -> Unit): ArrayList<T> {
    return ArrayList<T>(size).also {
        block.invoke(it)
    }
}

inline fun <reified T> listOfSize(size: Int, noinline elementFor: (index: Int) -> T): List<T> =
        Array(size, elementFor).toList()

/**
 * Finds the best element based on a given function.
 *
 * Returns null if empty.
 *
 * The starting best element is the first element.
 *
 * Return true from given function to indicate that the current element is better.
 */
inline fun <T : Any> Iterable<T>.findBest(currentIsBetter: (best: T, current: T) -> Boolean): T? {
    return singleOrNull() ?: firstOrNull()?.let {
        fold(it) { best, current -> if (currentIsBetter(best, current)) current else best }
    }
}

inline fun <T> Collection<T>.findIndex(predicate: (T) -> Boolean): Int? = find(predicate)?.let { indexOf(it) }

inline fun <T> diff(
        oldItems: Iterable<T>,
        newItems: Iterable<T>,
        onRemoved: Consumer<Set<T>> = {},
        onAdded: Consumer<Set<T>> = {}
) {
    (oldItems subtract newItems).let { removed -> onRemoved(removed) }
    (newItems subtract oldItems).let { added -> onAdded(added) }
}