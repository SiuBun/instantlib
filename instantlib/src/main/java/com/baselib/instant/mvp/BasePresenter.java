package com.baselib.instant.mvp;

import android.content.Context;

/**
 * P层基类
 * <p>
 * 项目中presenter相关类都要继承自该类，所有业务层逻辑都在该类及扩展类处理.子类继承该类时候需要指定M层对象泛型便于完成M层对象的初始化
 *
 * @author wsb
 */
public abstract class BasePresenter<V extends IBaseView, M extends BaseModel> {
    /**
     * 内部所持有的控件操作对象
     * */
    private V mView;
    /**
     * 内部所持有的数据操作对象
     * */
    private M mModel;

    public BasePresenter() {
        mModel = initModel();
    }

    /**
     * 初始化M层对象,M层类型根据所传泛型决定
     *
     * @return M层对象
     */
    public abstract M initModel();

    /**
     * 同步V层生命周期onCreate,创建时候进行view层对象挂载
     */
    public void attach(V view) {
        mView = view;
    }


    /**
     * 同步V层生命周期onDestroy,销毁时候进行view层对象解除挂载
     */
    public void detach(Context context) {
        mView = null;
        mModel.detach(context);
        mModel = null;
    }


    /**
     * 对外提供的获取M层对象方法
     * @return M层对象
     * */
    public M getModel() {
        return mModel;
    }

    /**
     * 对外提供的获取V层对象方法
     * @return V层对象
     * */
    public V getView() {
        return mView;
    }
}
