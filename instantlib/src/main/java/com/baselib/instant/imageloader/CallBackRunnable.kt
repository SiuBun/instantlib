package com.baselib.instant.imageloader

import android.graphics.Bitmap

/**
 * 获取图片后，同步到主线程，回调Runable
 *
 * @author wsb
 */
class CallBackRunnable internal constructor(private val mBitmap: Bitmap?, private val mRequest: ImageLoadRequest) : Runnable {

    override fun run() {
        mRequest.callBack?.also {
            if (mBitmap != null) {
                it.imageLoadSuccess(mRequest.imageUrl, mBitmap, mRequest.imageSavePath)
            } else {
                it.imageLoadFail(mRequest.imageUrl, -1)
            }
        }
    }
}