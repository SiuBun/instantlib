package com.baselib.mvpuse.widget;

import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.baselib.instant.floatwindow.FloatButtonController;
import com.baselib.instant.manager.GlobalManager;
import com.baselib.instant.mvp.BaseFragment;
import com.baselib.instant.util.LogUtils;
import com.baselib.mvpuse.R;
import com.baselib.mvpuse.entry.AppInstantItemBean;
import com.baselib.mvpuse.presenter.MenuPresenter;
import com.baselib.mvpuse.view.MenuFragView;
import com.baselib.mvvmuse.view.MvvmTestActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Objects;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Response;

public class MenuFragment extends BaseFragment<MenuPresenter, MenuFragView> {

    private Button mBtnRx;
    private Button mBtnOther;
//    private FloatButtonController mFloatButtonController;
    private Button mBtnNetData;
    private Button mBtnRetrofit;
    private Button mBtnMvvm;

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
//        mFloatButtonController = (FloatButtonController) GlobalManager.Companion.getManager(GlobalManager.FLOAT_WINDOWS_SERVICE);

//        mFloatButtonController
//                .setShowType(FloatButtonController.getShowType(false))
//                .showFloatButton(getActivity());
    }

    @Override
    protected void initFragmentViews(View fragmentView) {

        mBtnRx = findViewById(R.id.btn_rx, Button.class);
        mBtnOther = findViewById(R.id.btn_get_apps, Button.class);
        mBtnNetData = findViewById(R.id.btn_net_data, Button.class);
        mBtnRetrofit = findViewById(R.id.btn_retrofit, Button.class);
        mBtnMvvm = findViewById(R.id.btn_mvvm, Button.class);
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
                    LogUtils.d("Observer onNext:" + s);
                    // 在RxJava 2.x 中，新增的Disposable可以做到切断的操作，让Observer观察者不再接收上游事件,事件仍然分发
                    if ("cut".equals(s)) {
                        mDisposable.dispose();
                    }
                }

                @Override
                public void onError(Throwable e) {
                    LogUtils.d("Observer onError:" + e);
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
                Observable.fromIterable(
                        getPresenter().getInstallAppList(v.getContext())
                )
                        .map(AppInstantItemBean::getAppName)
                        .doOnError(Throwable::printStackTrace)
                        .subscribe(str -> LogUtils.d("已经安装" + str))
        );

        mBtnNetData.setOnClickListener(v ->
                Observable.create((ObservableOnSubscribe<Response>) e -> e.onNext(getPresenter().getJoke()))
                        .subscribeOn(Schedulers.io())
                        .flatMap(response -> new Observable<String>() {
                            @Override
                            protected void subscribeActual(Observer<? super String> observer) {
                                try {
                                    String string = Objects.requireNonNull(response.body()).string();
                                    LogUtils.i("获取到的结果为 " + string);
                                    JSONObject jsonObject = new JSONObject(string);
                                    JSONArray jsonArray = jsonObject.optJSONArray("result");
                                    for (int index = 0; index < jsonArray.length(); index++) {
                                        observer.onNext(jsonArray.getJSONObject(index).optString("text"));
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }).take(3)
                        .doOnError(Throwable::printStackTrace)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(title -> Toast.makeText(getActivity(), "获取到的标题为" + title, Toast.LENGTH_SHORT).show())
        );

        mBtnRetrofit.setOnClickListener(v -> {

        });

        mBtnMvvm.setOnClickListener(v->{
            startActivity(MvvmTestActivity.class);
        });

        getPresenter().observerAppChange();
    }

    @Override
    public void widgetDestory() {
//        mFloatButtonController.closeFloatButton();
        super.widgetDestory();
    }
}
