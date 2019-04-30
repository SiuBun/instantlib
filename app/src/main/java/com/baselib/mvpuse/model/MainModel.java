package com.baselib.mvpuse.model;

import com.baselib.instant.mvp.BaseModel;
import com.baselib.mvpuse.presenter.MainPresenter;

public class MainModel extends BaseModel {
    public void doLogin(String account, String pws, MainPresenter.ILoginListener loginListener) {
        if ("abc".equals(account)&&"123".equals(pws)){
            loginListener.loginResult(true);
        }else {
            loginListener.loginResult(false);
        }
        loginListener.loginFinish();
    }
}
