package com.baselib.widget;

import android.content.Intent;
import android.view.KeyEvent;

import com.baselib.instant.mvp.BaseActivity;
import com.baselib.mvpuse.presenter.SplashPresenter;
import com.baselib.mvpuse.view.SplashView;

import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;

/**
 * 示例闪屏界面
 * <p>
 * 该界面不做其他的操作也不设置界面,依赖清单列表中activity节点的theme属性进行设置
 *
 * @author wsb
 */
public class SplashActivity extends BaseActivity<SplashPresenter> implements SplashView {

    @Override
    protected void initData() {
        getPresenter().checkPermissions(reqActivity(), 1001);
    }

    @Override
    public void delayedToJump() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                junpToMain();
            }
        };
        getHandler().postDelayed(runnable, TimeUnit.SECONDS.toMillis(1));
    }

    private void junpToMain() {
        Intent intent = new Intent(reqActivity(), MainActivity.class);
        startActivity(intent);
        finish();
        //取消界面跳转时的动画
        overridePendingTransition(0, 0);
    }

    @Override
    protected SplashPresenter iniPresenter() {
        return new SplashPresenter();
    }

    @Override
    protected void initListener() {

    }

    @Override
    protected void initView() {

    }

    @Override
    public int getContentId() {
        return 0;
    }

    @Override
    protected void activitySettingBeforeSetContent() {
//        在Activity中恢复原有的style样式【否则的话，当activity的布局文件设置背景色为透明的时候，就会发现窗口的背景还是那张图片】
//        setTheme(R.style.AppTheme);
    }

    /**
     * 屏蔽物理返回键
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        getPresenter().onRequestPermissionsResult(requestCode, permissions, grantResults);

    }
}