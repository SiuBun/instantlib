package com.baselib.instant.observer.state

import com.baselib.instant.util.LogUtils

/**
 * 未注册状态
 *
 *
 * 未注册状态下执行注册方法应该正常执行,对解注方法不做操作
 *
 * @author wsb
 */
class UnregisterState : BaseRegisterState() {
    override fun register(registerStateOperate: IRegisterStateOperate): Boolean {
        LogUtils.i(this.toString() + "现进行广播注册")
        return registerStateOperate.realRegister()
    }

}
