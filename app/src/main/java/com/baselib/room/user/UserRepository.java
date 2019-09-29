package com.baselib.room.user;

import android.arch.lifecycle.LiveData;
import android.content.Context;

import com.baselib.instant.manager.GlobalManager;
import com.baselib.instant.executor.ThreadExecutorProxy;
import com.baselib.instant.repository.RepositoryManager;
import com.baselib.instant.util.LogUtils;

import java.util.List;

import io.reactivex.Flowable;

public class UserRepository {

    private final UserDao mUserDao;
    private ThreadExecutorProxy mThreadManager;

    UserRepository(Context context){
        mThreadManager = (ThreadExecutorProxy) GlobalManager.Companion.getManager(GlobalManager.EXECUTOR_POOL_SERVICE);

        UserDatabase db = RepositoryManager.getProvider(context).obtainDatabase(UserDatabase.class, "user_database");
        mUserDao = db.getUserDao();
    }

    LiveData<List<UserEntity>> getAllUser() {
        LogUtils.i("加载本地用户");
        return mUserDao.getAllUser();
    }

    void insertUser(UserEntity entity){
        mThreadManager.execute(() -> mUserDao.inserUser(entity)
        );
    }

    public Flowable<UserEntity> getOneUser() {
        return mUserDao.getLastUser();
    }
}
