package com.baselib.instant.imageloader

/**
 * 实现了优先级的Runnable
 *
 * @author matt
 * @date: 2015年5月5日
 */
abstract class PriorityRunnable internal constructor(
        /** 请求信息 */
        private var request: ImageLoadRequest) : Runnable, Comparable<PriorityRunnable> {

    abstract val priority: Int

    override fun compareTo(other: PriorityRunnable): Int {
        val priority1 = priority
        val priority2 = other.priority
        //			return priority1 < priority2 ? 1 : (priority1 == priority2 ? 0 : -1);

        return when {
            //this优先级低，排在后边
            priority1 < priority2 -> 1
            //this优先级高，排在前边
            priority1 > priority2 -> -1
            //this请求时间早，排在前边
            request.requestTime < other.request.requestTime -> -1
            request.requestTime > other.request.requestTime -> 1
            else -> 0
        }
    }

    companion object {
        public val PRIORITY_HIGH = 1
        public val PRIORITY_NORMAL = 0
    }
}