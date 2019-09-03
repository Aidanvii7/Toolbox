package com.aidanvii.toolbox.delegates.observable

infix fun <ST> ObservableProperty<ST, Boolean>.doOnTrue(
    doOnTrue: () -> Unit
): ObservableProperty<ST, Boolean> = doOnNext { if (it) doOnTrue() }