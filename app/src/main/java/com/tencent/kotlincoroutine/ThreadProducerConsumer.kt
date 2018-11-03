package com.tencent.kotlincoroutine

import android.util.Log
import java.util.*
import java.util.concurrent.ArrayBlockingQueue
import kotlin.concurrent.thread

class ThreadProducer(private val queue: ArrayBlockingQueue<Int>, val tname: String) : Thread(tname) {

    var started = false
    private set

    private val random = Random()

    override fun start() {
        started = true
        super.start()
    }

    override fun run() {
        println("$tname started")
        var resource = 1
        while (started) {
            try {
                sleep(random.nextInt(50).toLong())
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

class ThreadConsumer(private val queue: ArrayBlockingQueue<Int>, val tname: String) : Thread(tname) {

    var started = false
    private set
    private val random = Random()

    override fun start() {
        started = true
        super.start()
    }

    override fun run() {
        println("$tname start")
        while (started) {
            try {
                sleep(random.nextInt(300).toLong())
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

object ThreadProConTestCase {

    const val TAG = "ThreadTestCase"
    val producer: ThreadProducer
    val consumer1: ThreadConsumer
    val consumer2: ThreadConsumer
    val queue: ArrayBlockingQueue<Int>

    init {
        queue = ArrayBlockingQueue(10)
        producer = ThreadProducer(queue, "producer")
        consumer1 = ThreadConsumer(queue, "consumer1")
        consumer2 = ThreadConsumer(queue, "consumer2")
    }

    fun go() {
        thread {
            val before = System.currentTimeMillis()
            producer.start()
            consumer1.start()
            consumer2.start()
            producer.join()
            consumer1.join()
            consumer2.join()
            Log.w(TAG, "Total time consumed = ${System.currentTimeMillis() - before}")
        }
    }


}