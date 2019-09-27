package com.baselib.instant.repository.cache

/**
 * 缓存接口
 *
 * @author wsb
 */
interface Cache<K, V> {

    /**
     * 返回当前缓存所能允许的最大 size
     *
     * @return 当前缓存所能允许的最大 size
     */
    fun getMaxSize(): Int

    interface Factory<K,V> {

        /**
         * Returns a new cache
         *
         * @param type 框架中模块类型的 Id
         * @return Cache
         */
        fun build(type: CacheType): Cache<K, V>

        companion object {
            /**
             * 默认缓存大小
             */
            const val DEFAULT_CACHE_SIZE = 100
        }
    }

    /**
     * 返回当前缓存已占用的总 size
     *
     * @return 当前缓存已占用的总 size
     */
    fun size(): Int

    /**
     * 返回这个 key 在缓存中对应的 value, 如果返回 null 说明这个 key 没有对应的 value
     *
     * @param key Key
     * @return Value
     */
    operator fun get(key: K): V?

    /**
     * 将 key 和 value 以条目的形式加入缓存,如果这个 key 在缓存中已经有对应的 value
     * 则此 value 被新的 value 替换并返回,如果为 null 说明是一个新条目
     *
     * @param key   Key
     * @param value 新的 Value
     * @return 旧的 Value
     */
    fun put(key: K, value: V): V?

    /**
     * 移除缓存中这个 key 所对应的条目,并返回所移除条目的 value
     * 如果返回为 null 则有可能时因为这个 key 对应的 value 为 null 或条目不存在
     *
     * @param key Key
     * @return Value
     */
    fun remove(key: K): V?

    /**
     * 如果这个 key 在缓存中有对应的 value 并且不为 null, 则返回 true
     *
     * @param key Key
     * @return 存在缓存怎返回 true
     */
    fun containsKey(key: K): Boolean

    /**
     * 清除缓存中所有的内容
     */
    fun clear()
}

