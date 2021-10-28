package com.baselib.coroutine

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Time:2021/10/25 8:14 下午
 * Author:
 * Description:
 */
class CoroutineLaunch {

}

fun main() {
    runBlocking {
        launch {
            println("prinltStr launch start ${threadNameAndTime()}")
            delay(1000L)
            println("prinltStr launch over ${threadNameAndTime()}")
        }
        println("runBlocking ${threadNameAndTime()}")
    }
    println("main ${threadNameAndTime()}")
}

private fun threadNameAndTime() = "${Thread.currentThread().name} ${System.currentTimeMillis()}"
