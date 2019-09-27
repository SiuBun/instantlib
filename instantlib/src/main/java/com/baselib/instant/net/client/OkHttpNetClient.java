package com.baselib.instant.net.client;

import android.text.TextUtils;

import com.baselib.instant.net.base.CallbackHandler;
import com.baselib.instant.net.base.IHttpStateCallback;
import com.baselib.instant.net.base.INetClient;
import com.baselib.instant.net.base.NetConfig;
import com.baselib.instant.net.intercept.RedirectInterceptor;
import com.baselib.instant.net.provide.ConfigProvider;
import com.baselib.instant.util.LogUtils;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * okhttp请求方案
 *
 * @author wsb
 */
public class OkHttpNetClient implements INetClient {
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    /**
     * 默认策略 只取网络的缓存
     * <p>
     * CacheControl是针对Request的
     */
    public static final CacheControl FORCE_NETWORK = new CacheControl.Builder()
            .noCache()
            .build();

    /**
     * 默认策略 只取本地的缓存
     * <p>
     * CacheControl是针对Request的
     */
    public static final CacheControl FORCE_CACHE = new CacheControl.Builder()
//    只允许使用当前缓存数据，没有缓存就只能返回 504 响应
//            .onlyIfCached()
//            缓存保质期
            .maxAge(NetConfig.DEFAULT_CACHE_VALID_TIME, TimeUnit.SECONDS)
//            缓存超过保质期多久仍可接受
//            .maxStale(NetConfig.DEFAULT_CACHE_STALE_TIME, TimeUnit.SECONDS)
            .build();

    /**
     * 服务器作为网关不能从上游服务器得到响应返回给客户端
     */
    private static final int GATEWAY_TIMEOUT = 504;

    /**
     * 所请求的内容距离上次访问并没有变化，浏览器缓存有效
     */
    private static final int NOT_MODIFIED = 304;

    /**
     * 合适的资源作为响应体传回客户端
     */
    private static final int OK = 200;


    private OkHttpClient mClient;
    private CallbackHandler mCallbackHandler;


    public OkHttpNetClient() {
        mCallbackHandler = new CallbackHandler();
    }

    public static OkHttpNetClient build() {
        return new OkHttpNetClient();
    }

    @Override
    public void setConfig(NetConfig config) {
        int timeout = config.getTimeout();
        OkHttpClient.Builder clientBuilder = ConfigProvider.INSTANCE.obtainOkHttpClient()
                .connectTimeout(timeout, TimeUnit.SECONDS)
                .readTimeout(timeout, TimeUnit.SECONDS)
                .callTimeout(timeout, TimeUnit.SECONDS);

        setCustomInterceptor(config, clientBuilder);
        this.mClient = clientBuilder.build();

    }

    /**
     * 设置自定义拦截器
     * <p>
     * 对okhttpclient进行拦截设置
     *
     * @param config        客户端对网络请求client的要求
     * @param clientBuilder okhttpclient构造器
     */
    private void setCustomInterceptor(NetConfig config, OkHttpClient.Builder clientBuilder) {
        setLogInterceptor(clientBuilder);
        setClientCache(config, clientBuilder);
        setCacheInterceptor(config, clientBuilder);
        clientBuilder.addInterceptor(new RedirectInterceptor());
    }

    @Override
    public Response executeGet(String url, HashMap<String, Object> map, IHttpStateCallback stateCallback) {
        Response response = null;
        final Request request = new Request.Builder()
//                使用缓存
//                .cacheControl(FORCE_CACHE)
                .url(url)
                .build();

        mCallbackHandler.reqBefore(stateCallback, request.url().toString());

        mCallbackHandler.reqStart(stateCallback);
        try {
            response = mClient.newCall(request).execute();
            LogUtils.d("network response = " + response.networkResponse());
            LogUtils.d("cache response = " + response.cacheResponse());
            mCallbackHandler.reqSuccess(stateCallback, response);
        } catch (IOException e) {
            e.printStackTrace();
            mCallbackHandler.reqError(stateCallback, e);
        }
        mCallbackHandler.reqFinish(stateCallback);

        return response;
    }

    @Override
    public void reqGet(String url, Map<String, Object> params, final IHttpStateCallback stateCallback) {
        final Request request = new Request.Builder()
                .url(url)
                .build();
        enqueue(request, stateCallback);
    }

