package com.aidanvii.toolbox.databinding

import android.databinding.BindingConversion
import android.support.annotation.MainThread
import android.support.annotation.RestrictTo

data class BindableEvent<out T> internal constructor(
    val value: T,
    private var processed: Boolean
) {

    @MainThread
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    fun markAsProcessed() = !processed.also { processed = true }
}

@MainThread
inline fun <T, R> BindableEvent<T>.processEvent(block: (T) -> R) {
    if (markAsProcessed()) block(value)
}

@BindingConversion
fun <T> T.toBindableEvent() = BindableEvent(this, false)