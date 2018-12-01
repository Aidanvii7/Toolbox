package com.aidanvii.toolbox.adapterviews.databinding

import androidx.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.TESTS)
data class TestItem(
        val id: Int,
        val viewType: Int,
        override val bindingId: Int
) : BindableAdapterItem {
    override val layoutId: Int get() = viewType
}