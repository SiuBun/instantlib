package com.baselib.mvpuse.presenter;

import com.baselib.instant.mvp.BasePresenter;
import com.baselib.mvpuse.model.LoginFragModel;
import com.baselib.mvpuse.view.LoginFragView;

public class LoginFragPresenter extends BasePresenter<LoginFragView, LoginFragModel> {
    @Override
    public LoginFragModel initModel() {
        return new LoginFragModel();
    }

    public void doLogin(String account, String pws) {
        getModel().doLogin(account,pws,getLoginListener());
    }

    private ILoginListener getLoginListener() {
        return new ILoginListener() {
            @Override
            public void loginFinish() {
                getView().controlProgress(false);
            }

            @Override
            public void loginResult(boolean result) {
                getView().loginResult(result);
            }
        };
    }

    public interface ILoginListener {
        void loginFinish();

        void loginResult(boolean success);
    }
}