    @Override
    public void reqPost(String url, Map<String, Object> params, IHttpStateCallback stateCallback) {
        FormBody.Builder bodyBuilder = new FormBody.Builder();
        if (params != null && params.size() > 0) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                String key = entry.getKey();
                String entryValue = String.valueOf(entry.getValue());
                bodyBuilder.add(key, entryValue);
            }
        }
        Request request = new Request.Builder()
                .url(url)
                .post(bodyBuilder.build())
                .build();

        enqueue(request, stateCallback);
    }

    /**
     * 服务器支持缓存方案
     * <p>
     * 如果服务器支持缓存,请求返回的Response会带有这样的Header:Cache-Control,max-age=xxx,这种情况下我们只需要手动给okhttp设置缓存就可以让okhttp自动帮你缓存
     *
     * @param config        see {@link #setCustomInterceptor(NetConfig, OkHttpClient.Builder)}
     * @param clientBuilder see {@link #setCustomInterceptor(NetConfig, OkHttpClient.Builder)}
     */
    private void setClientCache(NetConfig config, OkHttpClient.Builder clientBuilder) {
        if (config.getCacheFile() != null && config.getCacheFile().exists()) {
            Cache cache = new Cache(config.getCacheFile(), NetConfig.DEFAULT_CACHE_SIZE);
            clientBuilder.cache(cache);
        }
    }

    /**
     * 整体缓存策略
     * <p>
     * 如果服务器不支持缓存就可能没有指定max-age这个头部，就需要使用Interceptor来重写Respose的头部信息，从而让okhttp支持缓存
     * <p>
     * 通过设置拦截器，对所有的请求都做缓存策略
     *
     * @param config        see {@link #setCustomInterceptor(NetConfig, OkHttpClient.Builder)}
     * @param clientBuilder see {@link #setCustomInterceptor(NetConfig, OkHttpClient.Builder)}
     */
    private void setCacheInterceptor(NetConfig config, OkHttpClient.Builder clientBuilder) {
        Interceptor interceptor = chain -> {

            Request request = chain.request();
            request = request.newBuilder().cacheControl(getCacheControl(config)).build();

            Response response = chain.proceed(request);
            String cacheControl = request.cacheControl().toString();
            LogUtils.i("请求的缓存策略 " + cacheControl);

            if (TextUtils.isEmpty(cacheControl)) {
                cacheControl = "public, max-age=" + NetConfig.DEFAULT_CACHE_VALID_TIME;
            }

            return response
                    .newBuilder()
                    .header("Cache-Control", cacheControl)
                    // 清除头信息，因为服务器如果不支持，可能会返回一些干扰信息，不清除下面无法生效
                    .removeHeader("Pragma")
                    .build();
        };
        clientBuilder.addNetworkInterceptor(interceptor);
    }


    /**
     * 获取缓存控制器
     * <p>
     * 从客户端配置内容判断是否使用客户端还是默认的
     *
     * @param config 客户端对网络请求client的要求
     * @return 缓存控制器对象
     */
    private CacheControl getCacheControl(NetConfig config) {
        return config.getCacheAvailableTime() > 0 ? new CacheControl.Builder()
                .maxAge(config.getCacheAvailableTime(), TimeUnit.SECONDS)
                .build() : FORCE_CACHE;
    }

    /**
     * 设置log拦截器
     * <p>
     * 对构造器增加拦截器,将请求前后信息获取并最后返回响应结果
     *
     * @param clientBuilder see {@link #setCustomInterceptor(NetConfig, OkHttpClient.Builder)}
     */
    private void setLogInterceptor(OkHttpClient.Builder clientBuilder) {
        Interceptor logInterceptor = chain -> {
            Request request = chain.request();
            LogUtils.i(String.format("Sending request %s on %s%n%s", request.url(), chain.connection(), request.headers()));
            long timeStart = System.nanoTime();
            Response response = chain.proceed(request);
            long timeStop = System.nanoTime();
            LogUtils.i(String.format("Received response for %s in %.1fms%n %s connection=%s", response.request().url(), (timeStop - timeStart) / 1e6d, response.headers(), chain.connection()));
            return response;
        };

        clientBuilder.addInterceptor(logInterceptor);
    }

    /**
     * 解析响应码
     *
     * @param code 响应码
     */
    private void parseRespCode(int code) {
        if (code == GATEWAY_TIMEOUT) {
            LogUtils.d("资源没有缓存，或者是缓存不符合条件");

        } else {

            LogUtils.d("资源已经缓存了，可以直接使用");
            if (code == NOT_MODIFIED) {
                LogUtils.d("所请求的内容距离上次访问并没有变化，浏览器缓存有效");
            }
            if (code == OK) {
                LogUtils.d("合适的资源作为响应体传回客户端");

            } else {
                LogUtils.d("采取别的方式获取缓存");
            }
        }
    }

    private void enqueue(Request request, final IHttpStateCallback stateCallback) {
        mCallbackHandler.reqBefore(stateCallback, request.url().toString());

        mCallbackHandler.reqStart(stateCallback);
        Callback callback = getNetWorkCallback(stateCallback);
        mClient.newCall(request).enqueue(callback);
    }

    private Callback getNetWorkCallback(final IHttpStateCallback stateCallback) {
        return new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                mCallbackHandler.reqError(stateCallback, e);
                mCallbackHandler.reqFinish(stateCallback);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                if (response.isSuccessful()) {
                    ResponseBody body = response.body();
                    if (null == body) {
                        mCallbackHandler.reqFail(stateCallback, "body is null");
                    } else {
                        mCallbackHandler.reqSuccess(stateCallback, response);
                    }
                } else {
                    mCallbackHandler.reqFail(stateCallback, "response fail");
                }

                mCallbackHandler.reqFinish(stateCallback);
            }
        };
    }

    @Override
    public void detach() {
        mCallbackHandler.removeCallbacksAndMessages(null);
        mClient = null;
    }


}
