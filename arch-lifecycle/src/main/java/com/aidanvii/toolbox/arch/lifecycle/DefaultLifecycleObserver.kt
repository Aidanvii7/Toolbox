package com.aidanvii.toolbox.arch.lifecycle

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.OpenLifecycleObserver

interface DefaultLifecycleObserver : OpenLifecycleObserver {
    override fun onCreate(owner: LifecycleOwner) {}
    override fun onResume(owner: LifecycleOwner) {}
    override fun onPause(owner: LifecycleOwner) {}
    override fun onStart(owner: LifecycleOwner) {}
    override fun onStop(owner: LifecycleOwner) {}
    override fun onDestroy(owner: LifecycleOwner) {}
}