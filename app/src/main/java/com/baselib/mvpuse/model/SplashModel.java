package com.baselib.mvpuse.model;

import android.Manifest;

import com.baselib.instant.mvp.BaseModel;

public class SplashModel extends BaseModel {
    private String[] mPermissions = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CALL_PHONE
    };

    public String[] getPermissions() {
        return mPermissions;
    }
}
