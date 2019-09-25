package com.baselib.room.user;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.databinding.ObservableField;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextUtils;

import com.baselib.instant.mvvm.viewmodel.BaseViewModel;
import com.baselib.instant.util.LogUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;


public class RoomViewModel extends BaseViewModel<RoomModel> {

    private ObservableField<String> mLocalData;
    private ObservableField<String> mSaveState;
    private MutableLiveData<String> mUsernameError = new MutableLiveData<>();
    private MutableLiveData<String> mPasswordError = new MutableLiveData<>();

    private LiveData<List<UserEntity>> mAllUserLiveData = new MutableLiveData<>();
    private UserRepository userRepository;


    public LiveData<String> getUserNameError() {
        return mUsernameError;
    }

    public LiveData<String> getPasswordError() {
        return mPasswordError;
    }

    public ObservableField<String> getSaveState() {
        return mSaveState;
    }

    public ObservableField<String> getLocalData() {
        return mLocalData;
    }

    public LiveData<List<UserEntity>> getAllUserLiveData() {
        return mAllUserLiveData;
    }

    public RoomViewModel(@NotNull Application application, @Nullable RoomModel model) {
        super(application, model);
        userRepository = new UserRepository(application);
        mAllUserLiveData = userRepository.getAllUser();
    }

    @Override
    public void onViewModelStart() {
        super.onViewModelStart();
        mLocalData = new ObservableField<>("暂无");
        mSaveState = new ObservableField<>("save");
    }

    public RoomViewModel(@NotNull Application application) {
        this(application, null);
    }

    public static ViewModelProvider.Factory getFactory(@NotNull Application application) {
        return new ViewModelProvider.Factory() {

            @Override
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                T instance;
                try {
                    instance = modelClass.getConstructor(Application.class, RoomModel.class).newInstance(application, new RoomModel(application));
                } catch (Exception e) {
                    e.printStackTrace();
                    instance = null;
                }
                return instance;
            }
        };
    }

    public void addUser(Editable userId, Editable userName, Editable password) {
        mSaveState.set("正在保存用户资料");
        if (TextUtils.isEmpty(userName)) {
            mUsernameError.postValue("用户名不能为空");
            mSaveState.set("条件不满足");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            mPasswordError.postValue("密码不能为空");
            mSaveState.set("条件不满足");
            return;
        }
        if (TextUtils.isEmpty(userId)) {
            mPasswordError.postValue("id不能为空");
            mSaveState.set("条件不满足");
            return;
        }

        mSaveState.set("保存用户资料");

        UserEntity entity = new UserEntity();
        entity.setUserId(userId.toString());
        entity.setUserName(userName.toString());
        entity.setPassword(password.toString());
        entity.setUpdateTime(System.currentTimeMillis());

        userRepository.insertUser(entity);


    }
}
