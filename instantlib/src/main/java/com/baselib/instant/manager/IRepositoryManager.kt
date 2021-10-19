package com.baselib.instant.manager

import androidx.room.RoomDatabase
import retrofit2.Retrofit

/**
 * 数据仓库管理对象操作行为规范
 *
 * 支持数据库对象及service对象复用,避免每次使用的时候都create新对象
 *
 * @author wsb
 * */
interface IRepositoryManager : IManager {
    /**
     * 根据传入的 hosturl 获取对应的 Retrofit 对象
     *
     * @param hostUrl Retrofit baseUrl
     * @return Retrofit
     */
    fun obtainRetrofit(hostUrl: String): Retrofit

    /**
     * 根据传入的 Class 获取对应的 RxCache service
     *
     * @param databaseClz RoomDatabase Class
     * @param dbName   RoomDatabase name
     *
     * @return RoomDatabase
     */
    fun <T : RoomDatabase> obtainDatabase(databaseClz: Class<T>, dbName: String): T

    /**
     * 根据传入的 Class 获取对应的 Retrofit service
     *
     * @param retrofit Retrofit Object
     * @param service Retrofit Service Class
     * @return Cache Service
     * */

    fun <T> obtainCacheService(retrofit: Retrofit, service: Class<T>): T

}