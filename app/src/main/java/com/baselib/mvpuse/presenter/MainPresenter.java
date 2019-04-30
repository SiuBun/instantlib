package com.baselib.mvpuse.presenter;

import com.baselib.instant.mvp.BasePresenter;
import com.baselib.mvpuse.view.MainView;
import com.baselib.mvpuse.model.MainModel;

public class MainPresenter extends BasePresenter<MainView, MainModel> {

    @Override
    public MainModel initModel() {
        return new MainModel();
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
