package com.baselib.instant.manager;

import com.baselib.instant.floatwindow.FloatButtonController;
import com.baselib.instant.net.NetworkManager;
import com.baselib.instant.permission.PermissionsManager;
import com.baselib.manager.IManager;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局功能管理对象
 * <p>
 * 权限,网络等管理对象在该对象创建时候进行实例化并存入列表,通过该对象获取
 *
 * @author wsb
 */
public class GlobalManager implements IManager {

    private GlobalManager() {
    }

    @Override
    public void detach() {
        if (!mManagerMap.isEmpty()) {
            for (String name : mManagerMap.keySet()) {
                IManager manager = mManagerMap.get(name);
                if (manager != null) {
                    manager.detach();
                }
            }
            mManagerMap.clear();
        }
        InnerProvider.single = null;

    }

    /**
     * 静态内部类
     */
    private static class InnerProvider {
        private static GlobalManager single = new GlobalManager();
    }

    private static GlobalManager getInstance() {
        return InnerProvider.single;
    }

    /**
     * 项目相关管理对象都存储在该容器内
     */
    private Map<String, IManager> mManagerMap = new HashMap<>();

    public static final String PERMISSION_SERVICE = "permission_service";
    public static final String FLOAT_WINDOWS_SERVICE = "float_windows_service";
    public static final String NETWORK_SERVICE = "network_service";

    /**
     * 根据名称获取对应管理对象
     *
     * @param name 目标名称
     * @return 各功能管理对象
     */
    public static IManager getManager(String name) {
        IManager baseManager = getInstance().mManagerMap.get(name);
        if (baseManager == null) {
            baseManager = getInstance().createManagerByName(name);
            getInstance().mManagerMap.put(name, baseManager);
        }
        return baseManager;
    }

    public static NetworkManager getNetworkManager() {
        return (NetworkManager) getManager(NETWORK_SERVICE);
    }

    public static void initNetManager(NetworkManager networkManager) {
        getInstance().mManagerMap.put(NETWORK_SERVICE,networkManager);
    }

    /**
     * 创建不同的管理对象
     *
     * @param name 目标名称
     * @return 各功能管理对象
     */
    private IManager createManagerByName(String name) {
        IManager baseManager;
        switch (name) {
            case PERMISSION_SERVICE:
                baseManager = new PermissionsManager();
                break;
            case FLOAT_WINDOWS_SERVICE:
                baseManager = new FloatButtonController();
                break;
            default:
                baseManager = null;
                break;
        }
        return baseManager;
    }
}
