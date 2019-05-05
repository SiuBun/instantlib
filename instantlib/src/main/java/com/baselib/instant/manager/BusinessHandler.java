package com.baselib.instant.manager;


import android.os.Handler;
import android.os.Looper;
import android.os.Message;
/**
 * 事务处理类
 * <p>
 * 项目中基类实例化,故项目中维护一个Handler类,剩余扩展交给其他{@link IHandlerMsgListener}处理
 *
 * @author wsb
 */
public class BusinessHandler extends Handler {
    /**
     * 所持有的消息处理对象
     * */
    private IHandlerMsgListener mListener;

    public BusinessHandler(Looper looper, IHandlerMsgListener listener) {
        super(looper);
        this.mListener = listener;
    }

    @Override
    public void handleMessage(Message msg) {
        if (mListener!=null){
            mListener.handleMessage(msg);
        }
    }

    /**
     * 销毁阶段回调
     * */
    public void onDestroy() {
        if (mListener!=null){
            mListener = null;
        }
        removeCallbacksAndMessages(null);
    }

    /**
     * handler消息处理器回调
     * */
    public interface IHandlerMsgListener{
        /**
         * 处理消息
         * @param msg 收到的消息对象
         * */
        void handleMessage(Message msg);
    }
}
