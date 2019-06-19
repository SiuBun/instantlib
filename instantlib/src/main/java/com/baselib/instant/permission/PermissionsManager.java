package com.baselib.instant.permission;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.baselib.instant.Const;
import com.baselib.manager.IManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 权限管理者
 *
 * @author wsb
 */
public class PermissionsManager implements IManager {

    /**
     * 提示重新授权所用请求码,项目中其他权限请求码避开使用
     * */
    private final int NOTIFY_REQ_CODE = 1002;

//    private PermissionsManager() {
//    }
//
//    /**
//     * 静态实例,借助静态代码块实现类加载时候进行初始化
//     */
//    private static PermissionsManager sManagerInstance;
//
//    static {
//        sManagerInstance = new PermissionsManager();
//    }
//
//
//    public static PermissionsManager getInstance() {
//        return sManagerInstance;
//    }

    /**
     * 存储回调对象的map，请求码对应回调对象
     */
    private Map<Integer, IPermissionsCheckCallback> mCallbackMap = new HashMap<>();


    /**
     * 使用功能前针对权限对象进行检测，检测后执行回调内容
     *
     * @param activity    上下文
     * @param permissions 权限内容
     * @param requestCode 请求内容
     * @param callback    回调{@link IPermissionsCheckCallback}
     */
    public void checkPermissions(Activity activity, String[] permissions, int requestCode, IPermissionsCheckCallback callback) {
        if (NOTIFY_REQ_CODE == requestCode) {
            Log.d(Const.TAG, "请不要使用该权限请求码,否则会出现权限重新请求错乱问题");
            return;
        }
        mCallbackMap.put(requestCode, callback);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ArrayList<String> permissionList = new ArrayList<>();

            //逐个判断所申请权限是否之前已经通过
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionList.add(permission);
                }
            }

            if (permissionList.isEmpty()) {
                checkPermissionSuccess(requestCode);
            } else {
                ActivityCompat.requestPermissions(activity, permissions, requestCode);
            }

        } else {
            checkPermissionSuccess(requestCode);
        }
    }

    /**
     * 对应activity生命周期
     * <p>
     * 请求权限回调里执行各请求码对应的响应对象回调,并将已经拒绝的权限内容返回
     *
     * @param requestCode  请求码
     * @param permissions  权限组
     * @param grantResults 授权结果
     */
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        boolean resultNull = grantResults != null && grantResults.length > 0;
//        尚未通过授权的权限,本次授权申请的内容就是该列表里的权限
        ArrayList<String> list = new ArrayList<>();
        if (resultNull && mCallbackMap.containsKey(requestCode)) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    list.add(permissions[i]);
                }
            }

            if (list.isEmpty()) {
                checkPermissionSuccess(requestCode);
            } else {
                String[] strings = list.toArray(new String[]{});
                checkPermissionFinish(requestCode, strings);
            }

        } else {
            Log.d(Const.TAG, "本次权限请求响应并非权限管理工具所发起的权限请求");
        }
    }


    /**
     * 提醒授权
     * <p>
     * 当用户拒绝首次授权后可以使用该方法提醒用户再次授权.如果被拒绝的权限里有打勾禁止询问的,将引导至设置页要求手动授权
     *
     * @param activity            展示弹窗的上下文
     * @param permissionsBeDenied 需要重新授权的权限内容
     */
    public void notifyReqPermission(final Activity activity, final String[] permissionsBeDenied, final IPermissionsCheckCallback callback) {
//        已勾选禁止,不再询问的权限将保存到该列表
        ArrayList<String> list = new ArrayList<>();
        for (String permission : permissionsBeDenied) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                list.add(permission);
            }
        }

        if (list.isEmpty()) {
            DialogInterface.OnClickListener positive = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    mCallbackMap.put(NOTIFY_REQ_CODE, callback);
                    ActivityCompat.requestPermissions(activity, permissionsBeDenied, NOTIFY_REQ_CODE);
                }
            };
            DialogInterface.OnClickListener negative = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            };
            new AlertDialog.Builder(activity).setMessage("已关闭相关权限,可能影响部分功能,是否重新授权").setPositiveButton("重新授权", positive).setNegativeButton("拒绝", negative).create().show();
        } else {
            showSystemPermissionsSettingDialog(activity, permissionsBeDenied);
        }

    }

    /**
     * 提醒用户手动开启对应权限
     *
     * @param activity            上下文打开应用设置打开
     * @param permissionsBeDenied 所有被拒绝的权限(无论是否勾选了禁止询问)
     */
    private void showSystemPermissionsSettingDialog(final Activity activity, String[] permissionsBeDenied) {
        final String mPackName = activity.getPackageName();

            new AlertDialog.Builder(activity)
                    .setMessage("已禁用相关权限如下:\n \t " + Arrays.toString(permissionsBeDenied) + "\n 请进入应用权限页手动授予再打开应用")
                    .setPositiveButton("设置", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();

                            Uri packageURI = Uri.parse("package:" + mPackName);
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            activity.startActivity(intent);
                            activity.finish();
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //关闭页面或者做其他操作
                            dialog.dismiss();
                        }
                    })
                    .create().show();
        }

    /**
     * 请求所有目标权限成功
     *
     * @param requestCode 本次权限申请对应的请求码
     */
    private void checkPermissionSuccess(int requestCode) {
        checkPermissionFinish(requestCode, null);
    }

    /**
     * 请求目标权限完成
     *
     * @param requestCode 对应的请求码
     * @param permissions 被拒绝的权限
     */
    private void checkPermissionFinish(int requestCode, String[] permissions) {
        IPermissionsCheckCallback checkCallback = mCallbackMap.get(requestCode);
        if (checkCallback != null) {
            checkCallback.onCheckedFinish(permissions);
            mCallbackMap.remove(requestCode);
        }
    }


    @Override
    public void detach() {
        mCallbackMap.clear();
        mCallbackMap = null;

    }

    /**
     * 权限检查回调
     */
    public interface IPermissionsCheckCallback {
        /**
         * 权限检查完毕
         *
         * @param permissionsBeDenied 被拒绝的权限内容
         */
        void onCheckedFinish(String[] permissionsBeDenied);
    }
}
