package com.baselib.coroutine

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope

/**
 * Time:2021/10/25 8:14 下午
 * Author:
 * Description:
 */
class SupervisorScopeUse {

}

fun main() = runBlocking {
    launch {
        delay(100)
        println("Task from runBlocking")
    }
    supervisorScope {
        println("supervisorScope start")
        launch {
            delay(500)
            println("Task throw Exception")
            throw Exception("failed")
        }
        launch {
            delay(600)
            println("Task from nested launch")
        }
        println("supervisorScope end")
    }
    println("Coroutine scope is over")
}


private fun threadNameAndTime() = "${Thread.currentThread().name} ${System.currentTimeMillis()}"
