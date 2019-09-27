package com.baselib.instant.repository.cache

import kotlin.collections.LinkedHashMap
import kotlin.math.roundToInt

/**
 * LRU 即 Least Recently Used,最近最少使用,
 *
 * 当缓存满了,会优先淘汰那些最近最不常访问的数据
 *
 * 此种缓存策略为默认提供,可自行实现其他缓存策略,如磁盘缓存,为框架或开发者提供缓存的功能
 *
 * @author wsb
 */
class LruCache<K, V> : Cache<K, V> {
    /**
     * 初始化时候指定的大小
     * */
    private val iniMaxSize: Int

    /**
     * 最大容量
     * */
    private var maxCapacity: Int

    /**
     * 当前容量大小
     * */
    private var currentSize: Int = 0

    /**
     * 真正保存对象的链表
     * */
    private val cache: LinkedHashMap<K, V> = LinkedHashMap()

    /**
     * Constructor for LruCache.
     *
     * @param size 这个缓存的最大 size,这个 size 所使用的单位必须和 [getItemSize] 所使用的单位一致.
     */
    constructor(size: Int) {
        iniMaxSize = size
        maxCapacity = size
    }

    /**
     * 设置一个系数应用于当时构造函数中所传入的 size, 从而得到一个新的[maxCapacity]
     * 并会立即调用[evict]开始清除满足条件的条目
     *
     * @param multiplier 系数
     */
    @Synchronized
    fun changeSizeMultiplier(multiplier: Float) {
        multiplier.takeUnless { it < 0 }?.run {
            maxCapacity = (iniMaxSize * multiplier).roundToInt()
            evict()
        }
    }

    override fun getMaxSize(): Int = maxCapacity

    override fun size(): Int = currentSize

    override fun get(key: K): V? = cache[key]

    /**
     * 如果 [getItemSize]返回的 size 大于或等于缓存所能允许的最大 size, 则不能向缓存中添加此条目
     * 此时会回调 [onItemEvicted]通知此方法当前被驱逐的条目
     *
     * 将key和value以条目的形式加入缓存,如果这个key在缓存中已经有对应的value
     * 则此value 被新的value 替换并返回,如果为null说明是一个新条目
     *
     *
     * @param key   通过这个key添加条目
     * @param value 需要添加的 value
     */
    override fun put(key: K, value: V): V? {
        return if (getItemSize(value) >= maxCapacity) {
//            所加入的对象过大,必须移除
            onItemEvicted(key, value)
            null
        } else {
            val result: V? = cache.put(key, value)

//            返回了空代表属于新增的key,不为空说明原先已有该key,不需要改变当前大小
            if (null == result) {
                changeCurrentSize(result, true)
            }

            evict()
            result
        }
    }

    @Synchronized
    override fun remove(key: K): V? = cache.remove(key)?.also { changeCurrentSize(it, false) }

    override fun containsKey(key: K): Boolean = cache.containsKey(key)

    override fun clear() = trimToSize(0)

    /**
     * 当缓存中已占用的总 size 大于所能允许的最大 size[maxCapacity] ,会使用  [trimToSize] 开始清除满足条件的条目
     */
    private fun evict() {
        trimToSize(maxCapacity)
    }

    /**
     * 目前缓存已占用的总 size [currentSize]大于指定的size时,会开始清除缓存中最近最少使用的条目
     *
     * @param size 指定缓存大小
     */
    @Synchronized
    fun trimToSize(size: Int) {
        var last: Map.Entry<K, V>
        while (currentSize > size) {
            last = cache.entries.iterator().next()
            val value: V = last.value
            val key: K = last.key
            cache.remove(key)
            changeCurrentSize(value, false)
            onItemEvicted(key, value)
        }
    }

    /**
     * 对当前size大小进行操作记录
     *
     * @param value 准备添加或者移除的值
     * @param add true代表添加操作
     * */
    private fun changeCurrentSize(value: V?, add: Boolean) {
        if (add) {
            currentSize += getItemSize(value)
        } else {
            currentSize -= getItemSize(value)
        }
    }

    /**
     * 返回每个 item 所占用的 size,默认为1,这个 size 的单位必须和构造函数所传入的 size 一致
     * 子类可以重写这个方法以适应不同的单位,比如说 bytes
     *
     * @param item 每个 item 所占用的 size
     */
    open fun getItemSize(item: V?): Int = 1

    /**
     * 当缓存中有被驱逐的条目时,会回调此方法,默认空实现,子类可以重写这个方法
     *
     * @param key   被驱逐条目的 key
     * @param value 被驱逐条目的 value
     */
    open fun onItemEvicted(key: K, value: V) {
    }
}