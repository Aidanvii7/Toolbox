package com.aidanvii.toolbox.delegates.weak

import java.lang.ref.WeakReference
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Creates a [WeakProperty] for the given [value]
 */
fun <T> weak(value: T?): WeakProperty<T?> = WeakProperty(value)

/**
 * Creates a [WeakProperty] for the given [value], with
 */
inline fun <T> weak(value: T?, crossinline actionOnCleared: () -> Unit): WeakProperty<T?> {
    return object : WeakProperty<T?>(value) {

        override fun getValue(thisRef: Any, property: KProperty<*>): T? {
            return super.getValue(thisRef, property).also {
                if (it == null) actionOnCleared()
            }
        }
    }
}

/**
 * Simply a property delegate wrapper around [WeakReference]
 */
open class WeakProperty<T>(value: T) : ReadWriteProperty<Any, T?> {

    var weakReference: WeakReference<T?> = WeakReference(value)

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T?) {
        weakReference = WeakReference(value)
    }

    override fun getValue(thisRef: Any, property: KProperty<*>): T? = weakReference.get()
}

fun <T> weakLazy(initializer: () -> T) = object : ReadWriteProperty<Any, T> {
    var weakReference: WeakReference<T?> = WeakReference(null)

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        weakReference = WeakReference(value)
    }

    override fun getValue(thisRef: Any, property: KProperty<*>): T =
            weakReference.get() ?: initializer().also { setValue(thisRef, property, it) }
}