package com.aidanvii.toolbox.databinding

import androidx.databinding.Bindable
import androidx.databinding.Observable

internal object AutoBRs : Observable {
    override fun removeOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {}
    override fun addOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {}


    @get:Bindable
    val viewModel
        get() = "viewModel"
}