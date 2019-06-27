package com.baselib.instant.observer.state;

/**
 * 注册状态基类
 * <p>
 * 基类本身不对注册和解注做操作，依赖扩展类去执行
 *
 * @author wsb
 */
public class BaseRegisterState {
    public boolean register(IRegisterStateOperate registerStateOperate) {
        return false;
    }

    public boolean unregister(IRegisterStateOperate registerStateOperate) {
        return false;
    }

    /**
     * 注册状态操作回调接口
     *
     * 在执行注册操作前需要做对应操作,所以把执行操作的行为用接口包裹.条件满足才执行
     *
     * @author wsb
     * */
    public interface IRegisterStateOperate {
        /**
         * 真正执行注册
         * @return true为注册成功
         * */
        boolean realRegister();

        /**
         * 真正执行解注
         * @return true为解注成功
         * */
        boolean realUnregister();
    }
}
