package com.baselib.instant.mvp;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;

/**
 * activity基类
 * <p>
 * 项目中activity相关类都要继承自该类.可以通过实现{@link #getContentId()},{@link #buildProgressBar()}等来实现界面快速构建.
 * <p>
 * 子类继承该类时候需要传入对应泛型指定P层和V层,原先需要子类手动继承,后优化调整为子类提供接口实例即可,从面向方法调整为面向接口编程
 *
 * @author wsb
 */
public abstract class BaseActivity<P extends BasePresenter, V extends IBaseView> extends Activity {
    private P mPresenter;
    private AlertDialog mProgressBar;
    private String tag = "mvp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        activitySettingBeforeCreate();
        super.onCreate(savedInstanceState);
        setContentView(getContentId());
        activitySettingAfterCreate();

        initView();
        initListener();
        mPresenter = iniPresenter();
        if (mPresenter != null) {
            mPresenter.attach(getViewImpl());
        }
    }

    /**
     * 设置基类中所内置控件
     * <p>
     * 基类中所内置控件的初始化等操作
     */
    private void supportView() {
        mProgressBar = buildProgressBar();
    }

    /**
     * 构建进度框
     * <p>
     * 当前默认一个适用于loading场景下的进度框，子类可以重写该方法进行loading框自定义
     *
     * @return 可展示在界面上的进度框
     */
    public AlertDialog buildProgressBar() {
        return new AlertDialog.Builder(this).setTitle("请稍候").create();
    }

    /**
     * 对界面内的进度框进行操作
     *
     * @param show true代表展示，false则是隐藏
     */
    public void controlProgressBar(final boolean show) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (show) {
                    if (!mProgressBar.isShowing()) {
                        mProgressBar.show();

                        Log.d(tag, "弹窗展示");
                    }
                } else {
                    mProgressBar.dismiss();
                    Log.d(tag, "弹窗关闭");
                }
            }
        });
    }

    /**
     * 获取V层绑定对象
     * <p>
     * 子类实现该方法完成和P层对象的绑定
     *
     * @return V层绑定对象
     */
    protected abstract V getViewImpl();

    @Override
    protected void onDestroy() {
        widgetDestory();
        mPresenter.detach();
        super.onDestroy();
    }

    /**
     * 控件回收
     * <p>
     * 界面销毁阶段调用该方法进行相关控件销毁，如果子类有相关控件销毁操作可以重写该方法，在方法中执行
     */
    public void widgetDestory() {
        controlProgressBar(false);
        mProgressBar = null;
    }

    /**
     * 初始化P层对象
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

    /**
     * 初始化界面内相关监听
     */
    protected abstract void initListener();

    /**
     * 初始化界面内控件
     */
    protected abstract void initView();

    /**
     * 获取界面布局控件对象
     * <p>
     * 子类重写该方法对界面进行布局
     *
     * @return 加载到界面上的布局id
     */
    public abstract int getContentId();

    /**
     * 可以在设置内容布局之前对activity的相关操作，如设置全屏,无标题,沉浸式等
     */
    protected void activitySettingBeforeCreate() {

    }

    /**
     * 可以在设置内容布局之后对activity的相关操作
     */
    protected void activitySettingAfterCreate() {
        supportView();
    }
}
