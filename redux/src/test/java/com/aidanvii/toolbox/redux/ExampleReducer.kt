package com.aidanvii.toolbox.redux
import com.aidanvii.toolbox.redux.ExampleAction.Bar1
import com.aidanvii.toolbox.redux.ExampleAction.Bar2
import com.aidanvii.toolbox.redux.ExampleAction.Foo1
import com.aidanvii.toolbox.redux.ExampleAction.Foo2

class ExampleReducer : Store.Reducer<ExampleAction, ExampleState> {
    override fun reduce(state: ExampleState, action: ExampleAction): ExampleState? {
        return when (action) {
            Foo1 -> null
            Foo2 -> null
            Bar1 -> if (state.fooBar1) null else state.copy(fooBar1 = true)
            Bar2 -> if (state.fooBar2) null else state.copy(fooBar2 = true)
        }
    }
}