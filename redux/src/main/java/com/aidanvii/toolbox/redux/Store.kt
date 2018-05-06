package com.aidanvii.toolbox.redux

import com.aidanvii.toolbox.rxutils.RxSchedulers
import com.aidanvii.toolbox.rxutils.disposable
import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import java.util.concurrent.atomic.AtomicBoolean

class Store<Action, State>(
    rxSchedulers: RxSchedulers,
    private val reducer: Reducer<Action, State>,
    @Volatile private var state: State,
    startActions: List<Action> = emptyList()
) : Dispatcher<Action> {

    private val disposed = AtomicBoolean(false)
    private var reducerRelayDisposable by disposable()

    private val stateLock = Any()
    private val stateRelay = BehaviorRelay.createDefault(state)
    private val actionStateRelay = BehaviorRelay.createDefault(ActionStatePair<Action, State>(null, state))
    private val stateReducerRelay = PublishRelay.create<Action>().apply {
        this.observeOn(rxSchedulers.single)
        reducerRelayDisposable = this.subscribe { action -> reduce(action) }
    }

    init {
        startActions.forEach { dispatch(it) }
    }

    private fun reduce(action: Action) {
        synchronized(stateLock) {
            reducer.reduce(state, action).let { newState ->
                if (newState != null && newState != state) {
                    state = newState
                    newState
                } else null
            }
        }?.let { newState ->
            stateRelay.accept(newState)
            actionStateRelay.accept(ActionStatePair(action, newState))
        }
    }

    val stateObservable: Observable<State> get() = stateRelay

    val actionStateObservable: Observable<ActionStatePair<Action, State>> get() = actionStateRelay

    val hasStateObservers: Boolean get() = stateRelay.hasObservers()

    val hasActionStateObservers: Boolean get() = actionStateRelay.hasObservers()

    override fun isDisposed() = disposed.get()

    override fun dispose() {
        if (!disposed.getAndSet(true)) {
            reducerRelayDisposable = null
        }
    }

    override fun dispatch(action: Action) {
        stateReducerRelay.accept(action)
    }

    class ActionStatePair<out Action, out State>(
        val action: Action?,
        val state: State
    )

    interface Reducer<in Action, State> {

        /**
         * Reducer function that may apply the given [action] to the given [state] and return a new [state].
         *
         * if the [Reducer] is not interested in the [action], it should return null to indicate no changes.
         */
        fun reduce(state: State, action: Action): State?
    }
}