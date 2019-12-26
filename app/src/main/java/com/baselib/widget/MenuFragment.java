package com.baselib.widget;

import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.baselib.instant.breakpoint.BreakPointHelper;
import com.baselib.instant.breakpoint.Task;
import com.baselib.instant.breakpoint.TaskPostListener;
import com.baselib.instant.mvp.BaseFragment;
import com.baselib.instant.util.LogUtils;
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
                    final Task task = new Task.Builder()
                            .setTaskUrl("https://qd.myapp.com/myapp/qqteam/AndroidQQ/mobileqq_android.apk")
//                            .setTaskFileName("downtask.apk")
                            .setTaskFileDir(getContext().getFilesDir().getAbsolutePath())
                            .build();

                    final TaskPostListener taskListener = new TaskPostListener() {

                        @Override
                        public void postNewTaskSuccess(int taskId) {
                            mTaskId = taskId;
                            LogUtils.i("任务添加成功,等待下载,id为" + task);
                        }

                        @Override
                        public void onTaskDownloadError(String message) {
                            LogUtils.e(message);
                        }

                        @Override
                        public void onTaskProgressUpdate(long taskTotalSize, long length) {
//                            LogUtils.i("任务下载进度更新,文件大小为" + taskTotalSize + ",已下载" + length);
                        }

                        @Override
                        public void onTaskDownloadFinish() {
                            LogUtils.i("任务下载完成");
                        }

                        @Override
                        public void onTaskCancel() {
                            LogUtils.i("任务取消");
                        }

                        @Override
                        public void onTaskDownloadStart(String downloadUrl) {
                            LogUtils.i("下载开始,文件来源:" + downloadUrl);
                        }
                    };

                    task.addTaskListener(taskListener);
                    BreakPointHelper.getInstance().postTask(getContext(), task);

//                    LogUtils.i("移除任务结果"+ BreakPointHelper.getInstance().removeTask(mTaskId));

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
