package com.baselib.instant.bpdownload

import kotlin.collections.ArrayList

/**
 * 下载管理类
 *
 * 支持多线程断点续传
 * @author wsb
 * */
object DownloadHelper {
    private var taskList: ArrayList<Runnable> = ArrayList()

    fun addTask(): DownloadHelper {

        return this
    }

}