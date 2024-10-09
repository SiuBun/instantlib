package com.baselib.instant.net.provide

import com.baselib.instant.rx.NullOnEmptyConverterFactory
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * 网络模块常规参数获取类
 *
 * @author wsb
 * */
object ConfigProvider {

    fun getRetrofitBuilder(hostUrl: String): Retrofit.Builder = Retrofit.Builder().apply {
        client(obtainOkHttpClient().build())
        baseUrl(hostUrl)
        addConverterFactory(NullOnEmptyConverterFactory())
        addConverterFactory(GsonConverterFactory.create(obtainGson()))
//        addCallAdapterFactory(RxJava2CallAdapterFactory.create())
    }

    fun obtainOkHttpClient(): OkHttpClient.Builder = OkHttpClient.Builder().apply {
        connectTimeout(5, TimeUnit.SECONDS)
        readTimeout(5, TimeUnit.SECONDS)
        callTimeout(5, TimeUnit.SECONDS)
    }

    private fun obtainGson(): Gson = GsonBuilder().apply {
        setLenient()
//        setFieldNamingStrategy(AnnotateNaming())
        serializeNulls()
    }.create()


}