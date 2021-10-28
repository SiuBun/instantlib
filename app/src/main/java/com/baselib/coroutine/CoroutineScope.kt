package com.baselib.coroutine

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Time:2021/10/25 8:14 下午
 * Author:
 * Description:runBlocking 和 coroutineScope看起来相似，都是等待其内部子节点完成。
 * 但主要区别在于 runBlocking 方法阻塞当前线程等待，而 coroutineScope 只是挂起，释放底层线程用于其他用途
 */
class CoroutineScope {

}

fun main() {
    runBlocking {
//        doWorld()
        doWorldMultiple()
        println("runBlocking ")
    }
}

private fun threadNameAndTime() = "${Thread.currentThread().name} ${System.currentTimeMillis()}"


suspend fun doWorld() = coroutineScope {  // this: CoroutineScope
    launch {
        delay(1000L)
        println("World! ")
    }
    println("Hello ")
}

suspend fun doWorldMultipleDelay() = coroutineScope { // this: CoroutineScope
    launch {
        delay(2000L)
        println("World 3 ")
    }

    launch {
        delay(2000L)
        println("World 2 ")
    }

    launch {
        delay(1000L)
        println("World 1 ")
    }

    launch {
        println("World 0 ")
    }

    println("Hello ")
}

suspend fun doWorldMultiple() = coroutineScope { // this: CoroutineScope
    launch {
        println("World 3 ")
    }

    launch {
        println("World 2 ")
    }

    launch {
        println("World 1 ")
    }

    launch {
        println("World 0 ")
    }

    println("Hello ")
}