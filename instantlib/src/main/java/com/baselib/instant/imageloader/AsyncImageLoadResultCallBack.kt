package com.baselib.instant.imageloader

import android.graphics.Bitmap

/**
 * 图片加载结果回调接口
 *
 * @author matt
 */
interface AsyncImageLoadResultCallBack {
    /**
     * 图片加载成功
     *
     * @param imgUrl      图片url, 也是图片的key
     * @param bmp
     * @param imgSavePath 图片加载成功后保存到SD卡的全路径
     */
    fun imageLoadSuccess(imgUrl: String, bmp: Bitmap, imgSavePath: String)

    /**
     * 图片加载失败
     *
     * @param imgUrl
     * @param exception 异常代码---暂无使用，预留接口
     */
    fun imageLoadFail(imgUrl: String, exception: Int)
}