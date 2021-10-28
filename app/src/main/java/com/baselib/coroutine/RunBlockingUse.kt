package com.baselib.coroutine

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Time:2021/10/25 8:14 下午
 * Author:
 * Description:
 */
class CoroutineRunBlocking {

}

fun main() {
    println("start")
    runBlocking {
        launch {
            repeat(3) {
                delay(100)
                println("launchA - $it")
            }
        }
        launch {
            repeat(3) {
                delay(100)
                println("launchB - $it")
            }
        }
        GlobalScope.launch {
            repeat(3) {
                delay(120)
//                delay(90)//延时不同会有不同打印结果
                println("GlobalScope - $it")
            }
        }
    }
    println("end")
}


private fun threadNameAndTime() = "${Thread.currentThread().name} ${System.currentTimeMillis()}"
