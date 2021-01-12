package com.tencent.kotlincoroutine

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

object ChannelUsage: ITestCase {

    private fun getChannel(): ReceiveChannel<Int> = GlobalScope.produce {
        for (i in 1..5) {
            send(i)
            delay(1000L)
        }
        close()
    }

    override fun test() {
        runBlocking {
            val channel = getChannel()
            GlobalScope.launch {
                try {
                    repeat(6) {
                        printFormatMsg("receiving ${channel.receive()}")
                    }
                } catch (e: ClosedReceiveChannelException) {
                    printFormatMsg("exception ${e.localizedMessage}")
                }
            }
        }
    }

}