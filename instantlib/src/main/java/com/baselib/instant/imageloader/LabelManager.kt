package com.baselib.instant.imageloader

import java.util.ArrayList

/**
 * 分组标签管理器
 *
 * @author matt
 * @date: 2015年5月6日
 */
class LabelManager {
    private val mLabels = ArrayList<String>()
    private val mLockLabels = ByteArray(0)

    fun addLabel(label: String) {
        synchronized(mLockLabels) {
            if (!mLabels.contains(label)) {
                mLabels.add(label)
            }
        }
    }

    fun removeLabel(label: String) {
        synchronized(mLockLabels) {
            mLabels.remove(label)
        }
    }

    fun clearLabel() {
        synchronized(mLockLabels) {
            mLabels.clear()
        }
    }

    operator fun contains(label: String): Boolean {
        synchronized(mLockLabels) {
            return mLabels.contains(label)
        }
    }
}