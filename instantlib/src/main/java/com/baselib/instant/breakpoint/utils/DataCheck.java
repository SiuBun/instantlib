package com.baselib.instant.breakpoint.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.baselib.instant.breakpoint.database.room.TaskRecordEntity;

import java.util.Collection;
import java.util.List;

/**
 * 数据检测类
 * <p>
 * 提供方法减少不必要模板代码
 *
 * @author wsb
 */
public class DataCheck {
    /**
     * 判断对象是否为空.如果是空对象那么将检查回调的结果赋值给这个对象
     *
     * @param data        源数据
     * @param checkResult 检查结果回调
     */
    public static <T> T checkNullWithAssignValue(@Nullable T data, IReturnValueCallback<T> checkResult) {
        if (checkNull(data)) {
            data = checkResult.assignValue();
        }
        return data;
    }

    /**
     * 检测不为空时候执行回调操作
     *
     * @param data 数据对象
     */
    public static <T> void checkNoNullWithCallback(@Nullable T data, ICheckCallback<T> checkResult) {
        takeIf(!checkNull(data), (result)-> checkResult.conformExpect(data));
    }

    /**
     * 判断对象是否为空.
     *
     * @param data 源数据
     */
    private static <T> boolean checkNull(@Nullable T data) {
        return null == data;
    }


    /**
     * 当表达式结果满足的时候,执行相关操作
     *
     * @param result      表达式结果
     * @param checkResult 满足状态下的相关操作
     */
    public static void takeIf(boolean result, ICheckCallback checkResult) {
        if (result) {
            checkResult.conformExpect(true);
        }
    }

    /**
     * 获取对象的属性值
     *
     * @param data     对象本身
     * @param defValue 对象为空时候的默认值
     * @param callback 取值操作
     * @return 对象属性值或者默认值
     */
    public static <T, R> R getField(@Nullable T data, @NonNull R defValue, IReturnValueCallback<R> callback) {
        R result;
        if (checkNull(data)) {
            result = defValue;
        } else {
            result = callback.assignValue();
        }
        return result;
    }

    /**
     * 判断集合是否为空
     * <p>
     * 含空集合判断
     *
     * @return true代表空集合或空对象
     */
    public static boolean checkEmpty(@Nullable Collection collection) {
        return checkNull(collection) || collection.isEmpty();
    }

    public static <T extends Collection>void checkNoEmptyWithCallback(T collection,ICheckCallback<T> checkCallback) {
        if (!checkEmpty(collection)){
            checkCallback.conformExpect(collection);
        }
    }


    /**
     * 工具方法检查结果回调
     *
     * @author wsb
     */
    public interface IReturnValueCallback<T> {
        /**
         * 检测符合预期时候回调
         *
         * @return 将检测结果返回
         */
        T assignValue();
    }

    public interface ICheckCallback<T> {
        /**
         * 检测符合预期时候回调
         * <p>
         * 该方法内做符合预期判断下的操作
         *
         * @param data 用于判断的数据
         */
        void conformExpect(T data);
    }
}
