package com.aidanvii.toolbox.redux

sealed class ExampleAction {
    object Foo1 : ExampleAction()
    object Foo2 : ExampleAction()
    object Bar1 : ExampleAction()
    object Bar2 : ExampleAction()
}