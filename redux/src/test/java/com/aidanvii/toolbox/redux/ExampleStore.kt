package com.aidanvii.toolbox.redux

import com.aidanvii.toolbox.rxutils.RxSchedulers

class ExampleStore(
    val exampleReducer: ExampleReducer = ExampleReducer(),
    val rxSchedulers: RxSchedulers = RxSchedulers.TestSchedulers()
) : Store<ExampleAction, ExampleState> by Store.Base(
    reducer = exampleReducer,
    rxSchedulers = rxSchedulers,
    state = ExampleState.DEFAULT
)