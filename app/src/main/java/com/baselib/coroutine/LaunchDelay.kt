package com.baselib.coroutine

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Time:2021/10/25 8:14 下午
 * Author:
 * Description:
 */
class CoroutineLaunchDelay {

}

fun main() {
    runBlocking {
        println("runBlocking start ")
        launch {
            println("launch start ")
            delay(1000L)
            delayPrintStr()
            println("launch over ")
        }

        println("delay in runBlocking ")
        delay(1000L)
        println("runBlocking over ")
    }
}

private fun threadNameAndTime() = "${Thread.currentThread().name} ${System.currentTimeMillis()}"

suspend fun delayPrintStr() {
    delay(1000L)
    println("delay printStr launch ")
}