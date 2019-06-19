package com.baselib.mvpuse.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
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
import com.baselib.instant.mvp.BaseActivity;
import com.baselib.mvpuse.R;
import com.baselib.mvpuse.presenter.MainPresenter;
import com.baselib.mvpuse.view.MainView;

/**
 * 示例界面
 * <p>
 * 继承自{@link BaseActivity}并指定对应泛型,实现对应方法
 *
 * @author wsb
 */
public class MainActivity extends BaseActivity<MainPresenter, MainView> {

    private Button mBtnLogin;
    private Button mBtnLogout;
    private EditText mEtAccount;
    private EditText mEtPsw;
    private LinearLayout mLltLoginContainer;
    private ImageView mIvLogo;
    private FloatButtonController mFloatButtonController;

    @Override
    protected void initData() {
        iniAnim();
        mFloatButtonController = (FloatButtonController) GlobalManager.getManager(GlobalManager.FLOAT_WINDOWS_SERVICE);
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
        WindowManager manager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
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

    @Override
    protected MainView getViewImpl() {
        return new MainView() {

            @Override
            public void controlProgress(boolean show) {
                controlProgressBar(show);
            }

            @Override
            public void loginResult(boolean result) {
                Toast.makeText(MainActivity.this, result ? "成功" : "失败", Toast.LENGTH_SHORT).show();
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
    protected MainPresenter iniPresenter() {
        return new MainPresenter();
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

    @Override
    protected void initView() {
        mIvLogo = findViewById(R.id.iv_logo);
        mLltLoginContainer = findViewById(R.id.llt_login_container);
        mBtnLogin = findViewById(R.id.button_login);
        mBtnLogout = findViewById(R.id.button_logout);
        mEtAccount = findViewById(R.id.editText);
        mEtPsw = findViewById(R.id.editText2);


    }

    @Override
    public int getContentId() {
        return R.layout.activity_main;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //实现只在冷启动时显示启动页，即点击返回键与点击HOME键退出效果一致
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
