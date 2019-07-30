package com.baselib.instant.util

import android.app.ActivityManager
import java.io.BufferedReader
import java.io.FileReader
import java.lang.Exception
/**
 * 内存管理工具
 *
 * @author wsb
 * */
class MemoryTool {
    companion object {
        fun getAvaliableMemory(actMgr: ActivityManager): Long {
            val memoryInfo = ActivityManager.MemoryInfo()
            actMgr.getMemoryInfo(memoryInfo)
            return memoryInfo.availMem shr 10
        }


        /**
         * 获取手机总内存
         * @return 手机总内存(KB)
         * */
        fun getTotalMemory():Long{
            var totalMemroy: Long = 0
            var array:List<String>
            val fileReader= FileReader("/proc/meminfo")
            val bufferedReader = BufferedReader(fileReader, 1024 * 4)
            try {
                val cat = bufferedReader.readLine()
                cat?.let {
                    array = cat.split("\\s+")
                    // 数组第2个为内存大小
                    if (array.size>1) {
                        totalMemroy = array[1].toLong()
                    }

                }
            }catch (e:Exception){
                e.printStackTrace()
            }finally {
                array = emptyList()
                bufferedReader.close()
                fileReader.close()
            }
            return totalMemroy
        }
    }
}
