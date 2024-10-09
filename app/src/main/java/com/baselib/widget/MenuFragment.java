package com.baselib.widget;

import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;

import com.baselib.bussiness.ByteObtain;
import com.baselib.instant.mvp.BaseFragment;
import com.baselib.instant.util.LogUtils;
import com.baselib.mvpuse.presenter.MenuPresenter;
import com.baselib.mvpuse.view.MenuFragView;
import com.baselib.use.R;
import com.baselib.use.databinding.FragmentMenuBinding;

import java.util.Arrays;

public class MenuFragment extends BaseFragment<MenuPresenter> implements MenuFragView {

    private Button mBtnRx;
    private Button mBtnOther;
    private Button mBtnNetData;
    private Button mBtnBpDownload;
    private Button mBtnMvvm;
    private Button mBtnRoom;
    private Button mBtnRepository;
    private FragmentMenuBinding menuBinding;

    @Override
    public int getFragmentLayout() {
        return R.layout.fragment_menu;
    }

    @Override
    public MenuPresenter initPresenter() {
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

        String url = "http://www.baidu.com";
        LogUtils.d("start req byte from url: " + url);
        byte[] bytes = ByteObtain.getInstance().getByteFromUrl(url);
        LogUtils.d("resp byte from url: " + Arrays.toString(bytes));
    }

    private int mTaskId;

    @Override
    protected void initFragmentViews(View fragmentView) {

        menuBinding = FragmentMenuBinding.bind(fragmentView);
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
        mBtnRx.setOnClickListener(v -> getPresenter().createString());

        mBtnOther.setOnClickListener(v -> getPresenter().fromIterable(v.getContext()));

        mBtnNetData.setOnClickListener(v -> getPresenter().flatMap());

        mBtnBpDownload.setOnClickListener(v -> {
                    startFragmentByClz(R.id.flt_main_root, BreakPointFragment.getInstance());
                }
        );

//        mBtnMvvm.setOnClickListener(v -> startActivity(MvvmTestActivity.class));

//        mBtnRoom.setOnClickListener(v -> startFragmentByClz(R.id.flt_main_root, RoomFragment.getInstance()));

        mBtnRepository.setOnClickListener(v -> getPresenter().useRepository(getContext()));

//        menuBinding.btnVp2.setOnClickListener(v -> startFragmentByClz(R.id.flt_main_root, MultiVp2Fragment.getInstance()));
    }

    @Override
    public void widgetDestroy() {
        super.widgetDestroy();
    }
}
