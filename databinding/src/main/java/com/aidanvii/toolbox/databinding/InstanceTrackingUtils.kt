package com.aidanvii.toolbox.databinding

import android.databinding.BindingAdapter
import android.support.annotation.IdRes
import android.support.annotation.RestrictTo
import android.view.View
import android.databinding.adapters.ListenerUtil as FrameworkListenerUtil

interface ListenerUtilDelegate {
    fun <T> trackListener(view: View, @IdRes listenerResourceId: Int, listener: T? = null): T?
}

internal class AndroidListenerUtilDelegate : ListenerUtilDelegate {
    override fun <T> trackListener(view: View, listenerResourceId: Int, listener: T?): T? =
            FrameworkListenerUtil.trackListener(view, listener, listenerResourceId)
}

object ListenerUtil : ListenerUtilDelegate {

    private var realDelegate: ListenerUtilDelegate = AndroidListenerUtilDelegate()
    private var delegate: ListenerUtilDelegate = realDelegate

    override fun <T> trackListener(view: View, @IdRes listenerResourceId: Int, listener: T?): T? =
            delegate.trackListener(view, listenerResourceId, listener)

    @RestrictTo(RestrictTo.Scope.TESTS)
    fun stubDelegate(stubDelegate: ListenerUtilDelegate) {
        delegate = stubDelegate
    }

    @RestrictTo(RestrictTo.Scope.TESTS)
    fun unstubDelegate() {
        delegate = realDelegate
    }
}

/**
 * Designed to track objects passed as optional parameters via static [BindingAdapter] methods.
 *
 * Only one instance per instanceResId can be tracked at a time.
 *
 * This is useful for add*Listener and remove*Listener methods,
 * where associated [BindingAdapter] methods must replace the previously added listener, or remove it.
 *
 * It is a wrapper around [ListenerUtil.trackListener], with less specific naming, as the instance being tracked does not
 * necessarily need to be a listener.
 *
 * Instances are tracked by referenctial equality rather than structural equality - that is,
 * a new instance with the same structural equality but different referential equality will trigger an [onDetached] > [onAttached] cycle.
 *
 * Example usage:
 * ```
 * @BindingAdapter("textWatcher")
 * fun TextView.setTextWatcher(textWatcher: TextWatcher?) {
 *     trackInstance(
 *              newInstance = textWatcher,
 *              instanceResId = R.id.textWatcher,
 *              onAttached = {
 *                  // [it] is the newly added listener, called when non-null.
 *                  addTextChangedListener(it)
 *              },
 *              onDetached = {
 *                  // [it] is the previously added listener, called when non-null.
 *                  removeTextChangedListener(it)
 *              })
 * }
 *
 * ```
 */
inline fun <V : View, I> V.trackInstance(
        newInstance: I?,
        @IdRes instanceResId: Int,
        onDetached: V.(I) -> Unit = {},
        onAttached: V.(I) -> Unit = {}
) {
    ListenerUtil.trackListener(this, instanceResId, newInstance).let { oldInstance ->
        if (oldInstance !== newInstance) {
            oldInstance?.let { onDetached(it) }
            newInstance?.let { onAttached(it) }
        }
    }
}

/**
 * Like [trackInstance], though tracks objects based on structural equality.
 */
inline fun <V : View, I> V.trackValue(
        newValue: I?,
        @IdRes valueResId: Int,
        onNewValue: V.(I) -> Unit = {},
        onOldValue: V.(I) -> Unit = {}
) {
    ListenerUtil.trackListener(this, valueResId, newValue).let { oldValue ->
        if (oldValue != newValue) {
            oldValue?.let { onOldValue(it) }
            newValue?.let { onNewValue(it) }
        }
    }
}

fun <Value> View.getTrackedValue(@IdRes valueResId: Int): Value? {
    return ListenerUtil.trackListener<Value>(this, valueResId, null)?.also { currentValue ->
        ListenerUtil.trackListener(this, valueResId, currentValue)
    }
}

fun <Value> View.setTrackedValue(@IdRes valueResId: Int, value: Value) {
    ListenerUtil.trackListener<Value>(this, valueResId, value)
}

inline fun <Value> View.onTrackedValue(
        @IdRes instanceResId: Int,
        onNextValue: (Value) -> Unit
) {
    onTrackedValue<Value>(null, instanceResId, onNextValue)
}

inline fun <Value> View.onTrackedValue(
        newValue: Value?,
        @IdRes instanceResId: Int,
        onNextValue: (Value) -> Unit
) {
    ListenerUtil.trackListener(this, instanceResId, newValue).let { oldValue ->
        if (newValue == null) {
            ListenerUtil.trackListener(this, instanceResId, oldValue)
            if (oldValue != newValue) {
                onNextValue(oldValue)
            }
        } else if (oldValue != newValue) {
            onNextValue(newValue)
        }
    }
}

inline fun <V : View, I> V.onTrackedInstance(
        @IdRes instanceResId: Int,
        provideNewInstance: V.() -> I,
        onInstance: I.() -> Unit
) {
    val instance = ListenerUtil.trackListener<I>(this, instanceResId)
            ?: provideNewInstance()
    ListenerUtil.trackListener(this, instanceResId, instance)
    instance.onInstance()
}