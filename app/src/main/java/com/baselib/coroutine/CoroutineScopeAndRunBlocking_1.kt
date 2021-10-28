package com.baselib.coroutine

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Time:2021/10/25 9:45 下午
 * Author:
 * Description:
 */
class CoroutineSpoceAndRunBlocking_1 {
}

fun main() = runBlocking { // this: CoroutineScope
    println("runBlocking is start")

    launch {
        println("launch from runBlocking")
    }

    coroutineScope { // Creates a coroutine scope
        println("coroutineScope is start")

        launch {
            println("launch from coroutineScope")
        }

        println("coroutineScope is over")
    }

    println("runBlocking is over")
}