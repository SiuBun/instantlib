package com.baselib.instant.repository

import android.app.Application
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import com.baselib.instant.manager.IRepositoryManager
import com.baselib.instant.net.provide.ConfigProvider
import com.baselib.instant.repository.cache.Cache
import com.baselib.instant.repository.cache.Cache.Factory.Companion.DEFAULT_CACHE_SIZE
import com.baselib.instant.repository.cache.CacheType
import com.baselib.instant.repository.cache.LruCache
import retrofit2.Retrofit

/**
 * 数据仓库管理对象
 *
 * @author wsb
 * */
class RepositoryManager private constructor(context: Context) : IRepositoryManager {

    companion object {
        @Volatile
        private var instance: RepositoryManager? = null

        @JvmStatic
        fun getProvider(context: Context): RepositoryManager = instance ?: synchronized(this) {
            instance ?: RepositoryManager(context.applicationContext).also { instance = it }
        }
    }

    private val mModuleMap: MutableMap<String, String> = mutableMapOf()
    /**
     * 建造工厂示例对象,实例化各个缓存对象
     * */
    private val mCacheFactory: Cache.Factory<String, Any?> = object : Cache.Factory<String, Any?> {
        override fun build(type: CacheType): Cache<String, Any?> = LruCache(DEFAULT_CACHE_SIZE)

    }

    private var application: Application = context.applicationContext as Application

    /**
     * 存放Retrofit所用的service对象,已类名本身来存储
     * */
    private var mRetrofitServiceCache: Cache<String, Any?>? = null

    /**
     * 存放room的对象
     * */
    private var mRoomDatabaseCache: Cache<String, Any?>? = null

    /**
     * 存放Retrofit的对象,各个retrofit对象对应不同的api后台接口
     * */
    private var mRetrofitCache: Cache<String, Any?>? = null

    override fun obtainRetrofit(hostUrl: String): Retrofit {
        return (lazyRetrofit()[hostUrl]
                ?: ConfigProvider.getRetrofitBuilder(hostUrl).build().also { lazyRetrofit().put(hostUrl, it) }) as Retrofit
    }

    override fun <T> obtainCacheService(retrofit: Retrofit, service: Class<T>): T {
        val key = service::class.java.name + "_" + retrofit.baseUrl()
        return (lazyService()[key]
                ?: (retrofit.create(service).also { lazyService().put(key, it) })) as T
    }

    override fun <T : RoomDatabase> obtainDatabase(databaseClz: Class<T>, dbName: String): T {
        val name = databaseClz::class.java.simpleName
        return (lazyDatabaseCache()[name]
                ?: Room.databaseBuilder(application, databaseClz, dbName).build().also { lazyDatabaseCache().put(name, it) }) as T
    }

    fun attachModule(name: String, url: String): RepositoryManager = apply {
        mModuleMap[name] = url
    }

    fun getModuleUrl(moduleName: String): String = mModuleMap[moduleName]
            ?: throw IllegalAccessException("this module and url not attached")

    private fun lazyRetrofit(): Cache<String, Any?> = mRetrofitCache
            ?: mCacheFactory.build(CacheType.RETROFIT_SERVICE_CACHE_TYPE).apply {
                mRetrofitCache = this
            }

    private fun lazyService(): Cache<String, Any?> = mRetrofitServiceCache
            ?: mCacheFactory.build(CacheType.CACHE_SERVICE_CACHE_TYPE).apply {
                mRetrofitServiceCache = this
            }

    private fun lazyDatabaseCache(): Cache<String, Any?> = mRoomDatabaseCache
            ?: mCacheFactory.build(CacheType.ROOM_DATABASE_CACHE_TYPE).apply {
                mRoomDatabaseCache = this
            }

    override fun onManagerDetach() {
        mRetrofitServiceCache?.clear()
        mRoomDatabaseCache?.clear()
        mRetrofitCache?.clear()
    }

}