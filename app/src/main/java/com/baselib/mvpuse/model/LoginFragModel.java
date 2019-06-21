package com.baselib.mvpuse.model;

import com.baselib.instant.mvp.BaseModel;
import com.baselib.mvpuse.presenter.LoginFragPresenter;

public class LoginFragModel extends BaseModel {
    public void doLogin(String account, String pws, LoginFragPresenter.ILoginListener loginListener) {
        if ("abc".equals(account)&&"123".equals(pws)){
            loginListener.loginResult(true);
        }else {
            loginListener.loginResult(false);
        }
        loginListener.loginFinish();
    }
}
