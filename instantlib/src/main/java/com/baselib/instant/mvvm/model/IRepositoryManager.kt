package com.baselib.instant.mvvm.model
/**
 * 数据仓库管理对象操作行为规范
 *
 * @author wsb
 * */
interface IRepositoryManager {
    /**
     * 根据传入的 Class 获取对应的 Retrofit service
     *
     * @param serviceClz Retrofit Service Class
     * @return Retrofit Service
     */
    fun <T> obtainRetrofitService(serviceClz: Class<T>): T

    /**
     * 根据传入的 Class 获取对应的 RxCache service
     *
     * @param databaseClz RoomDatabase Class
     * @param dbName   RoomDatabase name
     *
     * @return RoomDatabase
     */
    fun <T> obtainDatabase(databaseClz: Class<T>,dbName:String): T

}