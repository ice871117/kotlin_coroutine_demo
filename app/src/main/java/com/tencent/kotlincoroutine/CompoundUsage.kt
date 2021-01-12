package com.tencent.kotlincoroutine

import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Unconfined

object CompoundUsage: ITestCase {
    override fun test() {
        val job1 = GlobalScope.launch(Unconfined, CoroutineStart.LAZY) {
            var count = 0
            while (true) {
                count++
                delay(500)
                printFormatMsg("count::$count")
            }
        }

        val job2 = GlobalScope.async(DISPATCHER_API) {
            printFormatMsg("job2 start")
            job1.start()
            "Producer start"
        }

        GlobalScope.launch {
            printFormatMsg(job2.await())
            delay(3000)
            job1.cancel()
        }
    }
}
