package com.baselib.mvpuse.model;

import com.baselib.instant.mvp.BaseModel;

public class BreakPointModel extends BaseModel {

    private int mRecordPercentage;
    private long mContentLength;

    private int mTaskId;

    public void saveId(int taskId) {
        mTaskId = taskId;
    }

    public int getTaskId() {
        return mTaskId;
    }

    public boolean shouldUpdateView(long taskTotalSize, long length) {
        boolean update = false;
        final int percentage = getPercentage(taskTotalSize, length);
        if (this.mRecordPercentage != percentage){
            this.mRecordPercentage = percentage;
            update = true;
        }
        return update;
    }

    public int getPercentage(long taskTotalSize, long length) {
        final float progress = length / (float) taskTotalSize;
        return (int) (progress * 100);
    }

    public void saveLength(long contentLength) {

        mContentLength = contentLength;
    }

    public long getContentLength() {
        return mContentLength;
    }
}
