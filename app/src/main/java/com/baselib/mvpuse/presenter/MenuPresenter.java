package com.baselib.mvpuse.presenter;

import android.content.Context;
import android.content.pm.PackageInfo;

import com.baselib.instant.manager.GlobalManager;
import com.baselib.instant.observer.ObserverManager;
import com.baselib.instant.mvp.BasePresenter;
import com.baselib.instant.observer.observer.AppChangeObserver;
import com.baselib.instant.util.LogUtils;
import com.baselib.mvpuse.entry.AppInstantItemBean;
import com.baselib.mvpuse.model.MenuFragModel;
import com.baselib.mvpuse.view.MenuFragView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.Response;

public class MenuPresenter extends BasePresenter<MenuFragView, MenuFragModel> implements AppChangeObserver.OnAppChangedListener {

    private AppChangeObserver mAppChangeObserver;

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
        mAppChangeObserver = observerManager.getAppChangeObserver();
        mAppChangeObserver.addSubscriber(this);
    }

    @Override
    public void onAppInstalled(String pkgName) {
        LogUtils.i("客户端收到onAppInstalled " + pkgName);
    }

    @Override
    public void onAppUninstalled(String pkgName) {
        LogUtils.i("客户端收到onAppUninstalled " + pkgName);
    }

    @Override
    public void onAppReplaced(String pkgName) {
        LogUtils.i("客户端收到onAppReplaced " + pkgName);

    }

    @Override
    public void onPresenterDetach(Context context) {
        mAppChangeObserver.removeSubscriber(this);

        super.onPresenterDetach(context);
    }
}
