package com.aidanvii.toolbox.arch.lifecycle

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OpenLifecycleObserver

interface DefaultLifecycleObserver : OpenLifecycleObserver {
    override fun onCreate(owner: LifecycleOwner) {}
    override fun onResume(owner: LifecycleOwner) {}
    override fun onPause(owner: LifecycleOwner) {}
    override fun onStart(owner: LifecycleOwner) {}
    override fun onStop(owner: LifecycleOwner) {}
    override fun onDestroy(owner: LifecycleOwner) {}

    fun removeFrom(owner: LifecycleOwner) {
        owner.lifecycle.removeObserver(this)
    }
}

