package com.aidanvii.toolbox.delegates.observable

infix fun <ST> ObservableProperty<ST, Boolean>.doOnFalse(
    doOnFalse: () -> Unit
): ObservableProperty<ST, Boolean> = doOnNext { if (!it) doOnFalse() }