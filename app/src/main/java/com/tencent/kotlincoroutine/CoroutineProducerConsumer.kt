package com.tencent.kotlincoroutine

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.ArrayBlockingQueue

class CoroutineProducer(private val queue: ArrayBlockingQueue<Int>, val tname: String) {

    var started = true
        private set

    private val random = Random()

    suspend fun run() {
        println("$tname started")
        var resource = 1
        while (started) {
            try {
                delay(random.nextInt(50).toLong())
                val nextValue = resource++
                println("$tname producing ---> $nextValue")
                queue.put(nextValue)
                if (nextValue == 100) {
                    // an extra 100 to let the second consumer quit
                    queue.put(nextValue)
                    started = false
                }
            } catch(e: InterruptedException) {
                println("interrupted")
            }
        }
        println("$tname quit...")
    }

}

class CoroutineConsumer(private val queue: ArrayBlockingQueue<Int>, val tname: String) {

    var started = true
        private set
    private val random = Random()

    suspend fun run() {
        println("$tname start")
        while (started) {
            try {
                delay(random.nextInt(300).toLong())
                val nextValue = queue.take()
                println("$tname consuming ===> $nextValue")
                if (nextValue == 100) {
                    started = false
                }
            } catch(e: InterruptedException) {
                println("interrupted")
            }
        }
        println("$tname quit...")
    }

}

object CoroutineProConTestCase {

    const val TAG = "CoroutineProConTestCase"

    val queue = ArrayBlockingQueue<Int>(10)
    val producer = CoroutineProducer(queue, "producer")
    val consumer1 = CoroutineConsumer(queue, "consumer1")
    val consumer2 = CoroutineConsumer(queue, "consumer2")

    fun go() {
        GlobalScope.launch(Dispatchers.Default) {
            val before = System.currentTimeMillis()
            val job1 = GlobalScope.launch(Dispatchers.Default) { producer.run() }
            val job2 = GlobalScope.launch(Dispatchers.Default) { consumer1.run() }
            val job3 = GlobalScope.launch(Dispatchers.Default) { consumer2.run() }
            job1.join()
            job2.join()
            job3.join()
            Log.w(TAG, "Total time consumed = ${System.currentTimeMillis() - before}")
        }
    }


}