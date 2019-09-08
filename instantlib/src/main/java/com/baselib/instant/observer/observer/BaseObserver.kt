package com.baselib.instant.observer.observer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import com.baselib.instant.observer.IObserver
import com.baselib.instant.observer.ISubscriber
import com.baselib.instant.observer.receiver.AppChangedReceiver
import com.baselib.instant.observer.state.BaseRegisterState
import com.baselib.instant.observer.state.RegisterState
import com.baselib.instant.observer.state.UnregisterState
import com.baselib.instant.util.LogUtils
import java.util.ArrayList

abstract class BaseObserver<T : ISubscriber>(context: Context) : IObserver<T>, BaseRegisterState.IRegisterStateOperate {
    private var subscriberSet: ArrayList<T>

    private val appContext: Context = context.applicationContext

    private var unregisterState: UnregisterState? = null
    private var registerState: RegisterState? = null

    /**
     * 是否动态注册广播监听，默认为true
     *
     * @return 如该值为false(静态注册)，需要在AndroidManifest.xml里配置 [AppChangedReceiver]
     */
    internal fun registerByDynamic(): Boolean {
        return true
    }

    private val receiver: BroadcastReceiver?

    protected val lock = ByteArray(0)

    private var baseRegisterState: BaseRegisterState

    init {
        receiver = this.getReceiver()
        subscriberSet = ArrayList()
        unregisterState = UnregisterState()
        registerState = RegisterState()
        //        默认使用未注册状态
        baseRegisterState = unregisterState!!
    }

    override fun register() {
        LogUtils.i(this.toString() + "现指派" + baseRegisterState + "进行广播注册")
        if (baseRegisterState.register(this)) {
            switchRegisterState(true)
        } else {
            LogUtils.e(this.toString() + "已经注册过,不再进行重复注册")
            LogUtils.i(baseRegisterState.toString() + "未完成" + this + "指派进行的 广播注册")
        }
    }

    override fun unregister() {
        LogUtils.i(this.toString() + "现指派" + baseRegisterState + "进行广播解注")
        if (baseRegisterState.unregister(this)) {
            switchRegisterState(false)
        } else {
            LogUtils.e(this.toString() + "已经解注过,不再进行重复解注")
            LogUtils.i(baseRegisterState.toString() + "未完成" + this + "指派进行的 广播解注")
        }
    }

    /**
     * 切换注册状态
     *
     * @param registed true代表需要切换为 已注册
     */
    private fun switchRegisterState(registed: Boolean) {
        if (registed) {
            baseRegisterState = registerState!!
        } else {
            baseRegisterState = unregisterState!!
        }
    }

    override fun addSubscriber(listener: T) {
//        增加订阅者前检测是否已经开启广播接收器注册
        if (registerByDynamic()) {
            register()
        }

        synchronized(lock) {
            if (!subscriberSet.contains(listener)) {
                subscriberSet.add(listener)
                LogUtils.d("该订阅者 $listener,完成订阅")
            } else {
                LogUtils.d("已存在该订阅者 $listener,不再重复订阅")
            }
        }
    }

    override fun removeSubscriber(listener: T) {
        synchronized(lock) {
            if (subscriberSet.contains(listener)) {
                subscriberSet.remove(listener)
                LogUtils.d("该订阅者 $listener 完成退订")
            } else {
                LogUtils.d("该订阅者 $listener 不在订阅者列表中,无法进行退订")
            }
        }
    }

    override val listenersCopy: List<T>
        get(){
            synchronized(lock) {
                return subscriberSet.clone() as List<T>
            }
        }


    override fun clearUpSubscriber() {
        if (subscriberSet.isNotEmpty()) {
            subscriberSet.clear()
        }
    }

    override fun realRegister(): Boolean {
        var registerResult = false
        getIntentFilter()?.let {
            appContext.registerReceiver(receiver, getIntentFilter())
            registerResult = true
        }
        return registerResult
    }

    override fun realUnregister(): Boolean {
        var unregisterResult = false

        receiver?.let{
            appContext.unregisterReceiver(receiver)
            unregisterResult = true
        }

        return unregisterResult
    }

    override fun onObserverDetach() {
        unregister()
        clearUpSubscriber()
        registerState = null
        unregisterState = null
    }

    /**
     * 获取意图过滤
     *
     *
     * 子类扩展时候对该方法进行重写,明确监听内容
     *
     * @return 意图过滤对象
     */
    protected abstract fun getIntentFilter(): IntentFilter?


    /**
     * 获取广播接收者
     *
     *
     * 子类扩展时候对该方法进行重写,明确接收
     *
     * @return 意图过滤对象
     */
    protected abstract fun getReceiver(): BroadcastReceiver?
}