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

        fun getProvider(context: Context) {
            instance ?: synchronized(this) {
                instance ?: RepositoryManager(context.applicationContext).also { instance = it }
            }
        }
    }

    /**
     * 建造工厂示例对象,实例化各个缓存对象
     * */
    private val mCacheFactory: Cache.Factory<String, Any?> = object : Cache.Factory<String, Any?> {
        override fun build(type: CacheType): Cache<String, Any?> = LruCache(DEFAULT_CACHE_SIZE)

    }

    private var application: Application = context.applicationContext as Application

    /**
     * 存放Retrofit所用service的对象
     * */
    private var mRetrofitServiceCache: Cache<String, Any?>? = null

    /**
     * 存放room的对象
     * */
    private var mRoomDatabaseCache: Cache<String, Any?>? = null

    /**
     * 存放Retrofit的对象
     * */
    private var mRetrofitCache: Cache<String, Any?>? = null


    override fun obtainRetrofit(hostUrl: String): Retrofit {
        val cache = mRetrofitCache
                ?: mCacheFactory.build(CacheType.RETROFIT_SERVICE_CACHE_TYPE).apply {
                    mRetrofitCache = this
                }

        return (cache[hostUrl]
                ?: ConfigProvider.getRetrofitBuilder(hostUrl).build().also { cache.put(hostUrl,it) }) as Retrofit
    }


    override fun <T> Retrofit.obtainCacheService(service: Class<T>): T {
        val cache = mRetrofitServiceCache
                ?: mCacheFactory.build(CacheType.CACHE_SERVICE_CACHE_TYPE).apply {
                    mRetrofitServiceCache = this
                }

        val name = service::class.java.simpleName
        return (cache[name]
                ?: this.create(service).also { cache.put(name,it) }) as T
    }


    override fun <T : RoomDatabase> obtainDatabase(databaseClz: Class<T>, dbName: String): T {
        val cache = mRoomDatabaseCache
                ?: mCacheFactory.build(CacheType.ROOM_DATABASE_CACHE_TYPE).apply {
                    mRoomDatabaseCache = this
                }

        val name = databaseClz::class.java.simpleName
        return (cache[name]
                ?: Room.databaseBuilder(application, databaseClz, dbName).build().also { cache.put(name,it) }) as T
    }

    override fun onManagerDetach() {
        mRetrofitServiceCache?.clear()
        mRoomDatabaseCache?.clear()
        mRetrofitCache?.clear()
    }

}