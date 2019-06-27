package com.baselib.instant.observer;

import com.baselib.instant.util.LogUtils;

/**
 * 未注册状态
 * <p>
 * 未注册状态下执行注册方法应该正常执行,对解注方法不做操作
 *
 * @author wsb
 */
class UnregisterState extends BaseRegisterState {
    @Override
    boolean register(IRegisterStateOperate registerStateOperate) {
        LogUtils.i(this + "现进行广播注册");
        return registerStateOperate.realRegister();
    }

}
