package com.baselib.instant.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 反射工具类
 *
 * @author wsb
 */
public class ReflectUtil {
    /**
     * 获取对象里指定变量的值
     *
     * @param instance  对象本身
     * @param fieldName 成员变量名
     * @return 成员变量的值
     * @throws IllegalAccessException 成员未设置Accessible抛出
     * @throws NoSuchFieldException   无该成员时候抛出
     */
    public static Object getValue(Object instance, String fieldName)
            throws IllegalAccessException, NoSuchFieldException {
        Field field = getField(instance.getClass(), fieldName);
        // 参数值为true，禁用访问控制检查
        field.setAccessible(true);
        return field.get(instance);
    }

    /**
     * 获取类里指定变量的field
     * <p>
     * 如果该类中公开的成员里不存在目标成员，那么往上一层，即父类寻找
     *
     * @param thisClass 字节码对象
     * @param fieldName 目标成员名
     * @return 返回指定的成员
     * @throws NoSuchFieldException 找不到时候抛出
     */
    public static Field getField(Class<?> thisClass, String fieldName)
            throws NoSuchFieldException {
        if (thisClass == null) {
            throw new NoSuchFieldException("Error field !");
        }

        try {
            return thisClass.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            return getField(thisClass.getSuperclass(), fieldName);
        }
    }

    /**
     * 获取对象里的方法
     *
     * @param instance       对象本身
     * @param methodName     目标方法名
     * @param parameterTypes 该方法的参数列表各类型
     * @return 指定方法对象
     * @throws NoSuchMethodException 找不到时候抛出
     */
    public static Method getMethod(Object instance, String methodName, Class<?>[] parameterTypes)
            throws NoSuchMethodException {
        Method accessMethod = getMethod(instance.getClass(), methodName, parameterTypes);
        //参数值为true，禁用访问控制检查
        accessMethod.setAccessible(true);

        return accessMethod;
    }

    /**
     * 获取类里的方法
     * <p>
     * 如果该类中公开的成员里不存在目标成员，那么往上一层，即父类寻找
     *
     * @param thisClass      字节码对象
     * @param methodName     目标方法名
     * @param parameterTypes 该方法的参数列表各类型
     * @return 指定方法对象
     * @throws NoSuchMethodException 找不到时候抛出
     */
    private static Method getMethod(Class<?> thisClass, String methodName, Class<?>[] parameterTypes)
            throws NoSuchMethodException {
        if (thisClass == null) {
            throw new NoSuchMethodException("Error method !");
        }

        try {
            return thisClass.getDeclaredMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            return getMethod(thisClass.getSuperclass(), methodName, parameterTypes);
        }
    }

    /**
     * 调用含单个参数的方法
     *
     * @param instance   see{@link #invokeMethod(Object, String, Object...)}
     * @param methodName see{@link #invokeMethod(Object, String, Object...)}
     * @param arg        see{@link #invokeMethod(Object, String, Object...)}
     * @return see{@link #invokeMethod(Object, String, Object...)}
     * @throws NoSuchMethodException     see{@link #invokeMethod(Object, String, Object...)}
     * @throws IllegalAccessException    see{@link #invokeMethod(Object, String, Object...)}
     * @throws InvocationTargetException see{@link #invokeMethod(Object, String, Object...)}
     */
    public static Object invokeMethod(Object instance, String methodName, Object arg)
            throws NoSuchMethodException,
            IllegalAccessException, InvocationTargetException {

        Object[] args = new Object[1];
        args[0] = arg;
        return invokeMethod(instance, methodName, args);
    }

    /**
     * 调用含多个参数的方法
     *
     * @param instance   对象本身
     * @param methodName 方法名
     * @param args       传给上述方法名的参数对象
     * @return 方法执行结果
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public static Object invokeMethod(Object instance, String methodName, Object... args)
            throws NoSuchMethodException,
            IllegalAccessException, InvocationTargetException {
        Class<?>[] parameterTypes = null;
        if (args != null) {
            parameterTypes = new Class[args.length];
            for (int i = 0; i < args.length; i++) {
                if (args[i] != null) {
                    parameterTypes[i] = args[i].getClass();
                }
            }
        }
        return getMethod(instance, methodName, parameterTypes).invoke(instance, args);
    }
}
