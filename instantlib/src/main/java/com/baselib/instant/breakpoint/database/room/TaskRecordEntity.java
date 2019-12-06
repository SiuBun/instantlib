package com.baselib.instant.breakpoint.database.room;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

/**
 * room数据库条目实体,不作为下载任务使用
 *
 * @author wsb
 */
@Entity(tableName = "download_task_record_table")
public
class TaskRecordEntity {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "task_id")
    private int id;

    @ColumnInfo(name = "task_state")
    private int state;

    @ColumnInfo(name = "task_url")
    private String mUrl;
    
    @ColumnInfo(name = "task_dir")
    private String mFileDir;
    
    @ColumnInfo(name = "task_name")
    private String mFileName;

    @ColumnInfo(name = "total_size")
    private Long mTotalSize;

    @ColumnInfo(name = "current_size")
    private Long mCurrentSize;

    @ColumnInfo(name = "update_time")
    private Long mUpdateTime;

    @NonNull
    public int getId() {
        return id;
    }

    public void setId(@NonNull int id) {
        this.id = id;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public Long getTotalSize() {
        return mTotalSize;
    }

    public void setTotalSize(Long mTotalSize) {
        this.mTotalSize = mTotalSize;
    }

    public Long getCurrentSize() {
        return mCurrentSize;
    }

    public void setCurrentSize(Long mCurrentSize) {
        this.mCurrentSize = mCurrentSize;
    }

    public Long getUpdateTime() {
        return mUpdateTime;
    }

    public void setUpdateTime(Long mUpdateTime) {
        this.mUpdateTime = mUpdateTime;
    }


    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String mUrl) {
        this.mUrl = mUrl;
    }

    public String getFileDir() {
        return mFileDir;
    }

    public void setFileDir(String mFileDir) {
        this.mFileDir = mFileDir;
    }

    public String getFileName() {
        return mFileName;
    }

    public void setFileName(String mFileName) {
        this.mFileName = mFileName;
    }

    @Override
    public String toString() {
        return "TaskRecordEntity{" +
                "id='" + id + '\'' +
                ", state=" + state +
                ", mUrl='" + mUrl + '\'' +
                ", mFileDir='" + mFileDir + '\'' +
                ", mFileName='" + mFileName + '\'' +
                ", mTotalSize=" + mTotalSize +
                ", mCurrentSize=" + mCurrentSize +
                ", mUpdateTime=" + mUpdateTime +
                '}';
    }
}
