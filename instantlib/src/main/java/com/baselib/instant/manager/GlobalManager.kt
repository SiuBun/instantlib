package com.baselib.instant.manager

import com.baselib.instant.floatwindow.FloatButtonController
import com.baselib.instant.net.NetworkManager
import com.baselib.instant.observer.ObserverManager
import com.baselib.instant.permission.PermissionsManager
import com.baselib.instant.thread.ThreadExecutorProxy
import com.baselib.instant.util.LogUtils

/**
 * 全局功能管理对象
 *
 * 权限,网络等管理对象在该对象创建时候进行实例化并存入列表,通过该对象获取
 *
 * 1.外部类加载时并不需要立即加载内部类，就不会去初始化 instance。
 *
 * 2.确保了线程安全，也能保证单例的唯一性，同时也延迟了单例的实例化
 *
 * 3.致命缺点，无法传递参数
 *
 * @author wsb
 */
class GlobalManager : IManager {
    companion object {
        const val PERMISSION_SERVICE = "permission_service"
        const val FLOAT_WINDOWS_SERVICE = "float_windows_service"
        const val NETWORK_SERVICE = "network_service"
        const val EXECUTOR_POOL_SERVICE = "executor_pool_service"
        const val OBSERVER_SERVICE = "observer_service"

        /**
         * 依赖静态内部类[GlobalManagerHolder]实现[GlobalManager]的单例
         * */
        private var instance = GlobalManagerHolder.holder

        fun initNetManager(networkManager: NetworkManager) {
            instance.managerMap[NETWORK_SERVICE] = networkManager
        }

        fun getNetworkManager(): NetworkManager {
            return getManager(NETWORK_SERVICE) as NetworkManager
        }

        /**
         * 根据名称获取对应管理对象
         *
         * @param name 目标名称
         * @return 各功能管理对象
         */
        fun getManager(name: String): IManager? {
            var baseManager: IManager? = instance.managerMap[name]
            if (baseManager == null) {
                baseManager = instance.createManagerByName(name)
                instance.managerMap[name] = baseManager
            }
            return baseManager
        }

        fun onDestroy(){
            instance.detach()
        }
    }

    private var managerMap: HashMap<String, IManager> = HashMap()

    /**
     * 创建不同的管理对象
     *
     * @param name 目标名称
     * @return 各功能管理对象
     */
    private fun createManagerByName(name: String): IManager {
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
        /*if (!managerMap.isNullOrEmpty()) {
            for (name in managerMap.keys) {
                val manager = managerMap[name]
                manager?.detach()
            }
            managerMap.clear()
        }*/

        managerMap.takeUnless { map->map.isNullOrEmpty() }.also {
            for (name in managerMap.keys) {
                val manager = managerMap[name]
                manager?.detach()
            }
            managerMap.clear()
        }
    }


    /**
     * object关键字实现[GlobalManagerHolder]的饿汉式单例
     *
     * 1.以kotlin的object形式,依赖类的初始化锁机制,不存在线程安全问题
     *
     * 2.但是会使类加载变慢
     *
     * */
    object GlobalManagerHolder {
        val holder= GlobalManager()
    }
}

