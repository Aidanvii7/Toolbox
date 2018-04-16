package com.aidanvii.toolbox.databinding

import android.databinding.BindingConversion

data class ViewEvent<out T>(val value: T) {

    private var processed = false

    fun <R> processEvent(block: (T) -> R) {
        if (!processed) {
            processed = true
            block(value)
        }
    }
}

@BindingConversion
fun <T> T.toViewEvent() = ViewEvent(this)