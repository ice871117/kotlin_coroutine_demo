package com.tencent.kotlincoroutine

import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Unconfined

object CompoundUsage: ITestCase {
    override fun test() {
        //每秒输出两个数字
        val job1 = GlobalScope.launch(Unconfined, CoroutineStart.LAZY) {
            var count = 0
            while (true) {
                count++
                //delay()表示将这个协程挂起500ms
                delay(500)
                printFormatMsg("count::$count")
            }
        }

        //job2会立刻启动
        val job2 = GlobalScope.async {
            job1.start()
            "ZhangTao"
        }

        GlobalScope.launch {
            //await()的规则是：如果此刻job2已经执行完则立刻返回结果，否则等待job2执行
            printFormatMsg(job2.await())
            delay(3000)
            job1.cancel()
        }
    }
}
