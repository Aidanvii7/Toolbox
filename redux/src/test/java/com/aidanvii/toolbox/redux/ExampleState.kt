package com.aidanvii.toolbox.redux

data class ExampleState(
    val fooBar1: Boolean,
    val fooBar2: Boolean
) {
    companion object {
        val DEFAULT = ExampleState(
            fooBar1 = false,
            fooBar2 = false
        )
    }
}