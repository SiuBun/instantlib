package com.baselib.instant.imageloader

import android.graphics.Bitmap

/**
 * 图片缓存器接口
 * @author matt
 */
interface IImageCache {
    /**
     * 获取命中率
     * @return
     */
    val hitRate: Float

    /**
     * 添加图片进缓存
     * @param key
     * @param value
     */
    fun put(key: String, value: Bitmap)

    /**
     * 从缓存获取图片(不删除)
     * @param key
     * @return
     * 备注: 在检索过程中, 如发现对应的bitmap已经recycled, 则返回null
     */
    operator fun get(key: String): Bitmap

    /**
     * 从缓存删除图片
     * @param key
     * @return 被删除的图片
     */
    fun remove(key: String): Bitmap

    /**
     * 从缓存删除图片,并回收位图数据内存
     * @param key
     */
    fun recycle(key: String)

    /**
     * 清空缓存
     */
    fun clear()

    fun clear(groupLabel: String)
    /**
     * 清空缓存,并回收所有位图数据内存
     */
    fun recycleAllImages()

    /**
     * 获取缓存实际大小
     * @return
     */
    fun size(): Int
}
