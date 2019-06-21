package com.baselib.mvpuse.widget;

import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.baselib.instant.Const;
import com.baselib.instant.floatwindow.FloatButtonController;
import com.baselib.instant.manager.GlobalManager;
import com.baselib.instant.mvp.BaseFragment;
import com.baselib.instant.util.LogUtils;
import com.baselib.mvpuse.R;
import com.baselib.mvpuse.entry.AppInstantItemBean;
import com.baselib.mvpuse.presenter.MenuPresenter;
import com.baselib.mvpuse.view.MenuFragView;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

public class MenuFragment extends BaseFragment<MenuPresenter, MenuFragView> {

    private Button mBtnRx;
    private Button mBtnOther;
//    private FloatButtonController mFloatButtonController;

    @Override
    public int getFragmentLayout() {
        return R.layout.fragment_menu;
    }

    @Override
    protected MenuFragView getViewImpl() {
        return new MenuFragView() {
        };
    }

    @Override
    protected MenuPresenter initPresenter() {
        return new MenuPresenter();
    }

    @Override
    protected void initData() {
//        mFloatButtonController = (FloatButtonController) GlobalManager.getManager(GlobalManager.FLOAT_WINDOWS_SERVICE);

//        mFloatButtonController
//                .setShowType(FloatButtonController.getShowType(false))
//                .showFloatButton(getActivity());
    }

    @Override
    protected void initFragmentViews(View fragmentView) {

        mBtnRx = findViewById(R.id.btn_rx, Button.class);
        mBtnOther = findViewById(R.id.btn_other, Button.class);
    }

    @Override
    protected void initListener() {
        mBtnRx.setOnClickListener(v -> {
            Observer<String> observer = new Observer<String>() {

                private Disposable mDisposable;

                @Override
                public void onSubscribe(Disposable d) {
                    mDisposable = d;
                    LogUtils.d("Observer onSubscribe");
                }

                @Override
                public void onNext(String s) {
                    LogUtils.d( "Observer onNext:" + s);
                    // 在RxJava 2.x 中，新增的Disposable可以做到切断的操作，让Observer观察者不再接收上游事件,事件仍然分发
                    if ("cut".equals(s)) {
                        mDisposable.dispose();
                    }
                }

                @Override
                public void onError(Throwable e) {
                    LogUtils.d( "Observer onError:" + e);
                }

                @Override
                public void onComplete() {
                    LogUtils.d("Observer onComplete");
                }
            };
            Observable.create(e -> {
                LogUtils.d("ObservableEmitter Observable emit 1");
                e.onNext("1");
                LogUtils.d("ObservableEmitter Observable emit 2");
                e.onNext("2");
                LogUtils.d("ObservableEmitter Observable emit cut");
                e.onNext("cut");
                LogUtils.d("ObservableEmitter Observable emit 3");
                e.onNext("3");
                LogUtils.d("ObservableEmitter Observable emit 4");
                e.onNext("4");

                e.onComplete();
            }).map(s -> s + " after apply").subscribe(observer);

        });

        mBtnOther.setOnClickListener(v ->
                Observable.fromIterable(getPresenter().getInstallAppList(v.getContext()))
                        .map(AppInstantItemBean::getAppName)
                        .subscribe(str -> LogUtils.d("已经安装" + str)));

    }

    @Override
    public void widgetDestory() {
//        mFloatButtonController.closeFloatButton();
        super.widgetDestory();
    }
}
