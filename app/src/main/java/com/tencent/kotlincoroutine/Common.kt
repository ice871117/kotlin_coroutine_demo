package com.tencent.kotlincoroutine

import java.text.SimpleDateFormat
import java.util.*

val format = SimpleDateFormat("HH:mm:ss.SSS")

var COLLECTOR: ((text: CharSequence) -> Unit)? = null

inline fun printFormatMsg(msg: String) {
    val log = "${format.format(Date())} - $msg ; thread - ${Thread.currentThread().name}"
    println(log)
    COLLECTOR?.invoke(log)
}