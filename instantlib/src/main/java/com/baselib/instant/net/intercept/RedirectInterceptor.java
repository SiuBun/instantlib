package com.baselib.instant.net.intercept;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 重定向拦截器
 * <p>
 * okhttp重定向存在两个缺陷：
 * <p>
 * 1.okhttp处理301,302重定向时，会把请求方式设置为GET
 * 这样会丢失原来Post请求中的参数。
 * <p>
 * 2.okhttp默认不支持跨协议的重定向，比如http重定向到https
 * <p>
 * 为了解决这两个问题写了这个拦截器
 *
 * @author wsb
 */

public class RedirectInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        HttpUrl beforeUrl = request.url();
        Response response = chain.proceed(request);
        HttpUrl afterUrl = response.request().url();
//        根据url判断是否是重定向
        if (!beforeUrl.equals(afterUrl)) {
            //处理两种情况 1、跨协议 2、原先不是GET请求。
            if (!beforeUrl.scheme().equals(afterUrl.scheme()) || !request.method().equals("GET")) {
                //重新请求
                Request newRequest = request.newBuilder().url(response.request().url()).build();
                response = chain.proceed(newRequest);
            }
        }
        return response;
    }
}