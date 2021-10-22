package com.baselib.instant.mvp;

/**
 * Time:2021/10/22 4:29 下午
 * Author:
 * Description:
 */
public interface IPresenterOperate<P extends MvpPresenter> {
    /**
     * 初始化P层对象
     *
     * @return P层逻辑对象
     */
    P initPresenter();

    P getPresenter();


}
