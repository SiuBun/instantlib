package com.baselib.instant.observer

/**
 * 观察者行为规范接口
 *
 * 当前提供注册，解绑，回收的行为.
 * 所有的观察者都拥有这样的操作对外
 *
 * @author wsb
 */
interface IObserver<T : ISubscriber> {

    /**
     * 提供订阅者
     *
     *
     * 防止出现提供后调用者对list进行增删操作
     *
     * @return 订阅者列表副本
     */
    val listenersCopy: List<T>

    /**
     * 注册想要观察的对象
     */
    fun register()

    /**
     * 解注想要观察的对象
     */
    fun unregister()

    /**
     * 增加订阅者
     *
     * @param listener 订阅监听
     */
    fun addSubscriber(listener: T)

    /**
     * 解除订阅者
     *
     * @param listener 订阅监听
     */
    fun removeSubscriber(listener: T)

    /**
     * 清空订阅者
     */
    fun clearUpSubscriber()

    /**
     * 观察者资源销毁
     */
    fun onDestroy()
}