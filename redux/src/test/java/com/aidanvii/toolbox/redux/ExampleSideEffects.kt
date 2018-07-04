package com.aidanvii.toolbox.redux

import com.aidanvii.toolbox.redux.ExampleAction.Foo1
import com.aidanvii.toolbox.redux.ExampleAction.Foo2
import com.aidanvii.toolbox.rxutils.RxSchedulers

val actionStub = {}

class ExampleSideEffects(
    store: Store<ExampleAction, ExampleState>,
    rxSchedulers: RxSchedulers
) : SideEffects<ExampleAction, ExampleState>(store, rxSchedulers) {

    override fun ActionDispatcher<ExampleAction>.handleAction(action: ExampleAction) {
        when (action) {
            Foo1 -> dispatch(ExampleAction.Bar1)
            Foo2 -> dispatch(ExampleAction.Bar2)
            else -> actionStub
        }
    }

    override fun actionDispatcherFor(actions: List<ExampleAction>): ActionDispatcher<ExampleAction> =
        actions.bufferedActionDispatcherWhenContains(Foo1::class, Foo2::class)

    override fun onDisposed() {}
}