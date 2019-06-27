package com.baselib.mvpuse.presenter;

import android.content.Context;

import com.baselib.instant.mvp.BasePresenter;
import com.baselib.mvpuse.view.MainView;
import com.baselib.mvpuse.model.MainModel;

public class MainPresenter extends BasePresenter<MainView, MainModel> {

    @Override
    public MainModel initModel() {
        return new MainModel();
    }


    @Override
    public void detach(Context context) {
        super.detach(context);
    }
}
