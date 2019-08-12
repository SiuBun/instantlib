package com.baselib.instant.imageloader

/**
 * 图片（按比例）压缩配置信息
 *
 * @author matt
 * @date: 2015年5月6日
 */
class ImageScaleConfig(
        /**
         * 显示区域width，单位px
         */
        var mViewWidth: Int,
        /**
         * 显示区域height，单位px
         */
        var mViewHeight: Int,
        /**
         * 是否允许图片显示时被剪切（即图片超出显示区域）
         */
        var mIsCropInView: Boolean)