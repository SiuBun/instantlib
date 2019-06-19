package com.baselib.instant.net;

import com.baselib.instant.net.base.IHttpStateCallback;
import com.baselib.instant.net.base.INetClient;
import com.baselib.instant.net.base.NetConfig;
import com.baselib.instant.net.client.OkHttpNetClient;
import com.baselib.manager.IManager;

import java.util.Map;

/**
 * 网络管理对象
 *
 * @author wsb
 */
public class NetworkManager implements IManager {

    private INetClient mNetClient;


    private NetworkManager() {
    }

    private NetworkManager setConfig(NetConfig config) {
        mNetClient.setConfig(config);
        return this;
    }

    private NetworkManager setNetClient(INetClient netClient) {
        this.mNetClient = netClient;
        return this;
    }

    /**
     * 执行get请求
     * <p>
     * 交由client处理
     *
     * @param url           请求链接
     * @param params        请求参数
     * @param stateCallback 请求状态回调
     */
    public void reqGet(String url, Map<String, Object> params, IHttpStateCallback stateCallback) {
        mNetClient.reqGet(url, params, stateCallback);
    }

    /**
     * 执行post请求
     * <p>
     * 交由client处理
     *
     * @param url           请求链接
     * @param params        请求参数
     * @param stateCallback 请求状态回调
     */
    public void reqPost(String url, Map<String, Object> params, IHttpStateCallback stateCallback) {
        mNetClient.reqPost(url, params, stateCallback);
    }


    @Override
    public void detach() {
        mNetClient.detach();
    }

    /**
     * 网络管理对象构造类
     * <p>
     * 针对网络管理对象的配置进行设置
     *
     * @author wsb
     */
    public static final class Builder {
        NetworkManager mNetworkManager;

        private final NetConfig mNetConfig;
        private INetClient mClient;

        public Builder() {
            mNetConfig = new NetConfig();
        }

        public Builder setTimeOut(int timeOut) {
            if (timeOut > 0) {
                mNetConfig.setTimeout(timeOut);
            }
            return this;
        }

        public Builder setRetryCount(int retryCount) {
            if (retryCount > 0) {
                mNetConfig.setRetryCount(retryCount);
            }
            return this;
        }

        public Builder setClient(INetClient client) {
            if (client != null) {
                mClient = client;
            } else {
                mClient = getDefaultNetClient();
            }
            return this;
        }

        /**
         * 对网络client进行实例化
         * <p>
         * 此处可以实现网络方案更换
         */
        private INetClient getDefaultNetClient() {
            return OkHttpNetClient.build();
        }

        public NetworkManager build() {
            return mNetworkManager = new NetworkManager()
                    .setNetClient(mClient)
                    .setConfig(mNetConfig);
        }
    }


}
