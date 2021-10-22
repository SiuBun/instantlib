package com.baselib.instant.mvp;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * activity基类
 * <p>
 * 项目中mvp架构的activity相关类都要继承自该类.
 *
 * @author wsb
 */
public abstract class MvpActivity<P extends BasePresenter> extends AppCompatActivity implements IMvpView {
    private P mPresenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPresenter = iniPresenter();
        getLifecycle().addObserver(mPresenter);
        mPresenter.attach(this);
    }

    @Override
    protected void onDestroy() {
        getLifecycle().removeObserver(mPresenter);
        super.onDestroy();
    }

    /**
     * 初始化P层对象
     *
     * @return P层逻辑对象
     */
    protected abstract P iniPresenter();

    /**
     * 获取该界面内的P层对象
     *
     * @return 界面对应的P层对象
     */
    public P getPresenter() {
        return mPresenter;
    }

    public Activity getActivity() {
        return this;
    }

    @Override
    public Activity reqActivity() {
        return this;
    }

    @Override
    public void toast(String content) {
        reqActivity().runOnUiThread(() -> Toast.makeText(getActivity(), content, Toast.LENGTH_SHORT).show());
    }
}
