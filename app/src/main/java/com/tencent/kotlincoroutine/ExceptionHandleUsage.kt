package com.tencent.kotlincoroutine

import kotlinx.coroutines.*
import java.lang.RuntimeException
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

object ExceptionHandleUsage: ITestCase {

    /**
     * launch will throw an exception as soon as possible
     */
    private suspend fun exceptionWithinLaunch() = GlobalScope.launch {
        throw RuntimeException("Exception in launch")
    }

    /**
     * however async will not throw the exception until its returned deferred's await() is called.
     */
    private fun scopedExceptionHandle(): Deferred<*> {
        val scope = CoroutineScope(Job())
        return scope.async {
            printFormatMsg("launch parent coroutine")
            async {
                printFormatMsg("launch child coroutine")
                async {
                    printFormatMsg("launch grand child coroutine")
                    throw RuntimeException("An exception in grand child")
                }
            }
        }
    }

    private suspend fun execAndCatch(blocking: suspend () -> Unit) {
        try {
            blocking.invoke()
        } catch (t: Throwable) {
            printFormatMsg("Exception encountered: ${t.message}")
        }
    }

    override fun test() {
        runBlocking {
//            execAndCatch { exceptionWithinLaunch() }  // if can not catch it here, exception will be just thrown where it happen
            val deferred = scopedExceptionHandle()
            execAndCatch { deferred.await() }
        }

        // use SupervisorJob
        CoroutineScope(SupervisorJob() + MyExceptionPreHandler()).launch {
            launch {
                throw RuntimeException("An exception in child1")
                printFormatMsg("child 1 finish")
            }
            launch {
                printFormatMsg("child 2 finish")// Child 2
            }
        }
    }

    class MyExceptionPreHandler :
            AbstractCoroutineContextElement(CoroutineExceptionHandler), CoroutineExceptionHandler {
        override fun handleException(context: CoroutineContext, exception: Throwable) {
            printFormatMsg("Handling exception within $context, detail: $exception")
        }
    }

}