package com.aidanvii.toolbox.redux

import io.reactivex.disposables.Disposable

interface Dispatcher<in Action> : Disposable {
    fun dispatch(action: Action)
}