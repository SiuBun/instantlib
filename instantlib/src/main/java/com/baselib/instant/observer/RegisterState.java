package com.baselib.instant.observer;


import com.baselib.instant.util.LogUtils;

/**
 * 注册状态下对解注进行响应
 *
 * @author wsb
 */
class RegisterState extends BaseRegisterState {
    @Override
    boolean unregister(IRegisterStateOperate registerStateOperate) {
        LogUtils.i(this + "现进行广播解注");
        return registerStateOperate.realUnregister();
    }
}
