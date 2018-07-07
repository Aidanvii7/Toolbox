package com.aidanvii.toolbox.redux

import com.aidanvii.toolbox.rxutils.RxSchedulers
import com.aidanvii.toolbox.rxutils.disposeOnReassign
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.reflect.KClass

abstract class SideEffects<Action : Any, State>(
    private val store: Store<Action, State>,
    protected val rxSchedulers: RxSchedulers
) : Disposable {

    private var storeDisposable by disposeOnReassign()
    private val initialised = AtomicBoolean(false)
    private val disposed = AtomicBoolean(false)

    fun init() {
        if (!initialised.getAndSet(true)) {
            storeDisposable = subscribeToStore()
        }
    }

    final override fun isDisposed() = disposed.get()

    final override fun dispose() {
        if (!disposed.getAndSet(true)) {
            storeDisposable = null
            onDisposed()
        }
    }

    private fun subscribeToStore(): Disposable {
        return store.actionsObservable
            .subscribeOn(rxSchedulers.single)
            .subscribeBy { actions ->
                handleActions(actions)
            }
    }

    private fun handleActions(actions: List<Action>) {
        actionDispatcherFor(actions).let { actionDispatcher ->
            actions.forEach { action -> actionDispatcher.handleAction(action) }
        }
    }

    open fun actionDispatcherFor(actions: List<Action>): ActionDispatcher<Action> =
        SimpleActionDispatcher(store)

    protected fun List<Action>.bufferedActionDispatcherWhenContains(vararg actionTypes: KClass<*>): ActionDispatcher<Action> =
        if (containsActionTypes(*actionTypes)) BufferedActionDispatcher(
            store,
            actionTypes.size
        ) else SimpleActionDispatcher(store)

    protected fun List<Action>.containsActionTypes(vararg actionTypes: KClass<*>): Boolean =
        actionTypes.all { actionType ->
            firstOrNull { action ->
                action::class == actionType
            } != null
        }

    abstract fun ActionDispatcher<Action>.handleAction(action: Action)

    abstract fun onDisposed()
}
