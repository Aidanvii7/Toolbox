package com.aidanvii.toolbox.redux

interface ActionDispatcher<Action : Any> {
    fun dispatch(action: Action)

    abstract class Base<Action : Any>(protected val store: Store<Action, *>) :
        ActionDispatcher<Action>
}