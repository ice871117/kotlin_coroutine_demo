package com.tencent.kotlincoroutine

import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object SuspendCoroutineUsage : ITestCase {

    private val mainHandler = Handler(Looper.getMainLooper())

    private suspend fun doSomething(): Boolean = suspendCoroutine { continuation ->
        printFormatMsg("doSomething() started")
        mainHandler.postDelayed({
            continuation.resume(true)
        }, 5000)
        printFormatMsg("doSomething() finished")
    }

    override fun test() {
        GlobalScope.launch(DISPATCHER_SINGLE_POOL) {
            var count = 0
            while (count < 20) {
                count++
                delay(500)
                printFormatMsg("count::$count")
            }
        }

        GlobalScope.async(DISPATCHER_SINGLE_POOL) {
            printFormatMsg("-------before doSomething()")
            doSomething()
            printFormatMsg("-------after doSomething()")
        }
    }

}