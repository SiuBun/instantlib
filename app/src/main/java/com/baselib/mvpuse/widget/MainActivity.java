package com.baselib.mvpuse.widget;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;

import com.baselib.instant.mvp.BaseActivity;
import com.baselib.mvpuse.R;
import com.baselib.mvpuse.presenter.MainPresenter;
import com.baselib.mvpuse.view.MainView;

/**
 * 示例界面
 * <p>
 * 继承自{@link BaseActivity}并指定对应泛型,实现对应方法
 *
 * @author wsb
 */
public class MainActivity extends BaseActivity<MainPresenter, MainView> {

    private Fragment mFragmentMain;

    @Override
    protected void initData() {

    }


    @Override
    protected MainView getViewImpl() {
        return new MainView(){};
    }

    @Override
    protected MainPresenter iniPresenter() {
        return new MainPresenter();
    }

    @Override
    protected void initListener() {
    }

    @Override
    protected void initView() {
        mFragmentMain = getSupportFragmentManager().findFragmentById(R.id.frag_main_login);
    }

    @Override
    public int getContentId() {
        return R.layout.activity_main;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //实现只在冷启动时显示启动页，即点击返回键与点击HOME键退出效果一致
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
