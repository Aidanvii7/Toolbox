package com.aidanvii.toolbox.databinding

import androidx.databinding.BindingAdapter
import androidx.annotation.IdRes
import androidx.annotation.RestrictTo
import android.view.View
import androidx.databinding.adapters.ListenerUtil as FrameworkListenerUtil

interface ViewTagTrackerDelegate {
    fun <T> trackInstance(view: View, @IdRes instanceResourceId: Int, instance: T? = null): T?
    fun <T> getInstance(view: View, @IdRes instanceResourceId: Int): T?
}

internal class AndroidViewTagTrackerDelegate : ViewTagTrackerDelegate {
    override fun <T> trackInstance(view: View, instanceResourceId: Int, instance: T?): T? =
        FrameworkListenerUtil.trackListener(view, instance, instanceResourceId)

    override fun <T> getInstance(view: View, instanceResourceId: Int): T? =
        FrameworkListenerUtil.getListener(view, instanceResourceId)
}

object ViewTagTracker : ViewTagTrackerDelegate {

    private val androidDelegate = AndroidViewTagTrackerDelegate()
    private var delegate: ViewTagTrackerDelegate = androidDelegate

    override fun <T> trackInstance(view: View, @IdRes instanceResourceId: Int, instance: T?): T? =
        delegate.trackInstance(view, instanceResourceId, instance)

    override fun <T> getInstance(view: View, instanceResourceId: Int): T? =
        delegate.getInstance(view, instanceResourceId)

    @RestrictTo(RestrictTo.Scope.TESTS)
    fun stubDelegate(stubDelegate: ViewTagTrackerDelegate) {
        delegate = stubDelegate
    }

    @RestrictTo(RestrictTo.Scope.TESTS)
    fun unstubDelegate() {
        delegate = androidDelegate
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
 * It is a wrapper around [ViewTagTracker.trackInstance], with less specific naming, as the instance being tracked does not
 * necessarily need to be a listener.
 *
 * Instances are tracked by referential equality rather than structural equality - that is,
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
 *              }
 *     )
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
    ViewTagTracker.trackInstance(this, instanceResId, newInstance).let { oldInstance ->
        if (oldInstance !== newInstance) {
            oldInstance?.let { onDetached(oldInstance) }
            newInstance?.let { onAttached(newInstance) }
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
    ViewTagTracker.trackInstance(this, valueResId, newValue).let { oldValue ->
        if (oldValue != newValue) {
            oldValue?.let { onOldValue(oldValue) }
            newValue?.let { onNewValue(newValue) }
        }
    }
}

fun <Value> View.getTrackedValue(@IdRes valueResId: Int): Value? =
    ViewTagTracker.getInstance<Value>(this, valueResId)

fun <Value> View.setTrackedValue(
    @IdRes valueResId: Int,
    value: Value
) {
    ViewTagTracker.trackInstance(this, valueResId, value)
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
    ViewTagTracker.trackInstance(this, instanceResId, newValue).let { oldValue ->
        if (newValue == null) {
            ViewTagTracker.trackInstance(this, instanceResId, oldValue)
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
    (ViewTagTracker.getInstance<I>(this, instanceResId) ?: provideNewInstance()).onInstance()
}