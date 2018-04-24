package com.aidanvii.toolbox.adapterviews.databinding

import android.support.annotation.RestrictTo
import java.util.concurrent.atomic.AtomicBoolean

@RestrictTo(RestrictTo.Scope.TESTS)
data class TestItem(
        val id: Int,
        val viewType: Int,
        override val bindingId: Int
) : BindableAdapterItem {
    override val _disposed = AtomicBoolean(false)
    override val layoutId: Int get() = viewType
}