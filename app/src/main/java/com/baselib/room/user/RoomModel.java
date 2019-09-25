package com.baselib.room.user;

import android.app.Application;
import android.arch.lifecycle.LiveData;

import com.baselib.instant.manager.GlobalManager;
import com.baselib.instant.mvvm.model.BaseMvvmModel;
import com.baselib.instant.thread.ThreadExecutorProxy;
import com.baselib.instant.util.LogUtils;

import java.util.List;

class RoomModel extends BaseMvvmModel {
    private UserRepository mRepository;


    public RoomModel(Application application) {
        mRepository = new UserRepository(application);
    }

    public LiveData<List<UserEntity>> getAllUser() {
        return mRepository.getAllUser();
    }

    /*public String getAllUserValue(LiveData<List<UserEntity>> mAllUserLiveData) {
        if (null!=mAllUserLiveData.getValue()){
            Transformations.map(mAllUserLiveData, input -> {
                StringBuilder builder = new StringBuilder();
                for (UserEntity userEntity : input) {
                    builder.append(",");
                    builder.append(userEntity.getUserName());
                }

                String localData = builder.deleteCharAt(0).toString();
                LogUtils.i("加载内容为: " + localData);
                return localData;
            });
        }else{
            return null;
        }
    }*/

    public void insertUser(String userName, String password) {

        UserEntity entity = new UserEntity();
        entity.setUserId("10086");
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
}
