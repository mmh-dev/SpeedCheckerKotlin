package com.mmh.speedcheckerkotlin.utils

import java.text.DecimalFormat
import kotlin.math.pow

fun Int.toMbs(): String {
    return if (this == 0 ) "0.0 Mb/s"
    else DecimalFormat("#.##").format((this / 1024.0.pow(2)) / 0.1).toString() + " Mb/s"
}