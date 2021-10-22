package com.baselib.mvpuse.presenter;

import com.baselib.instant.mvp.BaseModel;
import com.baselib.instant.mvp.BasePresenter;
import com.baselib.instant.mvp.IMvpView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * 如果使用了rx的话，可以使用该P层对象进行扩展
 *
 * @author wangshaobin
 */
public abstract class RxBasePresenter<V extends IMvpView, M extends BaseModel> extends BasePresenter<V, M> {
    /**
     * 正在执行的可消费事件
     */
    private CompositeDisposable mDisposable;

    /**
     * 同步V层生命周期onDestroy,销毁时候进行view层对象解除挂载
     */
    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
        unRxSubscribe();
        super.onDestroy(owner);
    }

    /**
     * 解绑当前对象里产生的rx消费对象防止内存泄露
     */
    private void unRxSubscribe() {
        if (mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.clear();
            mDisposable = null;
        }
    }

    /**
     * 产生的rx消费对象添加到对应管理对象中便于处理
     *
     * @param disposable 产生的消费对象
     */
    protected void addDisposable(Disposable disposable) {
        if (mDisposable != null) {
            mDisposable.dispose();
        }

        this.mDisposable = new CompositeDisposable();
        this.mDisposable.add(disposable);
    }
}
