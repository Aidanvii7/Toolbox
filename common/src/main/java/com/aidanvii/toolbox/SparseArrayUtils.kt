package com.aidanvii.toolbox

import android.util.SparseArray
import android.util.SparseIntArray

operator fun <E> SparseArray<E>.iterator() = object : Iterable<E> {

    override fun iterator(): Iterator<E> = object : Iterator<E> {
        var index = 0
        override fun hasNext(): Boolean = index < size()

        override fun next(): E {
            val key = keyAt(index++)
            return get(key)
        }
    }
}

operator fun SparseIntArray.iterator() = object : Iterable<Int> {

    override fun iterator(): Iterator<Int> = object : Iterator<Int> {
        var index = 0
        override fun hasNext(): Boolean = index < size()

        override fun next(): Int {
            val key = keyAt(index++)
            return get(key)
        }
    }
}

inline fun <E> SparseArray<E>.getWithDefault(key: Int, initializer: () -> E): E = get(key) ?: initializer()
inline fun <E> SparseArray<E>.getOrPut(key: Int, initializer: () -> E): E = get(key) ?: initializer().also { put(key, it) }
fun <E> SparseArray<E>.getNullable(key: Int): E? = get(key)
fun <E> SparseArray<E>.valueAtNullable(key: Int): E? = valueAt(key)
