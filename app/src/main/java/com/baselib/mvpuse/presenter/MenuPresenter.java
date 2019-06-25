package com.baselib.mvpuse.presenter;

import android.content.Context;
import android.content.pm.PackageInfo;

import com.baselib.instant.mvp.BasePresenter;
import com.baselib.mvpuse.entry.AppInstantItemBean;
import com.baselib.mvpuse.model.MenuFragModel;
import com.baselib.mvpuse.view.MenuFragView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.Response;

public class MenuPresenter extends BasePresenter<MenuFragView, MenuFragModel> {
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
                bean = AppInstantItemBean.parseByPackageInfo(context.getPackageManager(),p);
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
}
