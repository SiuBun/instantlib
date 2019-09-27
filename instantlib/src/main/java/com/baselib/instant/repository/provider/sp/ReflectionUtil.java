package com.baselib.instant.repository.provider.sp;

import android.content.ContentValues;
import android.content.SharedPreferences.Editor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Set;

/**
 * 反射工具类
 *
 * @author chenchongji
 * 2016年3月21日
 */
class ReflectionUtil {

    static ContentValues contentValuesNewInstance(HashMap<String, Object> values) {
        try {
            // hide
            Constructor<ContentValues> c = ContentValues.class.getDeclaredConstructor(new Class[]{HashMap.class});
            c.setAccessible(true);
            return c.newInstance(values);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    static Editor editorPutStringSet(Editor editor, String key, Set<String> values) {
        try {
            // Android 3.0
            Method method = editor.getClass().getDeclaredMethod("putStringSet", new Class[]{String.class, Set.class});
            return (Editor) method.invoke(editor, key, values);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    static void editorApply(Editor editor) {
        try {
            // Android 2.3
            Method method = editor.getClass().getDeclaredMethod("apply");
            method.invoke(editor);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}