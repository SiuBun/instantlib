package com.baselib.widget;

import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baselib.instant.mvp.BaseFragment;
import com.baselib.instant.util.DataCheck;
import com.baselib.instant.util.LogUtils;
import com.baselib.mvpuse.presenter.BreakPointPresenter;
import com.baselib.mvpuse.view.BreakPointView;
import com.baselib.use.R;

import java.io.File;
import java.util.Locale;


public class BreakPointFragment extends BaseFragment<BreakPointPresenter, BreakPointView> {


    private TextView mTvTaskContent;
    private ProgressBar mPgbCommit;
    private Button mBtnStart;
    private Button mBtnPause;
    private Button mBtnCancel;

    @Override
    public int getFragmentLayout() {
        return R.layout.layout_fragment_bread_point;
    }

    @Override
    protected BreakPointView getViewImpl() {
        return new BreakPointView() {


            @Override
            public void setTextContent(String content) {
                mTvTaskContent.setText(content);
            }


            @Override
            public void onTaskDownloadError(String message) {
                LogUtils.e(message);
            }

            @Override
            public void onTaskProgressUpdate(long taskTotalSize, long length, int percentage) {
                showDownloadProgress(length, taskTotalSize);
                changeProgress(percentage);
            }

            @Override
            public void onTaskDownloadFinish(File taskFile) {
                Toast.makeText(getActivity(), "文件下载成功并保存为" + taskFile, Toast.LENGTH_LONG).show();
                changeProgress(100);

                mBtnPause.setEnabled(false);
                mBtnCancel.setEnabled(false);
            }

            @Override
            public void onTaskCancel() {
                changeProgress(0);
                showDownloadProgress(0, getPresenter().getLength());
                mBtnStart.setEnabled(true);
                mBtnPause.setEnabled(false);
                mBtnCancel.setEnabled(false);

                Toast.makeText(getActivity(), "任务取消", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onTaskDownloadStart(String downloadUrl, long contentLength) {
                getPresenter().saveLength(contentLength);
                mBtnStart.setEnabled(false);
                mBtnPause.setEnabled(true);
                mBtnCancel.setEnabled(true);

                showDownloadProgress(0, contentLength);
            }

            @Override
            public void onTaskPause() {
                mBtnStart.setEnabled(true);
                mBtnPause.setEnabled(false);
                mBtnCancel.setEnabled(true);
            }


            @Override
            public void changeProgress(int percentage) {
                mPgbCommit.setProgress(percentage);
            }

        };
    }

    @Override
    protected BreakPointPresenter initPresenter() {
        return new BreakPointPresenter();
    }


    @Override
    protected void initData() {


    }

    @Override
    protected void initFragmentViews(View fragmentView) {
        mTvTaskContent = findViewById(R.id.tv_commit, TextView.class);
        mPgbCommit = findViewById(R.id.progress_bar_commit, ProgressBar.class);
        mBtnStart = findViewById(R.id.btn_start, Button.class);
        mBtnPause = findViewById(R.id.btn_pause, Button.class);
        mBtnCancel = findViewById(R.id.btn_cancel, Button.class);
    }

    @Override
    protected void initListener() {

        mBtnStart.setOnClickListener(v -> DataCheck.checkNoNullWithCallback(getActivity(), activity -> getPresenter().startTask(activity)));

        mBtnPause.setOnClickListener(v -> getPresenter().pauseTask());

        mBtnCancel.setOnClickListener(v -> getPresenter().cancelTask());

    }

    private void showDownloadProgress(long length, long contentLength) {
        mTvTaskContent.setText(String.format(Locale.CHINESE, "%1d/%2d", length, contentLength));
    }

    public static BreakPointFragment getInstance() {
        return new BreakPointFragment();
    }
}
