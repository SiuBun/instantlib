package com.baselib.instant.observer;

import java.util.List;

/**
 * 观察者行为规范接口
 * <p>
 * 当前提供注册，解绑，回收的行为.所有的观察者都拥有这样的操作对外
 *
 * @author wsb
 */
public interface IObserver<T extends ISubscriber> {
    /**
     * 注册想要观察的对象
     */
    void register();

    /**
     * 解注想要观察的对象
     */
    void unregister();

    /**
     * 增加订阅者
     *
     * @param listener 订阅监听
     */
    void addSubscriber(T listener);

    /**
     * 解除订阅者
     *
     * @param listener 订阅监听
     */
    void removeSubscriber(T listener);

    /**
     * 提供订阅者
     * <p>
     * 防止出现提供后调用者对list进行增删操作
     *
     * @return 订阅者列表副本
     */
    List<T> getListenersCopy();

    /**
     * 清空订阅者
     */
    void clearUpSubscriber();

    /**
     * 观察者资源销毁
     */
    void onDestroy();
}
