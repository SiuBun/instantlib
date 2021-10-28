package com.baselib.coroutine

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Time:2021/10/25 9:27 下午
 * Author:
 * Description:
 */
class CoroutineLaunchJob {
}

fun main() = runBlocking {
//sampleStart
//    launch协程构建器返回一个 Job 对象，该对象是已启动协程的句柄，可用于显式等待其完成
    val job = launch { // launch a new coroutine and keep a reference to its Job
        println("delay 1000!")
        delay(1000L)
        println("World!")
    }
    println("Hello")
    job.join() // wait until child coroutine completes
    println("Done")
//sampleEnd
}

private fun threadNameAndTime() = "${Thread.currentThread().name} ${System.currentTimeMillis()}"