package com.tencent.kotlincoroutine

import android.annotation.SuppressLint
import android.os.Build
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.MainCoroutineDispatcher
import kotlinx.coroutines.android.asCoroutineDispatcher
import java.lang.reflect.Constructor
import java.util.concurrent.*
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext

/**
 * A general portal for global coroutine dispatchers and ThreadPools.
 * All dispatchers are generated from standard java [ExecutorService]. If certain
 * project wish to customize its own ThreadPool, feel free to use setters provided
 * by [ThreadPoolProvider].
 *
 * Created by willzhang on 2020/5/25
 *
 **/
object ThreadPoolProvider {

    val CPU_COUNT = Runtime.getRuntime().availableProcessors()

    // We want at least 4 threads and at most 9 threads in the core pool,
    // preferring to have 1 less than the CPU count to avoid saturating
    // the CPU with background work
    val CORE_POOL_SIZE = Math.max(4, Math.min(CPU_COUNT + 1, 9))
    val MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1
    const val KEEP_ALIVE_SECONDS = 10L

    /**
     * Even for immediate thread pool, we just want finite count of threads.
     */
    const val MAX_THREAD_COUNT = 150

    val IMMEDIATE_EXECUTOR: ExecutorService = AThreadExecutor(
            0, MAX_THREAD_COUNT,
            0L, TimeUnit.SECONDS,
            SynchronousQueue<Runnable>(),
            AThreadFactory("thread-immediate", AThreadPriority.HIGH)
    )

    val DEFAULT_EXECUTOR: ExecutorService = AThreadExecutor(
            CORE_POOL_SIZE, MAXIMUM_POOL_SIZE,
            KEEP_ALIVE_SECONDS, TimeUnit.SECONDS,
            LinkedBlockingQueue<Runnable>(),
            AThreadFactory("thread-default")
    ).apply { allowCoreThreadTimeOut(true) }

    val API_EXECUTOR: ExecutorService = AThreadExecutor(
            3, 3,
            KEEP_ALIVE_SECONDS, TimeUnit.SECONDS,
            LinkedBlockingQueue<Runnable>(),
            AThreadFactory("thread-api")
    )

    val IO_EXECUTOR: ExecutorService = AThreadExecutor(
            CORE_POOL_SIZE, MAXIMUM_POOL_SIZE,
            KEEP_ALIVE_SECONDS, TimeUnit.SECONDS,
            LinkedBlockingQueue<Runnable>(),
            AThreadFactory("thread-io")
    ).apply { allowCoreThreadTimeOut(true) }

    @SuppressLint("CI_NotAllowInvokeExecutorsMethods")
    val BACKGROUND_LOW_EXECUTOR: ExecutorService =
            Executors.newSingleThreadExecutor(AThreadFactory("thread-low", AThreadPriority.LOW))

    @SuppressLint("CI_NotAllowInvokeExecutorsMethods")
    val BACKGROUND_SINGLE_EXECUTOR: ExecutorService =
            Executors.newSingleThreadExecutor(AThreadFactory("thread-bg-single"))
}

val DISPATCHER_FAST_MAIN = Looper.getMainLooper().asHandler(true).asCoroutineDispatcher("fast-main")
val DISPATCHER_DEFAULT = AThreadPool(ThreadPoolProvider.DEFAULT_EXECUTOR)
val DISPATCHER_API = AThreadPool(ThreadPoolProvider.API_EXECUTOR)
val DISPATCHER_IMMEDIATE = AThreadPool(ThreadPoolProvider.IMMEDIATE_EXECUTOR)
val DISPATCHER_IO = AThreadPool(ThreadPoolProvider.IO_EXECUTOR)
val DISPATCHER_LOW = AThreadPool(ThreadPoolProvider.BACKGROUND_LOW_EXECUTOR)
val DISPATCHER_SINGLE_POOL = AThreadPool(ThreadPoolProvider.BACKGROUND_SINGLE_EXECUTOR)


class AThreadPool internal constructor(val executor: ExecutorService) : CoroutineDispatcher() {
    override fun dispatch(context: CoroutineContext, block: Runnable) =
            try {
                executor.execute(block)
            } catch (e: RejectedExecutionException) {
                e.printStackTrace()
            }

    override operator fun plus(context: CoroutineContext): CoroutineContext {
        val dispatcher = context[ContinuationInterceptor]
        if (dispatcher is MainCoroutineDispatcher) {
            throw UnsupportedOperationException("you can not plus a context running in main thread. because it's meaningless")
        }

        return super.plus(context)
    }
}

internal fun Looper.asHandler(async: Boolean): Handler {
    // Async support was added in API 16.
    if (!async || Build.VERSION.SDK_INT < 16) {
        return Handler(this)
    }

    if (Build.VERSION.SDK_INT >= 28) {
        return Handler.createAsync(this)
    }

    val constructor: Constructor<Handler>
    try {
        constructor = Handler::class.java.getDeclaredConstructor(
                Looper::class.java,
                Handler.Callback::class.java, Boolean::class.javaPrimitiveType)
    } catch (ignored: NoSuchMethodException) {
        // Hidden constructor absent. Fall back to non-async constructor.
        return Handler(this)
    }
    return constructor.newInstance(this, null, true)
}


