package com.baselib.instant.repository.provider.sp;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * 编辑器实现
 *
 * @author chenchongji
 * 2016年3月21日
 */
public final class EditorImpl implements SharedPreferences.Editor {
    private final HashMap<String, Object> mModified = new HashMap<>();
    private boolean mClear = false;
    private EditImplCallback mImplCallback;

    EditorImpl(EditImplCallback implCallback) {
        mImplCallback = implCallback;
    }

    @Override
    public SharedPreferences.Editor putString(String key, String value) {
        synchronized (this) {
            mModified.put(key, value);
            return this;
        }
    }

    /**
     * Android 3.0
     */
    @Override
    public SharedPreferences.Editor putStringSet(String key, Set<String> values) {
        synchronized (this) {
            mModified.put(key, (values == null) ? null : new HashSet<>(values));
            return this;
        }
    }

    @Override
    public SharedPreferences.Editor putInt(String key, int value) {
        synchronized (this) {
            mModified.put(key, value);
            return this;
        }
    }

    @Override
    public SharedPreferences.Editor putLong(String key, long value) {
        synchronized (this) {
            mModified.put(key, value);
            return this;
        }
    }

    @Override
    public SharedPreferences.Editor putFloat(String key, float value) {
        synchronized (this) {
            mModified.put(key, value);
            return this;
        }
    }

    @Override
    public SharedPreferences.Editor putBoolean(String key, boolean value) {
        synchronized (this) {
            mModified.put(key, value);
            return this;
        }
    }

    @Override
    public SharedPreferences.Editor remove(String key) {
        synchronized (this) {
            mModified.put(key, null);
            return this;
        }
    }

    @Override
    public SharedPreferences.Editor clear() {
        synchronized (this) {
            mClear = true;
            return this;
        }
    }

    @Override
    public void apply() {
        setValue(mImplCallback.getContext(), MpSpConst.PATH_APPLY);
    }

    @Override
    public boolean commit() {
        return setValue(mImplCallback.getContext(), MpSpConst.PATH_COMMIT);
    }

    private boolean setValue(Context context, String pathSegment) {
        boolean result = false;
        // 如果设备处在“安全模式”，返回false；
        if (!mImplCallback.confirmSafeMode2Edit()) {
            try {
                mImplCallback.invokeCheckInitAuthority(context);
            } catch (RuntimeException e) {
                // 解决崩溃：java.lang.RuntimeException: Package manager has died at android.app.ApplicationPackageManager.getPackageInfo(ApplicationPackageManager.java:77)
                result = mImplCallback.handleProcessPmhdException(e, false);
            }
            String[] selectionArgs = new String[]{String.valueOf(mImplCallback.getAccessMode()), String.valueOf(mClear)};
            synchronized (this) {
                Uri uri = Uri.withAppendedPath(Uri.withAppendedPath(mImplCallback.getAuthorityUrl(), mImplCallback.getEditFileName()), pathSegment);
                ContentValues values = ReflectionUtil.contentValuesNewInstance(mModified);
                try {
                    result = context.getContentResolver().update(uri, values, null, selectionArgs) > 0;
                } catch (IllegalArgumentException e) {
                    // 解决ContentProvider所在进程被杀时的抛出的异常：java.lang.IllegalArgumentException: Unknown URI content://xxx.xxx.xxx/xxx/xxx at android.content.ContentResolver.update(ContentResolver.java:1312)
//                    if (DEBUG) {
                    e.printStackTrace();
//                    }
                } catch (RuntimeException e) {
                    // 解决崩溃：java.lang.RuntimeException: Package manager has died at android.app.ApplicationPackageManager.resolveContentProvider(ApplicationPackageManager.java:609) ... at android.content.ContentResolver.update(ContentResolver.java:1310)
                    result = mImplCallback.handleProcessPmhdException(e, false);
                }
            }
        }
        return result;
    }


    /**
     * 编辑器回调接口
     *
     * @author wsb
     */
    public interface EditImplCallback {
        /**
         * 确认当前设备是否在安全模式后告知编辑器对象
         *
         * @return true代表安全模式下
         */
        boolean confirmSafeMode2Edit();

        /**
         * 获取上下文
         *
         * @return 操作所需上下文
         */
        Context getContext();

        /**
         * 回调对象处理java.lang.RuntimeException: Package manager has died的错误
         *
         * @param e      异常信息
         * @param result 期望返回内容
         * @return 最终处理结果
         */
        <T> T handleProcessPmhdException(RuntimeException e, T result);

        /**
         * 要求回调对象执行authority初始化的行为
         *
         * @param context 用于初始化的上下文
         */
        void invokeCheckInitAuthority(Context context);

        /**
         * 获取回调对象的sp文件的访问模式
         *
         * @return 访问模式
         */
        int getAccessMode();

        /**
         * 获取回调对象的sp文件名
         *
         * @return sp文件名
         */
        String getEditFileName();

        /**
         * 获取内容提供者url
         *
         * @return 协议url
         */
        Uri getAuthorityUrl();
    }
}