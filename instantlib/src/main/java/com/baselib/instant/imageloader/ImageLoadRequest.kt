package com.baselib.instant.imageloader

import android.text.TextUtils

/**
 * 图片加载请求信息
 *
 * @author matt
 * @date: 2015年5月6日
 */
class ImageLoadRequest(groupLabel: String?, imageUrl: String,
                       /**
                        * 图片保存到SD卡的目录, 路径尾部须带路径分隔符“/”
                        */
                       private val imagePath: String) {
    /**
     * 图片URL, 加载SD卡图片可传null，其它情况必须有值
     */
    var imageUrl: String
        internal set
    /**
     * 图片保存到SD卡的名称
     */
    private var imageName: String? = null
    /**
     * 加载图片成功后，是否缓存到内存
     */
    private var isCache = true
    /**
     * 网络图片处理器。当图片从网络加载完时，先经过operator处理再保存到SD卡，如果不需要处理，则传null
     */
    private var netBitmapOperator: AsyncNetBitmapOperator? = null
    /**
     * 图片加载结果回调, 回调在UI线程上执行
     */
    internal var callBack: AsyncImageLoadResultCallBack? = null
    /**
     * 图片分组标签，用于优先加载当前急需显示图片
     */
    private val groupLabel: String = groupLabel ?: ""
    /**
     * 图片压缩配置
     */
    private var scaleCfg: ImageScaleConfig? = null
    /**
     * 请求发起时间
     */
    var requestTime: Long = 0

    /**
     * 获取图片存储全路径
     *
     * @return
     */
    val imageSavePath: String
        get() = imagePath + imageName!!

    init {
        this.imageUrl = imageUrl
        imageName = imageUrl.hashCode().toString() + ""
        if (!TextUtils.isEmpty(groupLabel)) {
            imageName = "$groupLabel-$imageName"
        }
        requestTime = System.currentTimeMillis()
    }

    override fun toString(): String {
        return "[ImageLoadRequest] mGroupLabel:$groupLabel, mImageUrl:$imageUrl, mRequestTime:$requestTime"
    }
}