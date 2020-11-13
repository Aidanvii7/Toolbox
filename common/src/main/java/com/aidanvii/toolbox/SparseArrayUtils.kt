@file:JvmName("SparseArrayUtils")

package com.aidanvii.toolbox

import android.util.SparseArray
import android.util.SparseIntArray
import androidx.collection.SparseArrayCompat

@JvmName("mutableIterableFrom")
fun <T> SparseArray<T>.asMutableIterable(): MutableIterable<Pair<Int, T>> = object : MutableIterable<Pair<Int, T>> {
    override fun iterator(): MutableIterator<Pair<Int, T>> = object : MutableIterator<Pair<Int, T>> {
        private var index = 0
        override fun hasNext(): Boolean = index < size()

        override fun next(): Pair<Int, T> {
            val key = keyAt(index++)
            return key to get(key)
        }

        override fun remove() {
            removeAt(index - 1)
        }
    }
}

@JvmName("mutableValueIterableFrom")
fun <T> SparseArray<T>.asMutableValueIterable(): MutableIterable<T> = object : MutableIterable<T> {
    override fun iterator(): MutableIterator<T> = object : MutableIterator<T> {
        private var index = 0
        override fun hasNext(): Boolean = index < size()

        override fun next(): T {
            val key = keyAt(index++)
            return get(key)
        }

        override fun remove() {
            removeAt(index - 1)
        }
    }
}

@JvmName("mutableIterableFrom")
fun SparseIntArray.asMutableIterable(): MutableIterable<Pair<Int, Int>> = object : MutableIterable<Pair<Int, Int>> {
    override fun iterator(): MutableIterator<Pair<Int, Int>> = object : MutableIterator<Pair<Int, Int>> {
        private var index = 0
        override fun hasNext(): Boolean = index < size()

        override fun next(): Pair<Int, Int> {
            val key = keyAt(index++)
            return key to get(key)
        }

        override fun remove() {
            removeAt(index - 1)
        }
    }
}

@JvmName("mutableValueIterableFrom")
fun SparseIntArray.asMutableValueIterable(): MutableIterable<Int> = object : MutableIterable<Int> {
    override fun iterator(): MutableIterator<Int> = object : MutableIterator<Int> {
        private var index = 0
        override fun hasNext(): Boolean = index < size()

        override fun next(): Int {
            val key = keyAt(index++)
            return get(key)
        }

        override fun remove() {
            removeAt(index - 1)
        }
    }
}

@JvmName("mutableIterableFrom")
fun <T> SparseArrayCompat<T>.asMutableIterable(): MutableIterable<Pair<Int, T>> = object : MutableIterable<Pair<Int, T>> {
    override fun iterator(): MutableIterator<Pair<Int, T>> = object : MutableIterator<Pair<Int, T>> {
        private var index = 0
        private var next: T? = null
        override fun hasNext(): Boolean {
            val keyForNext = keyAt(index + 1)
            next = get(keyForNext) ?: return false
            return index < size()
        }

        override fun next(): Pair<Int, T> {
            val keyForNext = keyAt(index++)
            return keyForNext to next!!
        }

        override fun remove() {
            removeAt(index - 1)
        }
    }
}

@JvmName("mutableValueIterableFrom")
fun <T> SparseArrayCompat<T>.asMutableValueIterable(): MutableIterable<T> = object : MutableIterable<T> {
    override fun iterator(): MutableIterator<T> = object : MutableIterator<T> {
        private var index = 0
        private var next: T? = null
        override fun hasNext(): Boolean {
            val keyForNext = keyAt(index + 1)
            next = get(keyForNext) ?: return false
            return index < size()
        }

        override fun next(): T = next!!

        override fun remove() {
            removeAt(index - 1)
        }
    }
}

inline fun <E> SparseArray<E>.getWithDefault(key: Int, initializer: () -> E): E = get(key) ?: initializer()
inline fun <E> SparseArray<E>.getOrPut(key: Int, initializer: () -> E): E = get(key) ?: initializer().also { put(key, it) }
fun <E> SparseArray<E>.getNullable(key: Int): E? = get(key)
fun <E> SparseArray<E>.valueAtNullable(key: Int): E? = valueAt(key)
