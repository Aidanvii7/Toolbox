package com.aidanvii.toolbox.arch.lifecycle

import androidx.lifecycle.LifecycleOwner
import androidx.annotation.RestrictTo

@Suppress("UNCHECKED_CAST")
interface TypedDefaultLifecycleObserver<T : LifecycleOwner> :
    DefaultLifecycleObserver {

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    override fun onCreate(owner: LifecycleOwner) {
        onCreateTyped(owner as T)
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    override fun onResume(owner: LifecycleOwner) {
        onResumeTyped(owner as T)
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    override fun onPause(owner: LifecycleOwner) {
        onPauseTyped(owner as T)
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    override fun onStart(owner: LifecycleOwner) {
        onStartTyped(owner as T)
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    override fun onStop(owner: LifecycleOwner) {
        onStopTyped(owner as T)
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    override fun onDestroy(owner: LifecycleOwner) {
        onDestroyTyped(owner as T)
    }

    fun onCreateTyped(owner: T) {}
    fun onResumeTyped(owner: T) {}
    fun onPauseTyped(owner: T) {}
    fun onStartTyped(owner: T) {}
    fun onStopTyped(owner: T) {}
    fun onDestroyTyped(owner: T) {}
}