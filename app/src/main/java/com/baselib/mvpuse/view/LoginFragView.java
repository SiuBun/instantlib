package com.baselib.mvpuse.view;

import com.baselib.instant.mvp.IMvpView;

public interface LoginFragView extends IMvpView {
    /**
     * 控制进度框
     * @param show true代表展示
     * */
    void controlProgress(boolean show);

    /**
     * 登录结果
     * @param result true代表登录成功
     * */
    void loginResult(boolean result);
}
