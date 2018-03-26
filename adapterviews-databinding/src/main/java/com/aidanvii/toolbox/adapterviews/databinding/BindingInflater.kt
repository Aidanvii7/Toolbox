package com.aidanvii.toolbox.adapterviews.databinding

import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.support.annotation.LayoutRes
import android.view.LayoutInflater
import android.view.ViewGroup

object BindingInflater {
    fun <Binding : ViewDataBinding> ViewGroup.unattachedBindingOf(@LayoutRes layoutResourceId: Int): Binding =
            DataBindingUtil.inflate<Binding>(LayoutInflater.from(context), layoutResourceId, this, false).also { viewDataBinding ->
                if (viewDataBinding == null) {
                    throw IllegalStateException("provided layout resource was not a data binding layout")
                }
            }
}