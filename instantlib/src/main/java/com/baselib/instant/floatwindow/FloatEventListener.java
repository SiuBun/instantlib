package com.baselib.instant.floatwindow;

/**
 * <h1>悬浮窗按钮监听</h1>
 * @author wsb
 *
 * */
public interface FloatEventListener {
     /**
      * 事件，不同的code对应悬浮窗内部不同的按钮
      *
      * @param code 按钮编号
      * */
     void eventCode(int code);
}