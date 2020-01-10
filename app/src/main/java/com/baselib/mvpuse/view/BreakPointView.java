package com.baselib.mvpuse.view;

import com.baselib.instant.mvp.IBaseView;

import java.io.File;

public interface BreakPointView extends IBaseView {
    void onTaskDownloadStart(String downloadUrl, long contentLength);

    void onTaskCancel();

    void onTaskPause();

    void onTaskDownloadError(String message);

    void onTaskProgressUpdate(long taskTotalSize, long length, int percentage);

    void onTaskDownloadFinish(File taskFile);


    void setTextContent(String content);

    void changeProgress(int percentage);
}
