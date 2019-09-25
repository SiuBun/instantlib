package com.baselib.mvpuse.presenter;

import android.os.CountDownTimer;

import com.baselib.instant.mvp.BasePresenter;
import com.baselib.instant.util.LogUtils;
import com.baselib.mvpuse.model.LoginFragModel;
import com.baselib.mvpuse.view.LoginFragView;

import java.util.concurrent.TimeUnit;

public class LoginFragPresenter extends BasePresenter<LoginFragView, LoginFragModel> {
    @Override
    public LoginFragModel initModel() {
        return new LoginFragModel();
    }

    public void doLogin(final String account, final String pws) {
        getView().controlProgress(true);
        CountDownTimer countDownTimer = new CountDownTimer(TimeUnit.SECONDS.toMillis(1), TimeUnit.SECONDS.toMillis(1)){

            @Override
            public void onTick(long millisUntilFinished) {
                LogUtils.d("倒数时间"+TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished));
            }

            @Override
            public void onFinish() {
                getModel().doLogin(account,pws,getLoginListener());
            }
        };
        countDownTimer.start();
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
