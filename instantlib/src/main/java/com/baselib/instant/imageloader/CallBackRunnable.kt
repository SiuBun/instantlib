package com.baselib.instant.imageloader

import android.graphics.Bitmap

/**
 * 获取图片后，同步到主线程，回调Runable
 *
 * @author matt
 */
class CallBackRunnable internal constructor(private val mBitmap: Bitmap?, private val mRequest: ImageLoadRequest) : Runnable {

    override fun run() {
        if (null == mRequest.callBack) {
            return
        }
        if (mBitmap != null) {
            mRequest.callBack.imageLoadSuccess(mRequest.mImageUrl, mBitmap, mRequest.imageSavePath)
        } else {
            mRequest.callBack.imageLoadFail(mRequest.mImageUrl, -1)
        }
    }
}