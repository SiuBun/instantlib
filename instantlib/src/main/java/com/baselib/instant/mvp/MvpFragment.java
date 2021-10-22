package com.baselib.instant.mvp;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * 项目中mvp架构的Fragment基类
 * <p>
 * 定义时候指定P层对象并要求实现对应的{@link IMvpView}子类接口
 *
 * @author wangshaobin
 */
public abstract class MvpFragment<P extends MvpPresenter> extends Fragment implements IMvpView, IPresenterOperate<P> {
    private P mPresenter;

    @Override
    public Activity reqActivity() {
        return requireActivity();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPresenter = initPresenter();
        getLifecycle().addObserver(mPresenter);
        mPresenter.attach(this);
    }

    @Override
    public void onDestroy() {
        getLifecycle().removeObserver(mPresenter);
        super.onDestroy();
    }

    @Override
    public P getPresenter() {
        return mPresenter;
    }
}
