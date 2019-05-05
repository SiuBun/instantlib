package com.baselib.mvpuse.view;


import com.baselib.instant.mvp.IBaseView;
/**
 * 示例代码
 * <p>
 * 继承基类后在该视图操作类中定义对界面的控件的操作
 *
 * @author wsb
 */
public interface MainView extends IBaseView {
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
