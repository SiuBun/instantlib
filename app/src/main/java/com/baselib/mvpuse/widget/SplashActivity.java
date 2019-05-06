package com.baselib.mvpuse.widget;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;

import com.baselib.instant.mvp.BaseActivity;
import com.baselib.instant.permission.PermissionsManager;
import com.baselib.mvpuse.R;
import com.baselib.mvpuse.model.SplashModel;
import com.baselib.mvpuse.presenter.SplashPresenter;
import com.baselib.mvpuse.view.SplashView;

import java.util.Arrays;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * 示例闪屏界面
 * <p>
 * 该界面不做其他的操作也不设置界面,依赖清单列表中activity节点的theme属性进行设置
 *
 * @author wsb
 */
public class SplashActivity extends BaseActivity<SplashPresenter, SplashView> {

    @Override
    protected void initData() {
        SplashModel model = getPresenter().getModel();

        PermissionsManager.getInstance().checkPermissions(getActivity(), model.getPermissions(), 1001, new PermissionsManager.IPermissionsCheckCallback() {

            @Override
            public void onCheckedFinish(String[] permissionsBeDenied) {
                if (permissionsBeDenied==null||permissionsBeDenied.length == 0){
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            junpToMain();
                        }
                    };
                    getHandler().postDelayed(runnable, TimeUnit.SECONDS.toMillis(1));
                }else {
                    Log.d(TAG,"被拒绝的权限如下"+ Arrays.toString(permissionsBeDenied));

                    PermissionsManager.getInstance().notifyReqPermission(getActivity(),permissionsBeDenied);

                }
            }
        });
    }

    private void junpToMain() {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        startActivity(intent);
        finish();
        //取消界面跳转时的动画
        overridePendingTransition(0, 0);
    }

    @Override
    protected SplashView getViewImpl() {
        return new SplashView() {
        };
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionsManager.getInstance().onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}