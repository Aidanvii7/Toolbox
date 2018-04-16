package com.aidanvii.toolbox.databinding

import android.databinding.BindingAdapter
import android.support.annotation.IdRes
import android.support.annotation.RestrictTo
import android.view.View
import android.databinding.adapters.ListenerUtil as FrameworkListenerUtil

interface ViewTagTrackerDelegate {
    fun <T> trackObject(view: View, @IdRes objectResourceId: Int, `object`: T? = null): T?
    fun <T> getObject(view: View, @IdRes objectResourceId: Int): T?
}

internal class AndroidViewTagTrackerDelegate : ViewTagTrackerDelegate {
    override fun <T> trackObject(view: View, objectResourceId: Int, `object`: T?): T? =
        FrameworkListenerUtil.trackListener(view, `object`, objectResourceId)

    override fun <T> getObject(view: View, objectResourceId: Int): T? =
        FrameworkListenerUtil.getListener(view, objectResourceId)
}

object ViewTagTracker : ViewTagTrackerDelegate {

    private val androidDelegate = AndroidViewTagTrackerDelegate()
    private var delegate: ViewTagTrackerDelegate = androidDelegate

    override fun <T> trackObject(view: View, @IdRes objectResourceId: Int, `object`: T?): T? =
        delegate.trackObject(view, objectResourceId, `object`)

    override fun <T> getObject(view: View, objectResourceId: Int): T? =
        delegate.getObject(view, objectResourceId)

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
 * It is a wrapper around [ViewTagTracker.trackObject], with less specific naming, as the instance being tracked does not
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
    ViewTagTracker.trackObject(this, instanceResId, newInstance).let { oldInstance ->
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
    ViewTagTracker.trackObject(this, valueResId, newValue).let { oldValue ->
        if (oldValue != newValue) {
            oldValue?.let { onOldValue(oldValue) }
            newValue?.let { onNewValue(newValue) }
        }
    }
}

/**
 * Stores the given [newEvent] value on the receiver [View] if [newEvent] is non-null.
 *
 * Unlike [trackInstance] and [trackValue], the [onNewEvent] function
 * will always fire as long as the given [newEvent] is non-null.
 */
inline fun <V : View, I> V.trackEvent(
    newEvent: I?,
    @IdRes eventResId: Int,
    onNewEvent: V.(I) -> Unit = {}
) {
    ViewTagTracker.trackObject(this, eventResId, newEvent).let { _ ->
        newEvent?.let { onNewEvent(newEvent) }
    }
}

fun <Value> View.getTrackedValue(@IdRes valueResId: Int): Value? =
    ViewTagTracker.getObject<Value>(this, valueResId)

fun <Value> View.setTrackedValue(
    @IdRes valueResId: Int,
    value: Value
) {
    ViewTagTracker.trackObject(this, valueResId, value)
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
    ViewTagTracker.trackObject(this, instanceResId, newValue).let { oldValue ->
        if (newValue == null) {
            ViewTagTracker.trackObject(this, instanceResId, oldValue)
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
    (ViewTagTracker.getObject<I>(this, instanceResId) ?: provideNewInstance()).onInstance()
}