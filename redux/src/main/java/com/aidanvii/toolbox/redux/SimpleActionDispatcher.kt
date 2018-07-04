package com.aidanvii.toolbox.redux

class SimpleActionDispatcher<Action : Any>(
    store: Store<Action, *>
) : ActionDispatcher.Base<Action>(store) {
    override fun dispatch(action: Action) {
        store.dispatch(action)
    }
}