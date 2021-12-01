package com.baselib.bussiness;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.CountDownLatch;

/**
 * 从url获取byte内容
 *
 * @author wangshaobin
 * */
public class UrlByteReq extends Thread {

    private final String mUrl;
    private final CountDownLatch mCountDownLatch;
    private final InputStream[] mByteContent = new InputStream[1];

    public UrlByteReq(String url, CountDownLatch countDownLatch) {
        this.mUrl = url;
        this.mCountDownLatch = countDownLatch;
    }

    public InputStream getInputStream() {
        return mByteContent[0];
    }

    @Override
    public void run() {
        HttpURLConnection httpConnection = null;
        try {
            URL htmlUrl = new URL(mUrl);
            URLConnection connection = htmlUrl.openConnection();
            httpConnection = (HttpURLConnection) connection;
            int responseCode = httpConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                mByteContent[0] = httpConnection.getInputStream();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
            mCountDownLatch.countDown();
        }
    }
}
 
 