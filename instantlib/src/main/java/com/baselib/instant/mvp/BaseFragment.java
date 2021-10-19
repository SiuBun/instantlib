package com.baselib.instant.mvp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.baselib.instant.manager.BusinessHandler;
import com.baselib.instant.util.LogUtils;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

/**
 * 项目中mvp架构的Fragment基类
 * <p>
 * 定义时候指定P层对象并要求实现对应的{@link IBaseView}子类接口
 * <p>
 * 其子类只需要关注
 * <Li>{@link #initPresenter()}应该使用什么样的P层对象</Li>
 * <Li>{@link #getFragmentLayout()}方法返回什么内容的布局</Li>
 * <Li>{@link #initFragmentViews(View)}布局内容里哪些控件对象需要实体化</Li>
 *
 * @author wsb
 */
public abstract class BaseFragment<P extends BasePresenter, V extends IBaseView> extends Fragment {
    private View mFragmentView;
    private P mBasePresenter;
    private AlertDialog mProgressBar;
    private BusinessHandler mHandler;

    /**
     * 获取当前片段所用界面布局
     *
     * @return 展示的布局id
     */
    public abstract @LayoutRes
    int getFragmentLayout();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        LogUtils.lifeLog(this.getClass().getSimpleName(), " onCreateView");
        mFragmentView = inflater.inflate(getFragmentLayout(), container, false);

        initFragmentViews(mFragmentView);
        fragmentSetting();
        initHandler();
        mBasePresenter = initPresenter();
        if (mBasePresenter != null) {
            mBasePresenter.attach(getViewImpl());
        }
        initListener();
        initData();
        return mFragmentView;
    }

    private void fragmentSetting() {
        supportView();
    }

    /**
     * 获取V层绑定对象
     * <p>
     * 子类实现该方法完成和P层对象的绑定
     *
     * @return V层绑定对象
     */
    protected abstract V getViewImpl();

    /**
     * 初始化P层对象
     *
     * @return P层逻辑对象
     */
    protected abstract P initPresenter();

    /**
     * 加载数据
     */
    protected abstract void initData();

    /**
     * 初始化该fragment内的控件对象
     *
     * @param fragmentView fragment对象所填充的布局
     */
    protected abstract void initFragmentViews(View fragmentView);

    private void initHandler() {
        mHandler = new BusinessHandler(Looper.getMainLooper(), getHandlerListener());
    }

    /**
     * 如果子类需要Handler的消息做回调处理,可以直接重写该方法进行处理
     *
     * @return 消息回调处理对象
     */
    public BusinessHandler.IHandlerMsgListener getHandlerListener() {
        return null;
    }

    public P getPresenter() {
        return mBasePresenter;
    }

    /**
     * 初始化界面内相关监听
     */
    protected abstract void initListener();

    /**
     * 根据id实例化指定控件
     *
     * @param viewId 控件id
     * @return id对应的控件对象
     */
    public <C extends View> C findViewById(@IdRes int viewId, Class<C> clz) {
        return (C) mFragmentView.findViewById(viewId);
    }

    /**
     * 打开新的fragment并入栈
     *
     * @param fragmentRoot 挂载fragment界面的容器id
     * @param fragment     目标fragment对象
     */
    public void startFragmentByClz(@IdRes int fragmentRoot, Fragment fragment) {
        startFragmentByClz(fragmentRoot, fragment, false);
    }

    public void startFragmentByClz(@IdRes int fragmentRoot, Fragment fragment, boolean removeCurrent) {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        if (removeCurrent) {
            transaction.replace(fragmentRoot, fragment);
        } else {
            transaction.hide(this);
            transaction.add(fragmentRoot, fragment);
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }

    /**
     * 打开新界面
     *
     * @param activityClass 目标界面的字节码对象
     */
    public void startActivity(Class<? extends Activity> activityClass) {
        FragmentActivity activity = getActivity();
        if (activity != null) {
            activity.startActivity(new Intent(activity, activityClass));
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
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).setTitle("正在请求数据").setMessage("请稍候").create();
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
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (show) {
                    if (!mProgressBar.isShowing()) {
                        mProgressBar.show();
                        LogUtils.d("弹窗展示");
                    }
                } else {
                    if (mProgressBar.isShowing()) {
                        mProgressBar.dismiss();
                        LogUtils.d("弹窗关闭");
                    }
                }
            }
        });
    }

    /**
     * 销毁时候针对P层对象做回收处理
     */
    @Override
    public void onDestroyView() {
        LogUtils.lifeLog(this.getClass().getSimpleName(), " onDestroyView");
        widgetDestroy();
        mBasePresenter.onPresenterDetach(getActivity());
        mHandler.onDestroy();
        super.onDestroyView();
    }

    /**
     * 控件回收
     * <p>
     * 界面销毁阶段调用该方法进行相关控件销毁，如果子类有相关控件销毁操作可以重写该方法，在方法中执行
     */
    public void widgetDestroy() {
        controlProgressBar(false);
        mProgressBar = null;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        LogUtils.lifeLog(this.getClass().getSimpleName(), " onAttach");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.lifeLog(this.getClass().getSimpleName(), " onCreate");
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LogUtils.lifeLog(this.getClass().getSimpleName(), " onViewCreated");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        LogUtils.lifeLog(this.getClass().getSimpleName(), " onActivityCreated");
    }

    @Override
    public void onStart() {
        super.onStart();
        LogUtils.lifeLog(this.getClass().getSimpleName(), " onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        LogUtils.lifeLog(this.getClass().getSimpleName(), " onResume");
    }

    @Override
    public void onPause() {
        LogUtils.lifeLog(this.getClass().getSimpleName(), " onPause");
        super.onPause();
    }

    @Override
    public void onStop() {
        LogUtils.lifeLog(this.getClass().getSimpleName(), " onStop");
        super.onStop();
    }

    @Override
    public void onDestroy() {
        LogUtils.lifeLog(this.getClass().getSimpleName(), " onDestroy");
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        LogUtils.lifeLog(this.getClass().getSimpleName(), " onDetach");
        super.onDetach();
    }

}

