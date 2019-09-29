package com.baselib.widget;

import android.arch.lifecycle.ViewModelProviders;
import android.support.v7.widget.LinearLayoutManager;

import com.baselib.instant.mvvm.view.AbsMvvmFragment;
import com.baselib.instant.util.LogUtils;
import com.baselib.room.user.RoomViewModel;
import com.baselib.room.user.UserAdapter;
import com.baselib.use.R;
import com.baselib.use.databinding.LayoutFragmentRoomBinding;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;


public class RoomFragment extends AbsMvvmFragment<LayoutFragmentRoomBinding, RoomViewModel> {

    private UserAdapter userAdapter;

    @NotNull
    @Override
    public RoomViewModel initViewModel() {
        return ViewModelProviders.of(this, RoomViewModel.getFactory(getActivity().getApplication())).get(RoomViewModel.class);
    }

    public static RoomFragment getInstance() {
        return new RoomFragment();
    }

    @Override
    public int getContentLayout() {
        return R.layout.layout_fragment_room;
    }

    @Override
    public void initObserverAndData() {
        super.initObserverAndData();
        Objects.requireNonNull(getViewModel());
        dataBinding.setRoomVm(getViewModel());

        userAdapter = new UserAdapter(getContext());
        dataBinding.rcvUser.setAdapter(userAdapter);
        dataBinding.rcvUser.setLayoutManager(new LinearLayoutManager(getContext()));
        dataBinding.btnSave.setOnClickListener(v -> getViewModel().addUser(dataBinding.tietUserid.getText(), dataBinding.tietUsername.getText(), dataBinding.tietPassword.getText()));


        getViewModel().getUserNameError().observe(this, s -> dataBinding.tietUsername.setError(s));
        getViewModel().getPasswordError().observe(this, s -> dataBinding.tietPassword.setError(s));
        getViewModel().getShowUserLiveData().observe(this, userEntities -> {
                    if (null != userEntities) {
                        LogUtils.i("更新的用户列表为" + userEntities.toString());
                        userAdapter.setUsers(userEntities);
                    }
                }
        );

    }

    @Override
    public void lazyLoadData() {
        super.lazyLoadData();
        Objects.requireNonNull(getViewModel());
        getViewModel().loadLocalUser();
        showContentView();
    }

}
