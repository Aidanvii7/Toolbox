package com.aidanvii.toolbox.databinding

import android.arch.lifecycle.DefaultLifecycleObserver
import android.arch.lifecycle.LifecycleOwner
import android.databinding.ViewDataBinding
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

private val defaultOnUnbind: ViewDataBinding.() -> Unit = {
    PropertyMapper.resourceIds.forEach { resourceId ->
        setVariable(resourceId, null)
    }
}

/***
 * Calls the given [onBind] function when the [LifecycleOwner] is resumed,
 * and the given (optional) [onUnbind] when the [LifecycleOwner] is paused.
 *
 * By default, [onUnbind] will attempt to unbind all possible data bound variables from the
 */
fun <Binding : ViewDataBinding> LifecycleOwner.bindWhenResumed(
    onUnbind: Binding.() -> Unit = defaultOnUnbind,
    onBind: Binding.() -> Unit
) = LifecycleBindingProperty(this, true, onBind, onUnbind)

/***
 * Calls the given [onBind] function when the [LifecycleOwner] is started,
 * and the given (optional) [onUnbind] when the [LifecycleOwner] is stopped
 */
fun <Binding : ViewDataBinding> LifecycleOwner.bindWhenStarted(
    onUnbind: Binding.() -> Unit = defaultOnUnbind,
    onBind: Binding.() -> Unit
) = LifecycleBindingProperty(this, false, onBind, onUnbind)

class LifecycleBindingProperty<Binding : ViewDataBinding>(
    lifecycleOwner: LifecycleOwner,
    private val bindWhenResumed: Boolean,
    private val onBind: Binding.() -> Unit = {},
    private val onUnbind: Binding.() -> Unit = {}
) : ReadWriteProperty<LifecycleOwner, Binding?>, DefaultLifecycleObserver {

    private var binding: Binding? = null

    init {
        lifecycleOwner.lifecycle.addObserver(this)
    }

    override fun onResume(owner: LifecycleOwner) {
        if (bindWhenResumed) {
            binding?.apply(onBind)
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        if (!bindWhenResumed) {
            binding?.apply(onBind)
        }
    }

    override fun onPause(owner: LifecycleOwner) {
        if (bindWhenResumed) {
            binding?.apply(onUnbind)
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        if (!bindWhenResumed) {
            binding?.apply(onUnbind)
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        owner.lifecycle.removeObserver(this)
    }

    override fun getValue(thisRef: LifecycleOwner, property: KProperty<*>): Binding? = binding

    override fun setValue(thisRef: LifecycleOwner, property: KProperty<*>, value: Binding?) {
        binding = value
    }
}