package com.baselib.widget;

import com.baselib.instant.mvp.BaseActivity;
import com.baselib.mvpuse.presenter.MainPresenter;
import com.baselib.mvpuse.view.MainView;
import com.baselib.use.R;

import androidx.fragment.app.Fragment;

/**
 * 示例界面
 * <p>
 * 继承自{@link BaseActivity}并指定对应泛型,实现对应方法
 *
 * @author wsb
 */
public class MainActivity extends BaseActivity<MainPresenter> implements MainView {

    private Fragment mFragmentMain;

    @Override
    protected void initData() {
    }

    @Override
    protected MainPresenter iniPresenter() {
        return new MainPresenter();
    }

    @Override
    protected void initListener() {
    }

    @Override
    protected void initView() {
        getSupportFragmentManager()
            .beginTransaction()
            .add(R.id.flt_main_root, new MenuFragment())
            .commit();
    }

    @Override
    public int getContentId() {
        return R.layout.activity_main;
    }

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            List<Fragment> fragments = getSupportFragmentManager().getFragments();
//            if (fragments.size() == 1) {
//                LogUtils.d("当前只剩下一个挂载的fragment,拦截退栈为home键");
//                //实现只在冷启动时显示启动页，即点击返回键与点击HOME键退出效果一致
//                Intent intent = new Intent(Intent.ACTION_MAIN);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                intent.addCategory(Intent.CATEGORY_HOME);
//                startActivity(intent);
//            }
//            return true;
//        }
//        return super.onKeyDown(keyCode, event);
//
//    }
}
