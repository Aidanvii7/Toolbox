package com.aidanvii.common

typealias Action = () -> Unit
typealias Consumer<T> = (T) -> Unit
typealias Provider<T> = () -> T

typealias ExtensionAction<T> = T.() -> Unit
typealias ExtensionConsumer<T, I> = T.(I) -> Unit

val consumerStub: Consumer<Any> = {}
val actionStub: Action = {}