package com.aidanvii.toolbox.rxutils

import io.reactivex.disposables.Disposable
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Convenience method for creating a [DisposeOnReassignProperty]
 */
fun disposeOnReassign(disposable: Disposable? = null): DisposeOnReassignProperty {
    return DisposeOnReassignProperty(disposable)
}

/**
 * A property delegate for [Disposable] objects that performs cleanup on assignment.
 *
 * When a new [Disposable] is assigned, [setValue] will call [Disposable.dispose] on the previous [Disposable] if non-null.
 * @param disposable the new [Disposable]
 */
class DisposeOnReassignProperty(private var disposable: Disposable?) :
    ReadWriteProperty<Any?, Disposable?> {

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Disposable?) {
        disposable?.dispose()
        disposable = value
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): Disposable? = disposable
}