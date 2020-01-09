package com.baselib.widget;

import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.baselib.instant.mvp.BaseFragment;
import com.baselib.mvpuse.presenter.MenuPresenter;
import com.baselib.mvpuse.view.MenuFragView;
import com.baselib.mvvmuse.view.activity.MvvmTestActivity;
import com.baselib.use.R;

public class MenuFragment extends BaseFragment<MenuPresenter, MenuFragView> {

    private Button mBtnRx;
    private Button mBtnOther;
    private Button mBtnNetData;
    private Button mBtnBpDownload;
    private Button mBtnMvvm;
    private Button mBtnRoom;
    private Button mBtnRepository;

    @Override
    public int getFragmentLayout() {
        return R.layout.fragment_menu;
    }

    @Override
    protected MenuFragView getViewImpl() {
        return new MenuFragView() {
            @Override
            public void toast(String content) {
                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), content, Toast.LENGTH_SHORT).show());
            }
        };
    }

    @Override
    protected MenuPresenter initPresenter() {
        return new MenuPresenter();
    }

    @Override
    protected void initData() {
//        final Task task = new Task.Builder().setTaskUrl("http://ucan.25pp.com/Wandoujia_web_seo_baidu_homepage.apk").setTaskFileDir(getContext().getCacheDir().getAbsolutePath()).setTaskFileName("666.apk").build();
//        task.addTaskListener(new TaskPostListener() {
//            @Override
//            public void postNewTaskSuccess(int taskId) {
//
//            }
//
//            @Override
//            public void onTaskDownloadError(String message) {
//                LogUtils.i("默认任务下载出错");
//            }
//
//            @Override
//            public void onTaskProgressUpdate(long taskTotalSize, long length) {
//
//            }
//
//            @Override
//            public void onTaskDownloadFinish() {
//                LogUtils.i("默认任务下载完成");
//            }
//
//            @Override
//            public void onTaskCancel() {
//
//            }
//
//            @Override
//            public void onTaskDownloadStart(String downloadUrl) {
//
//            }
//        });
//        BreakPointHelper.getInstance().postTask(getContext(), task);

    }

    private int mTaskId;

    @Override
    protected void initFragmentViews(View fragmentView) {

        mBtnRx = findViewById(R.id.btn_rx, Button.class);
        mBtnOther = findViewById(R.id.btn_get_apps, Button.class);
        mBtnNetData = findViewById(R.id.btn_net_data, Button.class);
        mBtnBpDownload = findViewById(R.id.btn_bp_download, Button.class);
        mBtnMvvm = findViewById(R.id.btn_mvvm, Button.class);
        mBtnRoom = findViewById(R.id.btn_room, Button.class);
        mBtnRepository = findViewById(R.id.btn_rx_repository, Button.class);
    }

    @Override
    protected void initListener() {
        getPresenter().observerAppChange(getContext());

        mBtnRx.setOnClickListener(v -> getPresenter().createString());

        mBtnOther.setOnClickListener(v -> getPresenter().fromIterable(v.getContext()));

        mBtnNetData.setOnClickListener(v -> getPresenter().flatMap());

        mBtnBpDownload.setOnClickListener(v -> {
                    startFragmentByClz(R.id.flt_main_root, BreakPointFragment.getInstance());
                }
        );

        mBtnMvvm.setOnClickListener(v -> startActivity(MvvmTestActivity.class));

        mBtnRoom.setOnClickListener(v -> startFragmentByClz(R.id.flt_main_root, RoomFragment.getInstance()));

        mBtnRepository.setOnClickListener(v -> getPresenter().useRepository(getContext()));
    }

    @Override
    public void widgetDestroy() {
        super.widgetDestroy();
    }
}
