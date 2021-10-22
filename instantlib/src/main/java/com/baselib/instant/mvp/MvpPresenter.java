package com.baselib.instant.mvp;

import com.baselib.instant.util.LogUtils;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

/**
 * P层基类
 * <p>
 * 项目中presenter相关类都要继承自该类，所有业务层逻辑都在该类及扩展类处理.子类继承该类时候需要指定M层对象泛型便于完成M层对象的初始化
 *
 * @author wsb
 */
public abstract class MvpPresenter<V extends IMvpView, M extends BaseModel> implements DefaultLifecycleObserver {
    /**
     * 内部所持有的控件操作对象
     */
    private Reference<V> mViewRef;
    /**
     * 内部所持有的数据操作对象
     */
    private M mModel;

    public MvpPresenter() {
        mModel = initModel();
    }

    /**
     * 初始化M层对象,M层类型根据所传泛型决定
     *
     * @return M层对象
     */
    public abstract M initModel();

    /**
     * 同步V层生命周期onCreate,创建时候进行view层对象挂载
     */
    public void attach(V view) {
        mViewRef = new WeakReference<>(view);
    }

    /**
     * 对外提供的获取M层对象方法
     *
     * @return M层对象
     */
    public M getModel() {
        return mModel;
    }

    /**
     * 对外提供的获取V层对象方法
     *
     * @return V层对象
     */
    public V getView() {
        return isViewAttached() ? mViewRef.get() : null;
    }

    public boolean isViewAttached() {//判断是否与View建立了关联
        return mViewRef != null && mViewRef.get() != null;
    }

    @Override
    public void onCreate(@NonNull LifecycleOwner owner) {
        LogUtils.lifeLog(this.getClass().getSimpleName(), " onCreate");
    }

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        LogUtils.lifeLog(this.getClass().getSimpleName(), " onStart");
    }

    @Override
    public void onResume(@NonNull LifecycleOwner owner) {
        LogUtils.lifeLog(this.getClass().getSimpleName(), " onResume");
    }

    @Override
    public void onPause(@NonNull LifecycleOwner owner) {
        LogUtils.lifeLog(this.getClass().getSimpleName(), " onPause");
    }

    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        LogUtils.lifeLog(this.getClass().getSimpleName(), " onStop");
    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
        LogUtils.lifeLog(this.getClass().getSimpleName(), " onDestroy");

        mModel.onModelDetach(getView().reqActivity());
        mModel = null;

        mViewRef.clear();
        mViewRef = null;
    }
}
