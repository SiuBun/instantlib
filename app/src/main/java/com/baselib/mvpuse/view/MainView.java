package com.baselib.mvpuse.view;


import com.baselib.instant.mvp.IBaseView;

public interface MainView extends IBaseView {
    void controlProgress(boolean show);

    void loginResult(boolean result);
}
