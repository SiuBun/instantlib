package com.baselib.mvpuse.presenter;

import com.baselib.instant.mvp.BasePresenter;
import com.baselib.mvpuse.model.MainModel;
import com.baselib.mvpuse.view.MainView;

public class MainPresenter extends BasePresenter<MainView, MainModel> {

    @Override
    public MainModel initModel() {
        return new MainModel();
    }

}
