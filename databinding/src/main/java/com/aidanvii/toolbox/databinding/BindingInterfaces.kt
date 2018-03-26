package com.aidanvii.toolbox.databinding

interface BindingConsumer<in T> {
    fun invoke(value: T)
}

interface IntBindingConsumer {
    fun invoke(value: Int)
}

interface BindingAction {
    fun invoke()
}