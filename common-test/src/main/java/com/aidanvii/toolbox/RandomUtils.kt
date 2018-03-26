package com.aidanvii.toolbox

import java.util.*

fun Random.boundInt(min: Int, max: Int) = nextInt(max - min + 1) + min