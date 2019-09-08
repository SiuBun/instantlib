package com.baselib.mvpuse.presenter;

import android.content.Context;
import android.content.pm.PackageInfo;

import com.baselib.instant.manager.GlobalManager;
import com.baselib.instant.mvp.BasePresenter;
import com.baselib.instant.observer.ObserverManager;
import com.baselib.instant.observer.observer.NetStateObserver;
import com.baselib.instant.util.LogUtils;
import com.baselib.mvpuse.entry.AppInstantItemBean;
import com.baselib.mvpuse.model.MenuFragModel;
import com.baselib.mvpuse.view.MenuFragView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.Response;

public class MenuPresenter extends BasePresenter<MenuFragView, MenuFragModel>{

    private NetStateObserver mNetStateObserver;
    private NetStateObserver.OnNetStateChangeListener onNetStateChangeListener = new NetStateObserver.OnNetStateChangeListener() {
        @Override
        public void onNetStateChanged(boolean isAvailable) {
            LogUtils.i("当前网络状态可用：" + isAvailable);
        }

        @Override
        public void onWifiStateChanged(boolean isConnected) {
            LogUtils.i("当前wifi状态发生变化，是否为连接状态：" + isConnected);
        }
    };;

    @Override
    public MenuFragModel initModel() {
        return new MenuFragModel();
    }

    public ArrayList<AppInstantItemBean> getInstallAppList(Context context) {
        List<AppInstantItemBean> systemAppList = new ArrayList<>();
        List<AppInstantItemBean> userAppList = new ArrayList<>();
        AppInstantItemBean bean;
        List<PackageInfo> list = context.getPackageManager().getInstalledPackages(0);
        for (PackageInfo p : list) {
            bean = AppInstantItemBean.parseByPackageInfo(context.getPackageManager(), p);
            if (bean.isSystem()) {
                systemAppList.add(bean);
            } else {
                userAppList.add(bean);
            }
        }
        Collections.sort(systemAppList);
        Collections.sort(userAppList);
        ArrayList<AppInstantItemBean> result = new ArrayList<>();
        result.addAll(userAppList);
        result.addAll(systemAppList);
        return result;
    }

    public Response getJoke() {
        return getModel().getJokeByOkhttp();
    }

    public void observerAppChange() {
        LogUtils.i("客户端界面进行应用变化监听");

        ObserverManager observerManager = (ObserverManager) GlobalManager.Companion.getManager(GlobalManager.OBSERVER_SERVICE);
        mNetStateObserver = observerManager.getNetStateObserver();
        mNetStateObserver.addSubscriber(onNetStateChangeListener);
    }




    @Override
    public void onPresenterDetach(Context context) {
        mNetStateObserver.removeSubscriber(onNetStateChangeListener);

        super.onPresenterDetach(context);
    }
}
