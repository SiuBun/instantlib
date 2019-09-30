package com.baselib.widget;

import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.baselib.instant.mvp.BaseFragment;
import com.baselib.mvpuse.presenter.MenuPresenter;
import com.baselib.mvpuse.view.MenuFragView;
import com.baselib.mvvmuse.view.activity.MvvmTestActivity;
import com.baselib.use.R;

public class MenuFragment extends BaseFragment<MenuPresenter, MenuFragView> {

    private Button mBtnRx;
    private Button mBtnOther;
    //    private FloatButtonController mFloatButtonController;
    private Button mBtnNetData;
    private Button mBtnRetrofit;
    private Button mBtnMvvm;
    private Button mBtnRoom;
    private Button mBtnRepository;

    @Override
    public int getFragmentLayout() {
        return R.layout.fragment_menu;
    }

    @Override
    protected MenuFragView getViewImpl() {
        return new MenuFragView() {
            @Override
            public void toast(String content) {
                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), content,Toast.LENGTH_SHORT).show());
            }
        };
    }

    @Override
    protected MenuPresenter initPresenter() {
        return new MenuPresenter();
    }

    @Override
    protected void initData() {
//        mFloatButtonController = (FloatButtonController) GlobalManager.Companion.getManager(GlobalManager.FLOAT_WINDOWS_SERVICE);

//        mFloatButtonController
//                .setShowType(FloatButtonController.getShowType(false))
//                .showFloatButton(getActivity());
    }

    @Override
    protected void initFragmentViews(View fragmentView) {

        mBtnRx = findViewById(R.id.btn_rx, Button.class);
        mBtnOther = findViewById(R.id.btn_get_apps, Button.class);
        mBtnNetData = findViewById(R.id.btn_net_data, Button.class);
        mBtnRetrofit = findViewById(R.id.btn_retrofit, Button.class);
        mBtnMvvm = findViewById(R.id.btn_mvvm, Button.class);
        mBtnRoom = findViewById(R.id.btn_room, Button.class);
        mBtnRepository = findViewById(R.id.btn_rx_repository, Button.class);
    }

    @Override
    protected void initListener() {
        getPresenter().observerAppChange(getContext());

        mBtnRx.setOnClickListener(v -> getPresenter().createString());

        mBtnOther.setOnClickListener(v -> getPresenter().fromIterable(v.getContext()));

        mBtnNetData.setOnClickListener(v -> getPresenter().flatMap());

        mBtnRetrofit.setOnClickListener(v -> {

        });

        mBtnMvvm.setOnClickListener(v -> startActivity(MvvmTestActivity.class));

        mBtnRoom.setOnClickListener(v -> startFragmentByClz(R.id.flt_main_root, RoomFragment.getInstance()));


        mBtnRepository.setOnClickListener(v -> getPresenter().useRepository(getContext()));
    }

    @Override
    public void widgetDestory() {
//        mFloatButtonController.closeFloatButton();
        super.widgetDestory();
    }
}
