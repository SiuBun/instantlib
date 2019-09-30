package com.baselib.mvpuse.presenter;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Build;

import com.baselib.entity.FilmDetailBean;
import com.baselib.entity.FilmItemBean;
import com.baselib.entity.MtimeFilmeBean;
import com.baselib.instant.manager.GlobalManager;
import com.baselib.instant.mvp.BasePresenter;
import com.baselib.instant.observer.ObserverManager;
import com.baselib.instant.observer.observer.NetStateObserver;
import com.baselib.instant.repository.RepositoryManager;
import com.baselib.instant.util.LogUtils;
import com.baselib.mvpuse.entry.AppInstantItemBean;
import com.baselib.mvpuse.model.MenuFragModel;
import com.baselib.mvpuse.view.MenuFragView;
import com.baselib.repository.HostClient;
import com.baselib.repository.ModuleClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Response;
import retrofit2.Retrofit;

public class MenuPresenter extends BasePresenter<MenuFragView, MenuFragModel> {

    private NetStateObserver mNetStateObserver;
    private NetStateObserver.OnNetStateChangeListener onNetStateChangeListener = new NetStateObserver.OnNetStateChangeListener() {
        @Override
        public void onNetStateChanged(boolean isAvailable) {
            LogUtils.i("当前网络状态可用与否：" + isAvailable);
        }

        @Override
        public void onWifiStateChanged(boolean isConnected) {
            LogUtils.i("当前wifi状态发生变化，是否为连接状态：" + isConnected);
        }
    };
    ;

    @Override
    public MenuFragModel initModel() {
        return new MenuFragModel();
    }

    private ArrayList<AppInstantItemBean> getInstallAppList(Context context) {
        List<AppInstantItemBean> systemAppList = new ArrayList<>();
        List<AppInstantItemBean> userAppList = new ArrayList<>();
        AppInstantItemBean bean;
        List<PackageInfo> list = context.getPackageManager().getInstalledPackages(0);
        for (PackageInfo p : list) {
            bean = AppInstantItemBean.parseByPackageInfo(context.getPackageManager(), p);
            if (bean.isSystem()) {
                systemAppList.add(bean);
            } else {
                userAppList.add(bean);
            }
        }
        Collections.sort(systemAppList);
        Collections.sort(userAppList);
        ArrayList<AppInstantItemBean> result = new ArrayList<>();
        result.addAll(userAppList);
        result.addAll(systemAppList);
        return result;
    }

    public Response getJoke() {
        return getModel().getJokeByOkhttp();
    }

    public void observerAppChange(Context context) {
        LogUtils.i("客户端界面进行应用变化监听");

        ObserverManager observerManager = (ObserverManager) GlobalManager.Companion.getManager(GlobalManager.OBSERVER_SERVICE);
        mNetStateObserver = observerManager.getObserver(context, ObserverManager.NET_STATE_OBSERVER_NAME);
        mNetStateObserver.addSubscriber(onNetStateChangeListener);
    }


    @Override
    public void onPresenterDetach(Context context) {
        if (mNetStateObserver != null) {
            mNetStateObserver.removeSubscriber(onNetStateChangeListener);
        }

        super.onPresenterDetach(context);
    }

    public void createString() {
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
    }

    public void fromIterable(Context context) {
        addDisposable(Observable.fromIterable(
                getInstallAppList(context)
                )
                        .map(AppInstantItemBean::getAppName)
                        .doOnError(Throwable::printStackTrace)
                        .subscribe(str -> LogUtils.d("已经安装" + str))
        );
    }

    public void flatMap() {
        addDisposable(Observable.create((ObservableOnSubscribe<Response>) e -> e.onNext(getJoke()))
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
                .subscribe(title -> getView().toast("获取到的标题为" + title)));
    }

    public void useRepository(Context context) {
        RepositoryManager repositoryManager = RepositoryManager.getProvider(context);

        repositoryManager
                .attachModule("def", "https://api-m.mtime.cn/")
                .attachModule("module", "https://ticket-api-m.mtime.cn/");

        Observable<MtimeFilmeBean> hotFilm = getHostService(repositoryManager).getHotFilm();

        Observable<FilmDetailBean> filmDetail = getModuleService(repositoryManager, ModuleClient.class)
                .getFilmDetail(235701);

//        changeObserver2List(hotFilm,false);
        changeObserver2Item(hotFilm);
    }

    private void changeObserver2Item(Observable<MtimeFilmeBean> hotFilm) {
        addDisposable(hotFilm.flatMap((Function<MtimeFilmeBean, ObservableSource<FilmItemBean>>) filmeBean -> new Observable<FilmItemBean>() {
            @Override
            protected void subscribeActual(Observer<? super FilmItemBean> observer) {
                List<FilmItemBean> ms = filmeBean.getMs();
                for (FilmItemBean filmItemBean : ms) {
                    observer.onNext(filmItemBean);
                }
            }
        }).filter(FilmItemBean::isIs3D)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        frontpageBean -> LogUtils.i("普通获取到的数据为" + frontpageBean),
                        Throwable::printStackTrace
                ));
    }

    private void changeObserver2List(Observable<MtimeFilmeBean> hotFilm, boolean program) {
        Observable<List<FilmItemBean>> map;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (program) {
                map = hotFilm.map(
                        filmeBean -> filmeBean.getMs().stream().filter(FilmItemBean::isIs3D).collect(Collectors.toList())
                );

            } else {
                map = hotFilm.flatMap(
                        (Function<MtimeFilmeBean, ObservableSource<List<FilmItemBean>>>) filmeBean -> new Observable<List<FilmItemBean>>() {
                            @Override
                            protected void subscribeActual(Observer<? super List<FilmItemBean>> observer) {
                                observer.onNext(filmeBean.getMs().stream().filter(FilmItemBean::isIs3D).collect(Collectors.toList()));
                            }
                        }
                );
            }

            addDisposable(map.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            frontpageBean -> LogUtils.i("高版本map方式获取到的数据为" + frontpageBean),
                            Throwable::printStackTrace
                    ));
        }
    }

    private HostClient getHostService(RepositoryManager repositoryManager) {
        String def = repositoryManager.getModuleUrl("def");
        Retrofit retrofit = repositoryManager.obtainRetrofit(def);
        return repositoryManager.obtainCacheService(retrofit, HostClient.class);
    }

    private <T> T getModuleService(RepositoryManager manager, Class<T> clientClass) {
        String module = manager.getModuleUrl("module");
        Retrofit retrofit = manager.obtainRetrofit(module);
        return manager.obtainCacheService(retrofit, clientClass);
    }
}
