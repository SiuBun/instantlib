package com.baselib.instant.thread

/**
 * 拥有名称的Runnable
 * @author wsb
 */
class NamedTask constructor(private val task: Runnable) : Runnable {

    var threadName: String = ""

    var priority: Int = Thread.NORM_PRIORITY

    override fun run() {
        task.run()
    }

}