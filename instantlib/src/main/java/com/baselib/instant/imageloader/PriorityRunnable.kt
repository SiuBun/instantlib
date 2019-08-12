package com.baselib.instant.imageloader

/**
 * 实现了优先级的Runnable
 *
 * @author matt
 * @date: 2015年5月5日
 */
abstract class PriorityRunnable internal constructor(//请求信息
        protected var mRequest: ImageLoadRequest) : Runnable, Comparable<PriorityRunnable> {

    abstract val priority: Int

    override fun compareTo(another: PriorityRunnable): Int {
        val priority1 = priority
        val priority2 = another.priority
        //			return priority1 < priority2 ? 1 : (priority1 == priority2 ? 0 : -1);
        return if (priority1 < priority2) {
            //this优先级低，排在后边
            1
        } else if (priority1 > priority2) {
            //this优先级高，排在前边
            -1
        } else if (mRequest.requestTime < another.mRequest.requestTime) {
            //this请求时间早，排在前边
            -1
        } else if (mRequest.requestTime > another.mRequest.requestTime) {
            1
        } else {
            0
        }
    }

    companion object {
        public val PRIORITY_HIGH = 1
        public val PRIORITY_NORMAL = 0
    }
}