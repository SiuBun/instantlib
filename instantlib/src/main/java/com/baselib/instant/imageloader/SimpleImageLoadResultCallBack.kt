package com.baselib.instant.imageloader

/**
 * 图片加载结果回调，用于简化代码，无需实现 [AsyncImageLoadResultCallBack.imageLoadFail]
 *
 * @author matt
 * @date: 2015年2月27日
 */
abstract class SimpleImageLoadResultCallBack : AsyncImageLoadResultCallBack {
    override fun imageLoadFail(imgUrl: String, exception: Int) {}
}