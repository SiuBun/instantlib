package com.baselib.coroutine

import kotlinx.coroutines.*

/**
 * Time:2021/10/25 9:45 下午
 * Author:
 * Description:
 */
class CoroutineScopeAndRunBlocking {
}

fun main() = runBlocking { // this: CoroutineScope
    println("runBlocking is start")

    launch {
        delay(100L)
        println("launch from runBlocking")
    }

    coroutineScope { // Creates a coroutine scope
        println("coroutineScope is start")

        launch {
            println("launch from coroutineScope start")
            delay(500L)
            println("launch from coroutineScope over")
        }

        delay(100L)
        println("coroutineScope is over")
    }

    println("runBlocking is over")
}