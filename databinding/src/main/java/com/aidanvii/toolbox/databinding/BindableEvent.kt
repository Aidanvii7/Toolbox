package com.aidanvii.toolbox.databinding

import androidx.databinding.BindingConversion
import androidx.annotation.MainThread
import androidx.annotation.RestrictTo

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