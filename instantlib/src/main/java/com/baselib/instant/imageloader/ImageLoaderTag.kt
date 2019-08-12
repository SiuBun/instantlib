package com.baselib.instant.imageloader

/**
 * 图片加载器标签常量
 * @author matt
 */
object ImageLoaderTag {
    //图片加载器可识别imgUrl的前缀
    private val PRE_TAG = "imageloader:"
    /** SD卡图片imgUrl前缀  */
    internal val SD_IMAGE_PRENAME = PRE_TAG + "sd/"
    /** 应用内/res/drawable-xxx里图片imgUrl前缀  */
    internal val RES_IMAGE_PRENAME = "$PRE_TAG/res/"
    /** 应用内/assets里图片imgUrl前缀  */
    internal val ASSETS_IMAGE_PRENAME = "$PRE_TAG/assets/"
    /** 应用内/res/raw里图片imgUrl前缀  */
    internal val RAW_IAMGE_PRENAME = "$PRE_TAG/raw/"
    /** 程序icon图标 前缀  */
    internal val APP_ICON_PRENAME = PRE_TAG + "pkgname/"
    /** 小插件预览图 前缀  */
    internal val WIDGET_PREVIEW_PRENAME = PRE_TAG + "widget/"
}