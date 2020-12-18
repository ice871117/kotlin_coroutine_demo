package com.tencent.kotlincoroutine

import android.annotation.SuppressLint
import android.os.Process
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger


/**
 * A simple thread factory impl which provides 3 levels of thread priority control and a counter to
 * record how many threads are created.
 *
 * Created by willzhang on 2020/5/25
 **/
@SuppressLint("CI_NotAllowNewThread")
class AThreadFactory : ThreadFactory {
    private val localCount = AtomicInteger(0)
    private var mName: String
    private var mPriority: AThreadPriority = AThreadPriority.NORMAL

    constructor(name: String) {
        mName = name
    }

    constructor(name: String, priority: AThreadPriority) {
        mName = name
        mPriority = priority
    }

    override fun newThread(r: Runnable?): Thread {
        sCount.incrementAndGet() // just for debug
        val name = mName + "-" + localCount.incrementAndGet()
        return object : Thread(r, name) {
            override fun run() {
                when(mPriority) {
                    AThreadPriority.LOW -> Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND)
                    AThreadPriority.HIGH -> Process.setThreadPriority(Process.THREAD_PRIORITY_DISPLAY)
                    else -> {}
                }
                super.run()
            }
        }
    }

    override fun toString(): String {
        return super.toString() + " name = " + mName
    }

    companion object {
        private val sCount: AtomicInteger = AtomicInteger(0)
    }
}

enum class AThreadPriority {
    LOW, NORMAL, HIGH
}