package com.baselib.instant.mvp;

import android.app.Activity;
import android.os.Bundle;
import android.os.Looper;

import com.baselib.instant.manager.BusinessHandler;
import com.baselib.instant.util.LogUtils;

import androidx.annotation.LayoutRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

/**
 * activity基类
 * <p>
 * 项目中mvp架构的activity相关类都要继承自该类.可以通过实现
 * <Li>{@link #getContentId()},
 * <Li>{@link #buildProgressBar()}
 * 等抽象方法来实现界面快速构建.
 * <p>
 * 子类继承该类时候需要传入对应泛型指定P层和V层,原先需要子类手动继承,后优化调整为子类提供接口实例即可,从面向方法调整为面向接口编程
 *
 * @author wsb
 */
public abstract class BaseActivity<P extends BasePresenter, V extends IBaseView> extends AppCompatActivity {
    private P presenter;
    private AlertDialog progressBar;
    private BusinessHandler businessHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogUtils.lifeLog(this.getClass().getSimpleName(), " onCreate");
        super.onCreate(savedInstanceState);
        activitySettingBeforeSetContent();
        if (0 != getContentId()) {
            setContentView(getContentId());
        }
        activitySettingAfterSetContent();

//        初始化控件和监听,及轮询处理等
        initView();
        presenter = iniPresenter();
        if (presenter != null) {
            presenter.attach(getViewImpl());
        }
        initListener();

        initData();
    }

    private void initHandler() {
        businessHandler = new BusinessHandler(Looper.getMainLooper(), getHandlerListener());
    }

    /**
     * 如果子类需要Handler的消息做回调处理,可以直接重写该方法进行处理
     *
     * @return 消息回调处理对象
     */
    public BusinessHandler.IHandlerMsgListener getHandlerListener() {
        return null;
    }

    /**
     * 加载数据
     */
    protected abstract void initData();

    /**
     * 设置基类中所内置控件
     * <p>
     * 基类中所内置控件的初始化等操作
     */
    private void supportView() {
        progressBar = buildProgressBar();
    }

    /**
     * 构建进度框
     * <p>
     * 当前默认一个适用于loading场景下的进度框，子类可以重写该方法进行loading框自定义
     *
     * @return 可展示在界面上的进度框
     */
    public AlertDialog buildProgressBar() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).setTitle("正在请求数据").setMessage("请稍候").create();
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        return alertDialog;
    }

    /**
     * 对界面内的进度框进行操作
     *
     * @param show true代表展示，false则是隐藏
     */
    public void controlProgressBar(final boolean show) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (show) {
                    if (!progressBar.isShowing()) {
                        progressBar.show();
                        LogUtils.d("项目弹窗展示");
                    }
                } else {
                    if (progressBar.isShowing()) {
                        progressBar.dismiss();
                        LogUtils.d("项目弹窗关闭");
                    }
                }
            }
        };
        runOnUiThread(runnable);
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
        LogUtils.lifeLog(this.getClass().getSimpleName(), " onDestroy");
        widgetDestory();
        businessHandler.onDestroy();
        presenter.onPresenterDetach(getActivity());
        super.onDestroy();
    }

    /**
     * 控件回收
     * <p>
     * 界面销毁阶段调用该方法进行相关控件销毁，如果子类有相关控件销毁操作可以重写该方法，在方法中执行
     */
    public void widgetDestory() {
        controlProgressBar(false);
        progressBar = null;
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
        return presenter;
    }

    public BusinessHandler getHandler() {
        return businessHandler;
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
    public abstract @LayoutRes
    int getContentId();

    /**
     * 可以在设置内容布局之前对activity的相关操作，如设置全屏,无标题,沉浸式等
     */
    protected void activitySettingBeforeSetContent() {

    }

    /**
     * 可以在设置内容布局之后对activity的相关操作
     */
    protected void activitySettingAfterSetContent() {
        supportView();
        initHandler();
    }

    public Activity getActivity() {
        return this;
    }

    @Override
    protected void onStart() {
        super.onStart();
        LogUtils.lifeLog(this.getClass().getSimpleName(), " onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtils.lifeLog(this.getClass().getSimpleName(), " onResume");
    }

    @Override
    protected void onPause() {
        LogUtils.lifeLog(this.getClass().getSimpleName(), " onPause");
        super.onPause();
    }

    @Override
    protected void onStop() {
        LogUtils.lifeLog(this.getClass().getSimpleName(), " onStop");
        super.onStop();
    }

}
