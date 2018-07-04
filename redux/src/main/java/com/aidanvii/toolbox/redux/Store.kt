package com.aidanvii.toolbox.redux

import com.aidanvii.toolbox.rxutils.RxSchedulers
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observable

interface Store<Action, State> {

    val stateObservable: Observable<State>

    val actionsObservable: Observable<List<Action>>

    val hasStateObservers: Boolean

    val hasActionStateObservers: Boolean

    fun dispatch(action: Action)

    fun dispatch(vararg actions: Action)

    fun dispatch(actions: List<Action>)

    interface Reducer<in Action, State> {

        /**
         * Reducer function that may apply the given [action] to the given [state] and return a new [state].
         *
         * if the [Reducer] is not interested in the [action], it should return null to indicate no changes.
         */
        fun reduce(state: State, action: Action): State?
    }

    class Base<Action, State>(
        rxSchedulers: RxSchedulers,
        private val reducer: Reducer<Action, State>,
        @Volatile private var state: State,
        startActions: List<Action> = emptyList()
    ) : Store<Action, State> {

        private val stateLock = Any()
        private val stateRelay = BehaviorRelay.createDefault(state)
        private val actionsRelay = BehaviorRelay.create<List<Action>>()
        private val stateReducerRelay = BehaviorRelay.create<List<Action>>().apply {
            observeOn(rxSchedulers.single).subscribe { actions -> reduce(actions) }
        }

        init {
            dispatch(startActions)
        }

        private fun reduce(actions: List<Action>) {
            synchronized(stateLock) {
                actions.foldRight(initial = this.state) { action, intermediateState ->
                    reducer.reduce(intermediateState, action) ?: intermediateState
                }.let { newState -> getUpdatedStateIfChanged(newState) }
            }.let { newState ->
                newState?.let { stateRelay.accept(newState) }
                actionsRelay.accept(actions)
            }
        }

        private fun getUpdatedStateIfChanged(newState: State?): State? =
            if (newState != this.state && newState != null) {
                this.state = newState
                newState
            } else null

        override val stateObservable: Observable<State> get() = stateRelay

        override val actionsObservable: Observable<List<Action>> get() = actionsRelay

        override val hasStateObservers: Boolean get() = stateRelay.hasObservers()

        override val hasActionStateObservers: Boolean get() = actionsRelay.hasObservers()

        override fun dispatch(action: Action) {
            stateReducerRelay.accept(listOf(action))
        }

        override fun dispatch(vararg actions: Action) {
            stateReducerRelay.accept(actions.toList())
        }

        override fun dispatch(actions: List<Action>) {
            if (actions.isNotEmpty()) {
                stateReducerRelay.accept(actions)
            }
        }
    }
}