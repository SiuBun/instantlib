package com.baselib.mvpuse.presenter;

import com.baselib.instant.mvp.BasePresenter;
import com.baselib.mvpuse.model.SplashModel;
import com.baselib.mvpuse.view.SplashView;

public class SplashPresenter extends BasePresenter<SplashView, SplashModel> {
    @Override
    public SplashModel initModel() {
        return new SplashModel();
    }
}
