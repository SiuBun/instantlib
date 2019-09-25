package com.baselib.room.user;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "user_table")
public class UserEntity {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "user_id")
    private String mUserId;

    @ColumnInfo(name = "user_name")
    private String mUserName;

    @ColumnInfo(name = "user_psw")
    private String mPassword;

    @ColumnInfo(name = "update_time")
    private Long mUpdateTime;

    @NonNull
    public String getUserId() {
        return mUserId;
    }

    public String getUserName() {
        return mUserName;
    }

    public String getPassword() {
        return mPassword;
    }

    public Long getUpdateTime() {
        return mUpdateTime;
    }

    public void setPassword(String mPassword) {
        this.mPassword = mPassword;
    }

    public void setUpdateTime(Long mUpdateTime) {
        this.mUpdateTime = mUpdateTime;
    }

    public void setUserId(@NonNull String mUserId) {
        this.mUserId = mUserId;
    }

    public void setUserName(String mUserName) {
        this.mUserName = mUserName;
    }

    @Ignore
    public UserEntity(@NonNull String mUserId, String mUserName, String mPassword, Long mUpdateTime) {
        this.mUserId = mUserId;
        this.mUserName = mUserName;
        this.mPassword = mPassword;
        this.mUpdateTime = mUpdateTime;
    }

    public UserEntity() {
    }

    @Override
    public String toString() {
        return "UserEntity{" +
                "mUserId='" + mUserId + '\'' +
                ", mUserName='" + mUserName + '\'' +
                ", mPassword='" + mPassword + '\'' +
                ", mUpdateTime=" + mUpdateTime +
                '}';
    }
}
