package com.baselib.instant.observer.state


import com.baselib.instant.util.LogUtils

/**
 * 注册状态下对解注进行响应
 *
 * @author wsb
 */
class RegisterState : BaseRegisterState() {
    override fun unregister(registerStateOperate: IRegisterStateOperate): Boolean {
        LogUtils.i(this.toString() + "现进行广播解注")
        return registerStateOperate.realUnregister()
    }
}
