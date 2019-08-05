package com.baselib.instant.provider.sp;

import android.content.Context;
import android.content.UriMatcher;
import android.net.Uri;
import android.os.Bundle;

import java.util.HashMap;

public class UriDispatcher {

    public interface UriDispatcherCallback {

        Context getContext();

        UriMatcher getUriMatcher();

        Uri getUri();

        String[] getSelectionArgs();

        HashMap<String, Integer> getListenersCountNoNull();
    }

    static Bundle getBundleByUri(UriDispatcher.UriDispatcherCallback dispatcherCallback) {
        Bundle bundle = new Bundle();
        Context context = dispatcherCallback.getContext();
        Uri uri = dispatcherCallback.getUri();
        String name = uri.getPathSegments().get(0);
        String[] selectionArgs = dispatcherCallback.getSelectionArgs();
        int mode = Integer.parseInt(selectionArgs[0]);
        String key = selectionArgs[1];
        String defValue = selectionArgs[2];

        switch (dispatcherCallback.getUriMatcher().match(uri)) {
            case MpSpCons.GET_ALL:
                UriDispatcher.putAll(context, name, mode, bundle);
                break;
            case MpSpCons.GET_STRING:
                UriDispatcher.putString(context, name, mode, key, defValue, bundle);
                break;
            case MpSpCons.GET_INT:
                UriDispatcher.putInt(context, name, mode, key, defValue, bundle);
                break;
            case MpSpCons.GET_LONG:
                UriDispatcher.putLong(context, name, mode, key, defValue, bundle);
                break;
            case MpSpCons.GET_FLOAT:
                UriDispatcher.putFloat(context, name, mode, key, defValue, bundle);
                break;
            case MpSpCons.GET_BOOLEAN:
                UriDispatcher.putBoolean(context, name, mode, key, defValue, bundle);
                break;
            case MpSpCons.CONTAINS:
                UriDispatcher.putContains(context, name, mode, key, bundle);
                break;
            case MpSpCons.REGISTER_ON_SHARED_PREFERENCE_CHANGE_LISTENER:
                UriDispatcher.registerOnSpChange(bundle, dispatcherCallback);
                break;
            case MpSpCons.UNREGISTER_ON_SHARED_PREFERENCE_CHANGE_LISTENER:
                UriDispatcher.unregisterOnSpChange(bundle, dispatcherCallback);
                break;
            default:
                throw new IllegalArgumentException("This is Unknown Uri：" + uri);
        }
        return bundle;
    }

    static void putAll(Context context, String name, int mode, Bundle bundle) {
        bundle.putSerializable(MpSpCons.KEY, (HashMap<String, ?>) context.getSharedPreferences(name, mode).getAll());
    }

    static void putString(Context context, String name, int mode, String key, String defValue, Bundle bundle) {
        bundle.putString(MpSpCons.KEY, context.getSharedPreferences(name, mode).getString(key, defValue));
    }

    static void putInt(Context context, String name, int mode, String key, String defValue, Bundle bundle) {
        bundle.putInt(MpSpCons.KEY, context.getSharedPreferences(name, mode).getInt(key, Integer.parseInt(defValue)));
    }

    static void putLong(Context context, String name, int mode, String key, String defValue, Bundle bundle) {
        bundle.putLong(MpSpCons.KEY, context.getSharedPreferences(name, mode).getLong(key, Long.parseLong(defValue)));
    }

    static void putFloat(Context context, String name, int mode, String key, String defValue, Bundle bundle) {
        bundle.putFloat(MpSpCons.KEY, context.getSharedPreferences(name, mode).getFloat(key, Float.parseFloat(defValue)));
    }

    static void putBoolean(Context context, String name, int mode, String key, String defValue, Bundle bundle) {
        bundle.putBoolean(MpSpCons.KEY, context.getSharedPreferences(name, mode).getBoolean(key, Boolean.parseBoolean(defValue)));
    }

    static void putContains(Context context, String name, int mode, String key, Bundle bundle) {
        bundle.putBoolean(MpSpCons.KEY, context.getSharedPreferences(name, mode).contains(key));
    }

    static void registerOnSpChange(Bundle bundle, UriDispatcherCallback dispatcherCallback) {
        String name = dispatcherCallback.getUri().getPathSegments().get(0);

        Integer countInteger = dispatcherCallback.getListenersCountNoNull().get(name);
        int count = (countInteger == null ? 0 : countInteger) + 1;
        dispatcherCallback.getListenersCountNoNull().put(name, count);
        countInteger = dispatcherCallback.getListenersCountNoNull().get(name);
        bundle.putBoolean(MpSpCons.KEY, count == (countInteger == null ? 0 : countInteger));
    }

    static void unregisterOnSpChange(Bundle bundle, UriDispatcherCallback dispatcherCallback) {
        String name = dispatcherCallback.getUri().getPathSegments().get(0);
        Integer countInteger = dispatcherCallback.getListenersCountNoNull().get(name);
        int count = (countInteger == null ? 0 : countInteger) - 1;
        if (count <= 0) {
            dispatcherCallback.getListenersCountNoNull().remove(name);
            bundle.putBoolean(MpSpCons.KEY, !dispatcherCallback.getListenersCountNoNull().containsKey(name));
        } else {
            dispatcherCallback.getListenersCountNoNull().put(name, count);
            countInteger = dispatcherCallback.getListenersCountNoNull().get(name);
            bundle.putBoolean(MpSpCons.KEY, count == (countInteger == null ? 0 : countInteger));
        }
    }
}
