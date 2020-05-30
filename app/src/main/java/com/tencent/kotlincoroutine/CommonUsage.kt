package com.tencent.kotlincoroutine

import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main

object CommonUsage: ITestCase {

    suspend fun suspendFun1(param : Int) : Int {
        printFormatMsg("enter suspendFun1()")
        var result = GlobalScope.async {
            suspendFun2(param)
        }
        printFormatMsg("done suspendFun1()")
        return result.await() + 33
    }

    suspend fun suspendFun2(param : Int) : Int {
        printFormatMsg("enter suspendFun2()")
        delay(1000)
        printFormatMsg("done suspendFun2()")
        return 15 + param
    }

    override fun test() {
        GlobalScope.launch(Main) {
            printFormatMsg("enter test")
            withContext(IO) {
                printFormatMsg("result in runBlocking is ${CommonUsage.suspendFun1(1)}")
            }
            printFormatMsg("done test")
        }
    }
}

