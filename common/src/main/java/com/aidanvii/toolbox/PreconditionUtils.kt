package com.aidanvii.toolbox

import java.lang.IllegalStateException
import java.lang.IndexOutOfBoundsException

fun Boolean.throwIfTrue(errorWhenFalse: String = "An error has occurred") {
    if (this) {
        throw IllegalStateException(errorWhenFalse)
    }
}

fun Boolean.throwIfFalse(errorWhenFalse: String = "An error has occurred") {
    if (!this) {
        throw IllegalStateException(errorWhenFalse)
    }
}

fun Int.checkBelowMax(max: Int, message: String = "value is out of bounds"): Int {
    return checkInBounds(Int.MIN_VALUE, max, message)
}

fun Int.checkAboveMin(min: Int, message: String = "value is out of bounds"): Int {
    return checkInBounds(min, Int.MAX_VALUE, message)
}

fun Int.checkInBounds(min: Int, max: Int, message: String = "value is out of bounds"): Int {
    if (this in min..max) {
        return this
    }
    throw IndexOutOfBoundsException(message)
}
