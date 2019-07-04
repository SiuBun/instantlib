package com.baselib.instant.manager

import com.baselib.instant.floatwindow.FloatButtonController
import com.baselib.instant.net.NetworkManager
import com.baselib.instant.observer.ObserverManager
import com.baselib.instant.permission.PermissionsManager
import com.baselib.instant.thread.ThreadExecutorProxy
import com.baselib.instant.util.LogUtils

/**
 * 全局功能管理对象
 * <p>
 * 权限,网络等管理对象在该对象创建时候进行实例化并存入列表,通过该对象获取
 *
 * 以kotlin的object形式,依赖类的初始化锁机制实现线程安全的单例
 *
 * @author wsb
 */
object GlobalManager : IManager {
    private var mManagerMap: HashMap<String, IManager> = HashMap()

    const val PERMISSION_SERVICE = "permission_service"
    const val FLOAT_WINDOWS_SERVICE = "float_windows_service"
    const val NETWORK_SERVICE = "network_service"
    const val EXECUTOR_POOL_SERVICE = "executor_pool_service"
    const val OBSERVER_SERVICE = "observer_service"
    /**
     * 创建不同的管理对象
     *
     * @param name 目标名称
     * @return 各功能管理对象
     */
    fun createManagerByName(name: String): IManager {
        val baseManager: IManager?
        when (name) {
            PERMISSION_SERVICE -> baseManager = PermissionsManager()
            FLOAT_WINDOWS_SERVICE -> baseManager = FloatButtonController()
            EXECUTOR_POOL_SERVICE -> baseManager = ThreadExecutorProxy()
            OBSERVER_SERVICE -> baseManager = ObserverManager()
            else -> baseManager = object : IManager {
                override fun detach() {
                }
            }
        }
        return baseManager
    }

    override fun detach() {
        LogUtils.d("GlobalManager#detach阶段回收各管理对象")
        if (!mManagerMap.isNullOrEmpty()) {
            for (name in mManagerMap.keys) {
                val manager = mManagerMap[name]
                manager?.detach()
            }
            mManagerMap.clear()
        }
    }

    /**
     * 根据名称获取对应管理对象
     *
     * @param name 目标名称
     * @return 各功能管理对象
     */
    fun getManager(name: String): IManager? {
        var baseManager: IManager? = mManagerMap[name]
        if (baseManager == null) {
            baseManager = createManagerByName(name)
            mManagerMap[name] = baseManager
        }
        return baseManager
    }

    fun initNetManager(networkManager: NetworkManager) {
        mManagerMap[NETWORK_SERVICE] = networkManager
    }

    fun getNetworkManager(): NetworkManager {
        return getManager(NETWORK_SERVICE) as NetworkManager
    }


}