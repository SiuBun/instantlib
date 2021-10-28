package com.baselib.coroutine

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Time:2021/10/25 8:14 下午
 * Author:
 * Description:
 */
class Coroutine {

}

fun main() {
    runBlocking {
        launch {
            println("prinltStr launch ")
        }
        println("runBlocking ")
    }
    println("main ")
}

private fun threadNameAndTime() = "${Thread.currentThread().name} ${System.currentTimeMillis()}"
