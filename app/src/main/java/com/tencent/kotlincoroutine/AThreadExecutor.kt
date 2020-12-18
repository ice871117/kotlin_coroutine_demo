package com.tencent.kotlincoroutine

import java.util.concurrent.*

/**
 * A wrapper for ThreadPoolExecutor which can provide a customized exception handling.
 * Created by willzhang on 2020/5/25
 **/
class AThreadExecutor : ThreadPoolExecutor {
    constructor(
        corePoolSize: Int,
        maximumPoolSize: Int,
        keepAliveTime: Long,
        unit: TimeUnit?,
        workQueue: BlockingQueue<Runnable?>?
    ) : super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue) {
    }

    constructor(
        corePoolSize: Int,
        maximumPoolSize: Int,
        keepAliveTime: Long,
        unit: TimeUnit?,
        workQueue: BlockingQueue<Runnable?>?,
        threadFactory: ThreadFactory?
    ) : super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory) {
    }

    constructor(
        corePoolSize: Int,
        maximumPoolSize: Int,
        keepAliveTime: Long,
        unit: TimeUnit?,
        workQueue: BlockingQueue<Runnable?>?,
        handler: RejectedExecutionHandler?
    ) : super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler) {
    }

    constructor(
        corePoolSize: Int,
        maximumPoolSize: Int,
        keepAliveTime: Long,
        unit: TimeUnit?,
        workQueue: BlockingQueue<Runnable?>?,
        threadFactory: ThreadFactory?,
        handler: RejectedExecutionHandler?
    ) : super(
        corePoolSize,
        maximumPoolSize,
        keepAliveTime,
        unit,
        workQueue,
        threadFactory,
        handler
    ) {
    }

    protected override fun afterExecute(r: Runnable, t: Throwable?) {
        super.afterExecute(r, t)
        var runnableException: Throwable? = null
        if (t == null && r is Future<*>) {
            val future: Future<*> = r as Future<*>
            if (future.isDone() && !future.isCancelled()) {
                try {
                    future.get()
                } catch (e: InterruptedException) {
                    // ignore
                } catch (e: ExecutionException) {
                    runnableException = e.cause
                } catch (e: Throwable) {
                    // ignore
                }
            }
        }
        if (runnableException != null) {
            try {
                // todo our own exception handling
            } catch (e: Throwable) {
                // ignore
            }
        }
    }
}
