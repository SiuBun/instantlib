package com.baselib.instant.observer.observer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;

import com.baselib.instant.observer.IObserver;
import com.baselib.instant.observer.ISubscriber;
import com.baselib.instant.observer.receiver.AppChangedReceiver;
import com.baselib.instant.observer.state.BaseRegisterState;
import com.baselib.instant.observer.state.RegisterState;
import com.baselib.instant.observer.state.UnregisterState;
import com.baselib.instant.util.LogUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 观察者基类
 * <p>
 * 拥有对观察对象的注册解注以及对订阅者列表的成员增删通知操作
 *
 * @author wsb
 */
public abstract class BaseObserver<T extends ISubscriber> implements IObserver<T>, BaseRegisterState.IRegisterStateOperate {
    private UnregisterState mUnregisterState;
    private RegisterState mRegisterState;

    /**
     * 是否动态注册广播监听，默认为true
     *
     * @return 如该值为false(静态注册)，需要在AndroidManifest.xml里配置 {@link AppChangedReceiver}
     */
    boolean registerByDynamic() {
        return true;
    }

    private BroadcastReceiver mReceiver;

    private final byte[] mLock = new byte[0];

    private BaseRegisterState mBaseRegisteState;

    private final Context mApplicationContext;

    /**
     * 观察者本身持有订阅者列表,在收到事件的时候进行对应通知
     */
    private ArrayList<T> mSubscriberSet;

    BaseObserver(Context context) {
        mApplicationContext = context.getApplicationContext();
        mReceiver = getReceiver();
        mSubscriberSet = new ArrayList<>();

        mUnregisterState = new UnregisterState();
        mRegisterState = new RegisterState();
//        默认使用未注册状态
        mBaseRegisteState = mUnregisterState;
    }

    byte[] getLock() {
        return mLock;
    }

    @Override
    public void register() {
        LogUtils.i(this + "现指派" + mBaseRegisteState + "进行广播注册");
        if (mBaseRegisteState.register(this)) {
            switchRegisterState(true);
        } else {
            LogUtils.e(this + "已经注册过,不再进行重复注册");
            LogUtils.i(mBaseRegisteState + "未完成" + this + "指派进行的 广播注册");
        }
    }

    @Override
    public void unregister() {
        LogUtils.i(this + "现指派" + mBaseRegisteState + "进行广播解注");
        if (mBaseRegisteState.unregister(this)) {
            switchRegisterState(false);
        } else {
            LogUtils.e(this + "已经解注过,不再进行重复解注");
            LogUtils.i(mBaseRegisteState + "未完成" + this + "指派进行的 广播解注");
        }
    }

    /**
     * 切换注册状态
     *
     * @param registed true代表需要切换为 已注册
     */
    private void switchRegisterState(boolean registed) {
        if (registed) {
            mBaseRegisteState = mRegisterState;
        } else {
            mBaseRegisteState = mUnregisterState;
        }
    }

    @Override
    public void addSubscriber(T subscriber) {
        if (null == subscriber) {
            return;
        }
//        增加订阅者前检测是否已经开启广播接收器注册
        if (registerByDynamic()) {
            register();
        }

        synchronized (getLock()) {
            if (!mSubscriberSet.contains(subscriber)) {
                mSubscriberSet.add(subscriber);
                LogUtils.d("该订阅者 " + subscriber + ",完成订阅");
            } else {
                LogUtils.d("已存在该订阅者 " + subscriber + ",不再重复订阅");
            }
        }

    }

    @Override
    public void removeSubscriber(T subscriber) {
        if (subscriber != null) {
            synchronized (getLock()) {
                if (mSubscriberSet.contains(subscriber)) {
                    mSubscriberSet.remove(subscriber);
                    LogUtils.d("该订阅者 " + subscriber + " 完成退订");
                } else {
                    LogUtils.d("该订阅者 " + subscriber + " 不在订阅者列表中,无法进行退订");
                }
            }
        }
    }

    @Override
    public void clearUpSubscriber() {
        if (!mSubscriberSet.isEmpty()) {
            mSubscriberSet.clear();
        }
    }

    @Override
    public List<T> getListenersCopy() {
        synchronized (getLock()) {
            return (List<T>) mSubscriberSet.clone();
        }
    }

    @Override
    public boolean realRegister() {
        boolean registerResult = false;
        if (mReceiver != null && getIntentFilter() != null) {
            mApplicationContext.registerReceiver(mReceiver, getIntentFilter());
            registerResult = true;
        }
        return registerResult;
    }

    @Override
    public boolean realUnregister() {
        boolean unregisterResult = false;
        if (mReceiver != null) {
            mApplicationContext.unregisterReceiver(mReceiver);
            unregisterResult = true;
        }

        return unregisterResult;
    }

    @Override
    public void onDestroy() {
        clearUpSubscriber();
        unregister();
        mRegisterState = null;
        mUnregisterState = null;
        mBaseRegisteState = null;
        mSubscriberSet = null;
    }

    /**
     * 获取意图过滤
     * <p>
     * 子类扩展时候对该方法进行重写,明确监听内容
     *
     * @return 意图过滤对象
     */
    protected abstract IntentFilter getIntentFilter();


    /**
     * 获取广播接收者
     * <p>
     * 子类扩展时候对该方法进行重写,明确接收
     *
     * @return 意图过滤对象
     */
    protected abstract BroadcastReceiver getReceiver();


}
