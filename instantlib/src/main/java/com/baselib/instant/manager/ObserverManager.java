package com.baselib.instant.manager;

import android.content.Context;

import com.baselib.instant.observer.observer.AppChangeObserver;
import com.baselib.instant.observer.IObserver;
import com.baselib.instant.util.LogUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 观察者管理对象
 *
 * @author wsb
 */
public class ObserverManager implements IManager {

    private static final String APP_CHANGE_OBSERVER_NAME = "AppChangeObserver";
    private Map<String, IObserver> mObserverMap;

    public ObserverManager() {
        mObserverMap = new HashMap<>();
    }

    @Override
    public void detach() {
        for (String name : mObserverMap.keySet()) {
            IObserver observer = mObserverMap.get(name);
            if (observer != null) {
                observer.onDestroy();
            }
        }
        mObserverMap = null;
    }

    /**
     * 该方法内对观察者管理对象进行初始化
     * <p>
     * 后面get方法获取到的观察者对象不为空需要在此put进容器钟
     *
     * @param context 上下文
     */
    public void attach(Context context) {
        mObserverMap.put(APP_CHANGE_OBSERVER_NAME, new AppChangeObserver(context));
    }

    public AppChangeObserver getAppChangeObserver() {
        return getObserver(APP_CHANGE_OBSERVER_NAME, AppChangeObserver.class);
    }

    private <T extends IObserver> T getObserver(String name, Class<T> clz) {
        IObserver observer = mObserverMap.get(name);
        if (observer == null) {
            LogUtils.d("该观察者暂未被支持");
        }
        return observer == null ? null : (T) observer;
    }
}
