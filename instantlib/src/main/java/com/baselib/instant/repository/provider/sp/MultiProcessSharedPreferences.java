/*
 * 创建日期：2014年9月12日 下午0:0:02
 */
package com.baselib.instant.repository.provider.sp;

import android.content.BroadcastReceiver;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 使用ContentProvider实现多进程SharedPreferences读写;<br>
 * 1、ContentProvider天生支持多进程访问；<br>
 * 2、使用内部私有BroadcastReceiver实现多进程OnSharedPreferenceChangeListener监听；<br>
 * <p>
 * 使用方法：AndroidManifest.xml中添加provider申明：<br>
 * <pre>
 * &lt;provider android:name="com.android.zgj.utils.MultiProcessSharedPreferences"
 * android:authorities="com.android.zgj.MultiProcessSharedPreferences"
 * android:process="com.android.zgj.MultiProcessSharedPreferences"
 * android:exported="false" /&gt;
 * &lt;!-- authorities属性里面最好使用包名做前缀，apk在安装时authorities同名的provider需要校验签名，否则无法安装；--!/&gt;<br>
 * </pre>
 * <p>
 * ContentProvider方式实现要注意：<br>
 * 1、当ContentProvider所在进程android.os.Process.killProcess(pid)时，会导致整个应用程序完全意外退出或者ContentProvider所在进程重启；<br>
 * 重启报错信息：Acquiring provider <processName> for user 0: existing object's process dead；<br>
 * 2、如果设备处在“安全模式”下，只有系统自带的ContentProvider才能被正常解析使用，因此put值时默认返回false，get值时默认返回null；<br>
 * <p>
 * 其他方式实现SharedPreferences的问题：<br>
 * 使用FileLock和FileObserver也可以实现多进程SharedPreferences读写，但是会有兼容性问题：<br>
 * 1、某些设备上卸载程序时锁文件无法删除导致卸载残留，进而导致无法重新安装该程序（报INSTALL_FAILED_UID_CHANGED错误）；<br>
 * 2、某些设备上FileLock会导致僵尸进程出现进而导致耗电；<br>
 * 3、僵尸进程出现后，正常进程的FileLock会一直阻塞等待僵尸进程中的FileLock释放，导致进程一直阻塞；<br>
 * <p>
 * added by matt
 * --------------------
 * 系统SharePreferences不能支持到多进程，原因如下：
 * After testing a lot with MODE_MULTI_PROCESS, I've three trials to share:
 * 1- Initialize the SharedPreferences once in each process and use it multiple times.
 * The problem: The values are not reflected in each process as expected. So each process has its own value of the SharedPreferences.
 * 2- Initialize the SharedPreferences in each put or get.
 * This actually works and the value now is interchangeable between processes.
 * The problem: sometimes after aggressively accessing the sharedpref, the shared preferences file got deleted with all its content, as described in this issue, and I get this warning in the log:
 * W/FileUtils﹕ Failed to chmod(/data/data/com.hegazy.multiprocesssharedpref/shared_prefs/myprefs.xml): android.system.ErrnoException: chmod failed: ENOENT (No such file or directory)
 * <p>
 * <p>
 * added by matt==需要在AndroidManifest.xml里配置Provider
 * ---------------------
 * authorities配置规则：包名.commerce.MultiProcessSharedPreferences
 * android:process配置规则：所在进程不应调用android.os.Process.killProcess(pid)
 * <provider
 * android:name="com.jb.ga0.commerce.util.MultiProcessSharedPreferences"
 * android:authorities="com.example.matt.test.commerce.MultiProcessSharedPreferences"
 * android:exported="false"
 * android:process=":test2"
 * />
 *
 * @author zhangguojun
 * @version 1.0
 * @since JDK1.6
 * https://github.com/seven456/MultiProcessSharedPreferences
 */
public class MultiProcessSharedPreferences extends ContentProvider implements SharedPreferences, EditorImpl.EditImplCallback {
    private static final String TAG = "MultiProcessSP";
    public static boolean DEBUG = false;
    private Context mAppContext;
    /**
     * sp文件名
     */
    private String mName;

    /**
     * sp访问模式
     */
    private int mMode;
    /**
     * 当前设备是否在安全模式
     * <p>
     * true代表安全模式
     */
    private boolean mSafeMode;

    /**
     * 占位符
     * */
    private static final Object CONTENT = new Object();

    /**
     * sp改变监听对象
     */
    private HashMap<OnSharedPreferenceChangeListener, Object> mSpChangeListeners;

