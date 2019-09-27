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

    public EditorImpl(EditImplCallback implCallback) {
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
        setValue(mImplCallback.getContext(), MpSpCons.PATH_APPLY);
    }

    @Override
    public boolean commit() {
        return setValue(mImplCallback.getContext(), MpSpCons.PATH_COMMIT);
    }

    private boolean setValue(Context context, String pathSegment) {
        boolean result = false;
        // 如果设备处在“安全模式”，返回false；
        if (!mImplCallback.editSafeMode()) {
            try {
                mImplCallback.editCheckInitAuthority(context);
            } catch (RuntimeException e) {
                // 解决崩溃：java.lang.RuntimeException: Package manager has died at android.app.ApplicationPackageManager.getPackageInfo(ApplicationPackageManager.java:77)
                result = mImplCallback.editProcessPmhdException(e, false);
            }
            String[] selectionArgs = new String[]{String.valueOf(mImplCallback.getEditMode()), String.valueOf(mClear)};
            synchronized (this) {
                Uri uri = Uri.withAppendedPath(Uri.withAppendedPath(mImplCallback.getAuthorityUrl(), mImplCallback.getEditName()), pathSegment);
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
                    result = mImplCallback.editProcessPmhdException(e, false);
                }
            }
        }
        return result;
    }


    public interface EditImplCallback {
        boolean editSafeMode();

        Context getContext();

        <T> T editProcessPmhdException(RuntimeException e, T result);

        void editCheckInitAuthority(Context context);

        int getEditMode();

        String getEditName();

        Uri getAuthorityUrl();
    }
}