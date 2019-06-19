package com.baselib.manager;

/**
 * 管理对象基类
 *
 * @author wsb
 * */
public interface IManager {
    /**
     * 销毁阶段回收
     * */
    void detach();
}
