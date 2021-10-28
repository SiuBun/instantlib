package com.baselib.coroutine

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Time:2021/10/25 9:34 下午
 * Author:
 * Description:
 */
class CoroutineRepeat {
}

//sampleStart
fun main() = runBlocking {
//    启动 10 万个协程，5 秒后，每个协程打印一个点
    repeat(100_000) { // launch a lot of coroutines
        launch {
            delay(5000L)
            print(".")
        }
    }
}
//sampleEnd