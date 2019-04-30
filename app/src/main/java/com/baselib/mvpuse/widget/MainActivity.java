package com.baselib.mvpuse.widget;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.baselib.instant.mvp.BaseActivity;
import com.baselib.mvpuse.presenter.MainPresenter;
import com.baselib.mvpuse.view.MainView;
import com.baselib.mvpuse.R;

/**
 * 示例界面
 * <p>
 * 继承自{@link BaseActivity}并指定对应泛型,实现对应方法
 *
 * @author wsb
 */
public class MainActivity extends BaseActivity<MainPresenter, MainView> {

    private Button mBtnLogin;
    private EditText mEtAccount;
    private EditText mEtPsw;

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
    }

    @Override
    protected void initView() {
        mBtnLogin = findViewById(R.id.button);
        mEtAccount = findViewById(R.id.editText);
        mEtPsw = findViewById(R.id.editText2);
    }

    @Override
    public int getContentId() {
        return R.layout.activity_main;
    }
}
