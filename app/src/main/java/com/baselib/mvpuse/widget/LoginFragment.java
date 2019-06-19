package com.baselib.mvpuse.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.baselib.instant.floatwindow.AbstractShowMode;
import com.baselib.instant.floatwindow.FloatButtonController;
import com.baselib.instant.manager.GlobalManager;
import com.baselib.instant.mvp.BaseFragment;
import com.baselib.mvpuse.R;
import com.baselib.mvpuse.model.LoginFragModel;
import com.baselib.mvpuse.presenter.LoginFragPresenter;
import com.baselib.mvpuse.view.LoginFragView;

public class LoginFragment extends BaseFragment<LoginFragPresenter, LoginFragView> {
    private Button mBtnLogin;
    private Button mBtnLogout;
    private EditText mEtAccount;
    private EditText mEtPsw;
    private LinearLayout mLltLoginContainer;
    private ImageView mIvLogo;
    private FloatButtonController mFloatButtonController;

    @Override
    public int getFragmentLayout() {
        return R.layout.fragment_main_login;
    }

    @Override
    protected LoginFragView getViewImpl() {
        return new LoginFragView(){

            @Override
            public void controlProgress(boolean show) {
                controlProgressBar(show);
            }

            @Override
            public void loginResult(boolean result) {
                Toast.makeText(getActivity(), result ? "成功" : "失败", Toast.LENGTH_SHORT).show();
                if (result) {
                    AbstractShowMode showType = FloatButtonController.getShowType(false);
                    mFloatButtonController
                            .setShowType(showType)
                            .showFloatButton(getActivity());
                }
            }
        };
    }

    @Override
    protected LoginFragPresenter initPresenter() {
        return new LoginFragPresenter();
    }

    @Override
    protected void initData() {
        iniAnim();
        mFloatButtonController = (FloatButtonController) GlobalManager.getManager(GlobalManager.FLOAT_WINDOWS_SERVICE);
    }

    @Override
    protected void initFragmentViews(View fragmentView) {
        mIvLogo = findViewById(R.id.iv_logo,ImageView.class);
        mLltLoginContainer = findViewById(R.id.llt_login_container,LinearLayout.class);
        mBtnLogin = findViewById(R.id.button_login,Button.class);
        mBtnLogout = findViewById(R.id.button_logout,Button.class);
        mEtAccount = findViewById(R.id.editText,EditText.class);
        mEtPsw = findViewById(R.id.editText2,EditText.class);
    }

    @Override
    protected void initListener() {
        mBtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String account = mEtAccount.getText().toString();
                final String pws = mEtPsw.getText().toString();

                controlProgressBar(true);
                getPresenter().doLogin(account, pws);
            }
        });

        mBtnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFloatButtonController.closeFloatButton();
            }
        });
    }

    private void iniAnim() {
        //以控件自身所在的位置为原点，从下方距离原点200像素的位置移动到原点
        ObjectAnimator tranLogin = ObjectAnimator.ofFloat(mLltLoginContainer, "translationY", 200, 0);
        //将注册、登录的控件alpha属性从0变到1
        ObjectAnimator alphaLogin = ObjectAnimator.ofFloat(mLltLoginContainer, "alpha", 0, 1);
        final AnimatorSet bottomAnim = new AnimatorSet();
        bottomAnim.setDuration(1000);
        //同时执行控件平移和alpha渐变动画
        bottomAnim.playTogether(tranLogin, alphaLogin);

        //获取屏幕高度
        WindowManager manager = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(metrics);
        int screenHeight = metrics.heightPixels;


        //通过测量，获取ivLogo的高度
        mIvLogo.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        int logoHeight = mIvLogo.getMeasuredHeight();


        //初始化ivLogo的移动和缩放动画
        float transY = (screenHeight - logoHeight) * 0.1f;
        //ivLogo向上移动 transY 的距离
        ObjectAnimator tranLogo = ObjectAnimator.ofFloat(mIvLogo, "translationY", 0, -transY);
        //ivLogo在X轴和Y轴上都缩放0.75倍
        ObjectAnimator scaleXLogo = ObjectAnimator.ofFloat(mIvLogo, "scaleX", 1f, 0.75f);
        ObjectAnimator scaleYLogo = ObjectAnimator.ofFloat(mIvLogo, "scaleY", 1f, 0.75f);

        AnimatorSet logoAnim = new AnimatorSet();
        logoAnim.setDuration(1000);
        logoAnim.playTogether(tranLogo, scaleXLogo, scaleYLogo);

        logoAnim.start();
        logoAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                //待ivLogo的动画结束后,开始播放底部注册、登录按钮的动画
                bottomAnim.start();
            }
        });

    }
}
