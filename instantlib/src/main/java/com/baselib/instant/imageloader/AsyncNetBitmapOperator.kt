package com.baselib.instant.imageloader

import android.graphics.Bitmap

/**
 * 图片处理器, 如加载图片后，需要做处理则实现此接口, 如限制图片尺寸等
 */
interface AsyncNetBitmapOperator {
    fun operateBitmap(bmp: Bitmap): Bitmap
}