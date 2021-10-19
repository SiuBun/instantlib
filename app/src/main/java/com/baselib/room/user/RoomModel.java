package com.baselib.room.user;

import android.app.Application;

import com.baselib.instant.mvvm.model.BaseMvvmModel;
import com.baselib.instant.util.LogUtils;

import java.util.List;

import androidx.lifecycle.LiveData;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

class RoomModel extends BaseMvvmModel {
    private UserRepository mRepository;

    RoomModel(Application application) {
        mRepository = new UserRepository(application);
    }

    LiveData<List<UserEntity>> getAllUser() {
        return mRepository.getAllUser();
    }

    void insertUser(String userId, String userName, String password) {

        UserEntity entity = new UserEntity();
        entity.setUserId(userId);
        entity.setUserName(userName);
        entity.setPassword(password);
        entity.setUpdateTime(System.currentTimeMillis());
        LogUtils.i("M层插入数据");
        mRepository.insertUser(entity);

    }

    @Override
    public void onModelDestroy() {
        super.onModelDestroy();
    }

    public Flowable<UserEntity> getOneUser() {
        return mRepository.getOneUser().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }
}
