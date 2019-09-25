package com.baselib.room.user;

import android.arch.lifecycle.LiveData;
import android.content.Context;

import com.baselib.instant.manager.GlobalManager;
import com.baselib.instant.thread.ThreadExecutorProxy;
import com.baselib.instant.util.LogUtils;

import java.util.List;

public class UserRepository {

    private final UserDao mUserDao;
    private LiveData<List<UserEntity>> allUser;
    private ThreadExecutorProxy mThreadManager;

    UserRepository(Context context){
        mThreadManager = (ThreadExecutorProxy) GlobalManager.Companion.getManager(GlobalManager.EXECUTOR_POOL_SERVICE);
        UserDatabase db = UserDatabase.getInstance(context);
        mUserDao = db.getUserDao();
        allUser = mUserDao.getAllUser();
    }

    LiveData<List<UserEntity>> getAllUser() {
        LogUtils.i("加载本地用户");
        return allUser;
    }

    void insertUser(UserEntity entity){
        mThreadManager.execute(() -> mUserDao.inserUser(entity)
        );
    }
}
