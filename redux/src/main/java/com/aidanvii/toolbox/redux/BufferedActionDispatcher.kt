package com.aidanvii.toolbox.redux

import io.reactivex.processors.UnicastProcessor

class BufferedActionDispatcher<Action : Any>(
    store: Store<Action, *>,
    bufferCount: Int
) : ActionDispatcher.Base<Action>(store) {

    private val actionProcessor = UnicastProcessor.create<Action>()

    init {
        actionProcessor
            .onBackpressureBuffer(bufferCount)
            .buffer(bufferCount)
            .subscribe { actions -> store.dispatch(actions) }
    }

    override fun dispatch(action: Action) {
        actionProcessor.onNext(action)
    }
}