package com.aidanvii.toolbox

import android.util.SparseArray
import android.util.SparseIntArray
import com.nhaarman.mockito_kotlin.spy
import de.jodamob.reflect.SuperReflect
import java.util.*

// TODO move this to test artifact and reuse!

fun <T> Any.testableSparseArray(variableName: String, size: Int = 10): TestableSparseArray<T> {
    val testableSparseArray = TestableSparseArray<T>(size)
    SuperReflect.on(this).set(variableName, testableSparseArray)
    return testableSparseArray
}

fun <T> Any.spiedTestableSparseArray(variableName: String, size: Int = 10): TestableSparseArray<T> {
    val testableSparseArray = spy(TestableSparseArray<T>(size))
    SuperReflect.on(this).set(variableName, testableSparseArray)
    return testableSparseArray
}

fun Any.testableSparseIntArray(variableName: String, size: Int = 10): TestableSparseIntArray {
    val testableSparseArray = TestableSparseIntArray(size)
    SuperReflect.on(this).set(variableName, testableSparseArray)
    return testableSparseArray
}

fun Any.spiedTestableSparseIntArray(variableName: String, size: Int = 10): TestableSparseIntArray {
    val testableSparseArray = spy(TestableSparseIntArray(size))
    SuperReflect.on(this).set(variableName, testableSparseArray)
    return testableSparseArray
}

class TestableSparseArray<E>(size: Int) : SparseArray<E>(size) {

    val map = TreeMap<Int, E>()

    private fun getKeyAt(index: Int): Int = ArrayList(map.keys)[index]

    private fun getValueAt(index: Int): E? = ArrayList(map.values)[index]

    private fun getIndexForKey(key: Int): Int = ArrayList(map.keys).indexOf(key)

    private fun getIndexForValue(value: E): Int = ArrayList(map.values).indexOf(value)

    override fun get(key: Int): E? = map[key]

    override fun get(key: Int, valueIfKeyNotFound: E): E? = map[key] ?: valueIfKeyNotFound

    override fun delete(key: Int) {
        map.remove(key)
    }

    override fun remove(key: Int) {
        delete(key)
    }

    override fun removeAt(index: Int) {
        map.remove(getKeyAt(index))
    }

    override fun removeAtRange(index: Int, size: Int) {
        val endIndex = index + size
        for (curIndex in index until endIndex) {
            map.remove(getKeyAt(curIndex))
        }
    }

    override fun put(key: Int, value: E) {
        map[key] = value
    }

    override fun size(): Int = map.size

    override fun keyAt(index: Int): Int = getKeyAt(index)

    override fun valueAt(index: Int): E? = getValueAt(index)

    override fun setValueAt(index: Int, value: E) {
        map[getKeyAt(index)] = value
    }

    override fun indexOfKey(key: Int): Int = getIndexForKey(key)

    override fun indexOfValue(value: E): Int = getIndexForValue(value)

    override fun clear() = map.clear()

    override fun append(key: Int, value: E) {
        map[key] = value
    }
}

class TestableSparseIntArray(size: Int) : SparseIntArray(size) {

    val map = TreeMap<Int, Int>()

    private fun getKeyAt(index: Int): Int = ArrayList(map.keys)[index]

    private fun getValueAt(index: Int): Int = ArrayList(map.values)[index]

    private fun getIndexForKey(key: Int): Int = ArrayList(map.keys).indexOf(key)

    private fun getIndexForValue(value: Int): Int = ArrayList(map.values).indexOf(value)

    override fun get(key: Int): Int = map[key]!!

    override fun get(key: Int, valueIfKeyNotFound: Int): Int = map[key] ?: valueIfKeyNotFound

    override fun delete(key: Int) {
        map.remove(key)
    }

    override fun removeAt(index: Int) {
        map.remove(getKeyAt(index))
    }

    override fun put(key: Int, value: Int) {
        map[key] = value
    }

    override fun size(): Int = map.size

    override fun keyAt(index: Int): Int = getKeyAt(index)

    override fun valueAt(index: Int): Int = getValueAt(index)

    override fun indexOfKey(key: Int): Int = getIndexForKey(key)

    override fun indexOfValue(value: Int): Int = getIndexForValue(value)

    override fun clear() = map.clear()

    override fun append(key: Int, value: Int) {
        map[key] = value
    }
}