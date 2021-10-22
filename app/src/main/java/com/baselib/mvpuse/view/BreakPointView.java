package com.baselib.mvpuse.view;

import com.baselib.instant.mvp.IMvpView;

import java.io.File;

public interface BreakPointView extends IMvpView {
    void onTaskDownloadStart(String downloadUrl, long contentLength);

    void onTaskCancel();

    void onTaskPause();

    void onTaskDownloadError(String message);

    void onTaskProgressUpdate(long taskTotalSize, long length, int percentage);

    void onTaskDownloadFinish(File taskFile);


    void setTextContent(String content);

    void changeProgress(int percentage);
}
