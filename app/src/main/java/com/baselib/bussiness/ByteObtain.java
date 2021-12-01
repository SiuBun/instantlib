package com.baselib.bussiness;

import androidx.annotation.Nullable;

import com.baselib.instant.executor.ThreadExecutorProxy;
import com.baselib.instant.manager.GlobalManager;

import java.io.InputStream;
import java.util.concurrent.CountDownLatch;

/**
 * 字节获取管理对象
 *
 * @author wangshaobin
 */
public class ByteObtain {

    private final CountDownLatch mCountDownLatch = new CountDownLatch(1);

    public static ByteObtain getInstance() {
        return ByteObtainProvider.get();
    }

    /**
     * 采用同步形式从指定url获取byte
     * <p>
     * 该方法将导致调用线程阻塞
     *
     * @param url 目标
     * @return 对url的解析byte
     */
    @Nullable
    public byte[] getByteFromUrl(String url) {
        UrlByteReq task = new UrlByteReq(url, mCountDownLatch);
        ThreadExecutorProxy threadManager = (ThreadExecutorProxy) GlobalManager.Companion.getManager(GlobalManager.EXECUTOR_POOL_SERVICE);
        threadManager.execute(task);

        try {
            mCountDownLatch.await();
        } catch (Exception e) {
            e.printStackTrace();
        }

        InputStream inputStream = task.getInputStream();
        return inputStream != null ? inputStream.toString().getBytes() : null;
    }

    private ByteObtain() {
    }

    private static class ByteObtainProvider {
        private static final ByteObtain BYTE_OBTAIN = new ByteObtain();

        public static ByteObtain get() {
            return BYTE_OBTAIN;
        }
    }

}