    private BroadcastReceiver mReceiver;
    /**
     * 写在清单列表中的provider的authorities值
     */
    private static String sAuthoriry;

    /**
     * 拼接清单列表中的provider的authorities值后的uri地址
     */
    private static volatile Uri sAuthorityUrl;

    private UriMatcher mUriMatcher;

    private HashMap<String, Integer> mListenersCount;

    /**
     * mode不使用{@link Context#MODE_MULTI_PROCESS} 也可以支持多进程了；
     *
     * @param mode sp模式，参考如下
     * @see Context#MODE_PRIVATE
     * @see Context#MODE_WORLD_READABLE
     * @see Context#MODE_WORLD_WRITEABLE
     */
    public static SharedPreferences getSharedPreferences(Context context, String name, int mode) {
        return new MultiProcessSharedPreferences(context, name, mode);
    }

    /**
     * @deprecated 此默认构造函数只用于父类ContentProvider在初始化时使用；
     */
    @Deprecated
    public MultiProcessSharedPreferences() {
    }

    private MultiProcessSharedPreferences(Context context, String name, int mode) {
        mAppContext = context.getApplicationContext();
        mName = name;
        mMode = mode;
        mSafeMode = MpspUtils.isSafeMode(mAppContext);
    }

    /**
     * 检查并防止{@link #sAuthorityUrl}为null的情况
     *
     * @param context 用于获取当前应用内关于该内容提供者的信息
     */
    public void checkInitAuthority(Context context) {
        if (sAuthorityUrl == null) {
            synchronized (MultiProcessSharedPreferences.this) {
                if (sAuthorityUrl == null) {
                    PackageInfo packageInfos = null;
                    try {
                        // MpspUtils#isPkgManagerDied需要额外处理的异常：java.lang.RuntimeException: Package manager has died at android.app.ApplicationPackageManager.getPackageInfo(ApplicationPackageManager.java:77)
                        packageInfos = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_PROVIDERS);
                    } catch (PackageManager.NameNotFoundException e) {
                        if (DEBUG) {
                            e.printStackTrace();
                        }
                    }
                    if (packageInfos != null && packageInfos.providers != null) {
                        for (ProviderInfo providerInfo : packageInfos.providers) {
                            if (providerInfo.name.equals(MultiProcessSharedPreferences.class.getName())) {
                                sAuthoriry = providerInfo.authority;
                                break;
                            }
                        }
                    }
                    if (sAuthoriry == null) {
                        throw new IllegalArgumentException("'AUTHORITY' initialize failed, Unable to find explicit provider class " + MultiProcessSharedPreferences.class.getName() + "; have you declared this provider in your AndroidManifest.xml?");
                    }
                    sAuthorityUrl = Uri.parse(ContentResolver.SCHEME_CONTENT + "://" + sAuthoriry);
                    if (DEBUG) {
                        Log.d(TAG, "checkInitAuthority.AUTHORITY = " + sAuthoriry);
                    }
                }
            }
        }
    }

    @Override
    public int getAccessMode() {
        return this.mMode;
    }

    @Override
    public String getEditFileName() {
        return mName;
    }

    @Override
    public Uri getAuthorityUrl() {
        return sAuthorityUrl;
    }

    @Override
    public boolean confirmSafeMode2Edit() {
        return mSafeMode;
    }

    @Override
    public void invokeCheckInitAuthority(Context context) {
        checkInitAuthority(context);
    }

    @Override
    public <T> T handleProcessPmhdException(RuntimeException e, T result) {
        // java.lang.RuntimeException: Package manager has died at android.app.ApplicationPackageManager.getPackageInfo(ApplicationPackageManager.java:80) ... Caused by: android.os.DeadObjectException at android.os.BinderProxy.transact(Native Method) at android.content.pm.IPackageManager$Stub$Proxy.getPackageInfo(IPackageManager.java:1374)
        return MpspUtils.processPmhdException(e, result);
    }


    @SuppressWarnings("unchecked")
    @Override
    public Map<String, ?> getAll() {
        Map<String, ?> v = (Map<String, ?>) getValue(mAppContext, MpSpConst.PATH_GET_ALL, null, null);
        return v != null ? v : new HashMap<>();
    }

    @Override
    public String getString(String key, String defValue) {
        return (String) getValue(mAppContext, MpSpConst.PATH_GET_STRING, key, defValue);
    }

    /**
     * Android 3.0
     */
    @Override
    public Set<String> getStringSet(String key, Set<String> defValues) {
        return (Set<String>) getValue(mAppContext, MpSpConst.PATH_GET_STRING, key, defValues);
    }

    @Override
    public int getInt(String key, int defValue) {
        return (Integer) getValue(mAppContext, MpSpConst.PATH_GET_INT, key, defValue);
    }

    @Override
    public long getLong(String key, long defValue) {
        return (Long) getValue(mAppContext, MpSpConst.PATH_GET_LONG, key, defValue);
    }

    @Override
    public float getFloat(String key, float defValue) {
        return (Float) getValue(mAppContext, MpSpConst.PATH_GET_FLOAT, key, defValue);
    }

    @Override
    public boolean getBoolean(String key, boolean defValue) {
        return (Boolean) getValue(mAppContext, MpSpConst.PATH_GET_BOOLEAN, key, defValue);
    }

    @Override
    public boolean contains(String key) {
        try {
            Object result = getValue(mAppContext, MpSpConst.PATH_CONTAINS, key, null);
            return Boolean.TRUE.equals(result);
        } catch (Throwable e) {
            //IGNORED
        }
        return false;
    }

    @Override
    public Editor edit() {
        return new EditorImpl(MultiProcessSharedPreferences.this);
    }

    @Override
    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        synchronized (this) {
            if (mSpChangeListeners == null) {
                mSpChangeListeners = new HashMap<>();
            }
            Boolean result = (Boolean) getValue(mAppContext, MpSpConst.PATH_REGISTER_ON_SHARED_PREFERENCE_CHANGE_LISTENER, MpSpConst.SP_CHANGE_LISTENER_KEY, false);
            if (Boolean.TRUE.equals(result)) {
                mSpChangeListeners.put(listener, CONTENT);
                if (mReceiver == null) {
                    mReceiver = new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {
                            String name = intent.getStringExtra(MpSpConst.KEY_NAME);
                            @SuppressWarnings("unchecked")
                            List<String> keysModified = (List<String>) intent.getSerializableExtra(MpSpConst.KEY);
                            if (mName.equals(name) && keysModified != null) {
                                Set<OnSharedPreferenceChangeListener> listeners = new HashSet<>(mSpChangeListeners.keySet());
                                for (int i = keysModified.size() - 1; i >= 0; i--) {
                                    final String key = keysModified.get(i);
                                    for (OnSharedPreferenceChangeListener listener : listeners) {
                                        if (listener != null) {
                                            listener.onSharedPreferenceChanged(MultiProcessSharedPreferences.this, key);
                                        }
                                    }
                                }
                            }
                        }
                    };
                    mAppContext.registerReceiver(mReceiver, new IntentFilter(makeAction(mName)));
                }
            }
        }
    }

    @Override
    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        synchronized (this) {
            // WeakHashMap
            getValue(mAppContext, MpSpConst.PATH_UNREGISTER_ON_SHARED_PREFERENCE_CHANGE_LISTENER, MpSpConst.SP_CHANGE_LISTENER_KEY, false);
            if (mSpChangeListeners != null) {
                mSpChangeListeners.remove(listener);
                if (mSpChangeListeners.isEmpty() && mReceiver != null) {
                    mAppContext.unregisterReceiver(mReceiver);
                }
            }
        }
    }


    /**
     * 从文件中获取对应key的值,提供默认值用于查找失败时候返回
     * @param context 上下文
     * @param key 保存数据的键
     * @param defValue 键对应的值查找失败时候的默认返回
     * */
    private Object getValue(Context context, String pathSegment, String key, Object defValue) {
        Object v = null;
        // 如果设备处在“安全模式”，返回defValue；
        if (mSafeMode) {
            v = defValue;
        } else {
            try {
                checkInitAuthority(context);
            } catch (RuntimeException e) {
                // 解决崩溃：java.lang.RuntimeException: Package manager has died at android.app.ApplicationPackageManager.getPackageInfo(ApplicationPackageManager.java:77)
                MpspUtils.processPmhdException(e, defValue);
            }
            Uri uri = Uri.withAppendedPath(Uri.withAppendedPath(sAuthorityUrl, mName), pathSegment);
            String[] selectionArgs = new String[]{String.valueOf(mMode), key, defValue == null ? null : String.valueOf(defValue)};
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(uri, null, null, selectionArgs, null);
            } catch (SecurityException e) {
                // 解决崩溃：java.lang.SecurityException: Permission Denial: reading com.qihoo.storager.MultiProcessSharedPreferences uri content://com.qihoo.appstore.MultiProcessSharedPreferences/LogUtils/getBoolean from pid=2446, uid=10116 requires the provider be exported, or grantUriPermission() at android.content.ContentProvider$Transport.enforceReadPermission(ContentProvider.java:332) ... at android.content.ContentResolver.query(ContentResolver.java:317)
                if (DEBUG) {
                    e.printStackTrace();
                }
            } catch (RuntimeException e) {
                // 解决崩溃：java.lang.RuntimeException: Package manager has died at android.app.ApplicationPackageManager.resolveContentProvider(ApplicationPackageManager.java:609) ... at android.content.ContentResolver.query(ContentResolver.java:404)
                MpspUtils.processPmhdException(e, defValue);
            }
            if (cursor != null) {
                Bundle bundle = null;
                try {
                    bundle = cursor.getExtras();
                } catch (RuntimeException e) {
                    // 解决ContentProvider所在进程被杀时的抛出的异常：java.lang.RuntimeException: android.os.DeadObjectException at android.database.BulkCursorToCursorAdaptor.getExtras(BulkCursorToCursorAdaptor.java:173) at android.database.CursorWrapper.getExtras(CursorWrapper.java:94)
                    if (DEBUG) {
                        e.printStackTrace();
                    }
                }
                if (bundle != null) {
                    v = bundle.get(MpSpConst.KEY);
                    bundle.clear();
                }
                cursor.close();
            }
            v = v != null ? v : defValue;
        }
        return v;
    }

    private String makeAction(String name) {
        return String.format("%1$s_%2$s", MultiProcessSharedPreferences.class.getName(), name);
    }

    @Override
    public void attachInfo(Context context, ProviderInfo info) {
        if (mAppContext == null) {
            mAppContext = context.getApplicationContext();
        }
        super.attachInfo(context, info);
    }

    @Override
    public boolean onCreate() {
        try {
            checkInitAuthority(mAppContext);
        } catch (RuntimeException e) {
            // 解决崩溃：java.lang.RuntimeException: Package manager has died at android.app.ApplicationPackageManager.getPackageInfo(ApplicationPackageManager.java:77)
            sAuthoriry = MpspUtils.processPmhdException(e, "");
        }
        mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        mUriMatcher.addURI(sAuthoriry, MpSpConst.PATH_WILDCARD + MpSpConst.PATH_GET_ALL, MpSpConst.GET_ALL);
        mUriMatcher.addURI(sAuthoriry, MpSpConst.PATH_WILDCARD + MpSpConst.PATH_GET_STRING, MpSpConst.GET_STRING);
        mUriMatcher.addURI(sAuthoriry, MpSpConst.PATH_WILDCARD + MpSpConst.PATH_GET_INT, MpSpConst.GET_INT);
        mUriMatcher.addURI(sAuthoriry, MpSpConst.PATH_WILDCARD + MpSpConst.PATH_GET_LONG, MpSpConst.GET_LONG);
        mUriMatcher.addURI(sAuthoriry, MpSpConst.PATH_WILDCARD + MpSpConst.PATH_GET_FLOAT, MpSpConst.GET_FLOAT);
        mUriMatcher.addURI(sAuthoriry, MpSpConst.PATH_WILDCARD + MpSpConst.PATH_GET_BOOLEAN, MpSpConst.GET_BOOLEAN);
        mUriMatcher.addURI(sAuthoriry, MpSpConst.PATH_WILDCARD + MpSpConst.PATH_CONTAINS, MpSpConst.CONTAINS);
        mUriMatcher.addURI(sAuthoriry, MpSpConst.PATH_WILDCARD + MpSpConst.PATH_APPLY, MpSpConst.APPLY);
        mUriMatcher.addURI(sAuthoriry, MpSpConst.PATH_WILDCARD + MpSpConst.PATH_COMMIT, MpSpConst.COMMIT);
        mUriMatcher.addURI(sAuthoriry, MpSpConst.PATH_WILDCARD + MpSpConst.PATH_REGISTER_ON_SHARED_PREFERENCE_CHANGE_LISTENER, MpSpConst.REGISTER_ON_SHARED_PREFERENCE_CHANGE_LISTENER);
        mUriMatcher.addURI(sAuthoriry, MpSpConst.PATH_WILDCARD + MpSpConst.PATH_UNREGISTER_ON_SHARED_PREFERENCE_CHANGE_LISTENER, MpSpConst.UNREGISTER_ON_SHARED_PREFERENCE_CHANGE_LISTENER);
        return true;
    }

    @Override
    public Cursor query(@NotNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        UriDispatcher.UriDispatcherCallback dispatcherCallback = new UriDispatcher.UriDispatcherCallback() {

            @Override
            public Context getContext() {
                return mAppContext;
            }

            @Override
            public UriMatcher getUriMatcher() {
                return mUriMatcher;
            }

            @Override
            public Uri getUri() {
                return uri;
            }

            @Override
            public String[] getSelectionArgs() {
                return selectionArgs;
            }

            @Override
            public HashMap<String, Integer> getListenersCountNoNull() {
                checkInitListenersCount();
                return mListenersCount;
            }
        };
        return new BundleCursor(UriDispatcher.getBundleByUri(dispatcherCallback));
    }

    @Override
    public String getType(@NotNull Uri uri) {
        throw new UnsupportedOperationException("No external call");
    }

    @Override
    public Uri insert(@NotNull Uri uri, ContentValues values) {
        throw new UnsupportedOperationException("No external insert");
    }

    @Override
    public int delete(@NotNull Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("No external delete");
    }

    @Override
    public int update(@NotNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int result = 0;
        String name = uri.getPathSegments().get(0);
        int mode = Integer.parseInt(selectionArgs[0]);
        SharedPreferences preferences = mAppContext.getSharedPreferences(name, mode);
        int match = mUriMatcher.match(uri);
        switch (match) {
            case MpSpConst.APPLY:
            case MpSpConst.COMMIT:
                boolean hasListeners = mListenersCount != null && mListenersCount.get(name) != null && mListenersCount.get(name) > 0;
                ArrayList<String> keysModified = null;
                Map<String, Object> map = null;
                if (hasListeners) {
                    keysModified = new ArrayList<>();
                    map = (Map<String, Object>) preferences.getAll();
                }
                Editor editor = preferences.edit();
                boolean clear = Boolean.parseBoolean(selectionArgs[1]);
                if (clear) {
                    if (hasListeners && !map.isEmpty()) {
                        for (Map.Entry<String, Object> entry : map.entrySet()) {
                            keysModified.add(entry.getKey());
                        }
                    }
                    editor.clear();
                }
                for (Map.Entry<String, Object> entry : values.valueSet()) {
                    String k = entry.getKey();
                    Object v = entry.getValue();
                    // Android 5.L_preview : "this" is the magic value for a removal mutation. In addition,
                    // setting a value to "null" for a given key is specified to be
                    // equivalent to calling remove on that key.
                    if (v instanceof EditorImpl || v == null) {
                        editor.remove(k);
                        if (hasListeners && map.containsKey(k)) {
                            keysModified.add(k);
                        }
                    } else {
                        if (hasListeners && (!map.containsKey(k) || (map.containsKey(k) && !v.equals(map.get(k))))) {
                            keysModified.add(k);
                        }
                    }

                    if (v instanceof String) {
                        editor.putString(k, (String) v);
                    } else if (v instanceof Set) {
                        // Android 3.0
                        ReflectionUtil.editorPutStringSet(editor, k, (Set<String>) v);
                    } else if (v instanceof Integer) {
                        editor.putInt(k, (Integer) v);
                    } else if (v instanceof Long) {
                        editor.putLong(k, (Long) v);
                    } else if (v instanceof Float) {
                        editor.putFloat(k, (Float) v);
                    } else if (v instanceof Boolean) {
                        editor.putBoolean(k, (Boolean) v);
                    }
                }
                if (hasListeners && keysModified.isEmpty()) {
                    result = 1;
                } else {
                    switch (match) {
                        case MpSpConst.APPLY:
                            // Android 2.3
                            ReflectionUtil.editorApply(editor);
                            result = 1;
                            // Okay to notify the listeners before it's hit disk
                            // because the listeners should always get the same
                            // SharedPreferences instance back, which has the
                            // changes reflected in memory.
                            notifyListeners(name, keysModified);
                            break;
                        case MpSpConst.COMMIT:
                            if (editor.commit()) {
                                result = 1;
                                notifyListeners(name, keysModified);
                            }
                            break;
                        default:
                            break;
                    }
                }
                values.clear();
                break;
            default:
                throw new IllegalArgumentException("This is Unknown Uri：" + uri);
        }
        return result;
    }

    private void checkInitListenersCount() {
        if (mListenersCount == null) {
            mListenersCount = new HashMap<>();
        }
    }

    private void notifyListeners(String name, ArrayList<String> keysModified) {
        if (keysModified != null && !keysModified.isEmpty()) {
            Intent intent = new Intent();
            intent.setAction(makeAction(name));
            intent.setPackage(mAppContext.getPackageName());
            intent.putExtra(MpSpConst.KEY_NAME, name);
            intent.putExtra(MpSpConst.KEY, keysModified);
            mAppContext.sendBroadcast(intent);
        }
    }


}