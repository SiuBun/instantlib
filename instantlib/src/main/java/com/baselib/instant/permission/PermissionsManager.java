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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 权限管理者
 *
 * @author wsb
 */
public class PermissionsManager {

    private final int NOTIFY_REQ_CODE = 1002;

    private PermissionsManager() {
    }

    /**
     * 静态实例,借助静态代码块实现类加载时候进行初始化
     */
    private static PermissionsManager sManagerInstance;

    static {
        sManagerInstance = new PermissionsManager();
    }


    public static PermissionsManager getInstance() {
        return sManagerInstance;
    }

    /**
     * 存储回调对象的map，请求码对应回调对象
     */
    private Map<Integer, IPermissionsCheckCallback> mCallbackMap = new HashMap<>();

    /**
     * 不再提示权限时的展示对话框
     */
    private AlertDialog mPermissionDialog;

    /**
     * 使用功能前针对权限对象进行检测，检测后执行回调内容
     *
     * @param activity    上下文
     * @param permissions 权限内容
     * @param requestCode 请求内容
     * @param callback    回调{@link IPermissionsCheckCallback}
     */
    public void checkPermissions(Activity activity, String[] permissions, int requestCode, IPermissionsCheckCallback callback) {
        mCallbackMap.clear();
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
     *
     * @param requestCode  请求码
     * @param permissions  权限组
     * @param grantResults 授权结果
     */
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        boolean resultNull = grantResults != null && grantResults.length > 0;
        ArrayList<String> list = new ArrayList<>();

        if (resultNull&& mCallbackMap.containsKey(requestCode)) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    list.add(permissions[i]);
                }
            }

            if (list.isEmpty()){
                checkPermissionSuccess(requestCode);
            }else {
                String[] strings = list.toArray(new String[]{});
                checkPermissionFinish(requestCode, strings);
            }

        }
    }


    public void notifyReqPermission(final Activity activity, final String[] permissionsBeDenied) {
        ArrayList<String> list = new ArrayList<>();
        for (String permission:permissionsBeDenied){
            if (!ActivityCompat.shouldShowRequestPermissionRationale(activity,permission)){
                list.add(permission);
            }
        }

        if (list.isEmpty()){
            new AlertDialog.Builder(activity).setMessage("已关闭相关权限,可能影响部分功能,是否重新授权").setPositiveButton("重新授权", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    ActivityCompat.requestPermissions(activity, permissionsBeDenied, NOTIFY_REQ_CODE);
                }
            }).setNegativeButton("拒绝", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).create().show();
        }else {
            showSystemPermissionsSettingDialog(activity,permissionsBeDenied);
        }

    }

    /**
     * 提醒用户手动开启对应权限
     * @param context 上下文打开应用设置打开
     * */
    private void showSystemPermissionsSettingDialog(final Activity context, String[] permissionsBeDenied) {
        final String mPackName = context.getPackageName();

        if (mPermissionDialog == null) {
            mPermissionDialog = new AlertDialog.Builder(context)
                    .setMessage("已禁用相关权限如下:\n \t "+Arrays.toString(permissionsBeDenied)+"\n 请进入应用权限页手动授予")
                    .setPositiveButton("设置", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            cancelPermissionDialog();

                            Uri packageURI = Uri.parse("package:" + mPackName);
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
                            context.startActivity(intent);
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //关闭页面或者做其他操作
                            cancelPermissionDialog();
                        }
                    })
                    .create();
        }
        mPermissionDialog.show();
    }


    /**
     * 请求所有目标权限成功
     * */
    private void checkPermissionSuccess(int requestCode) {
        checkPermissionFinish(requestCode,null);
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

    private void cancelPermissionDialog() {
        if (mPermissionDialog != null) {
            mPermissionDialog.cancel();
            mPermissionDialog = null;
        }

    }

    /**
     * 权限检查回调
     */
    public interface IPermissionsCheckCallback {
        /**
         * 权限检查完毕
         * @param permissionsBeDenied 被拒绝的权限内容
         */
        void onCheckedFinish(String[] permissionsBeDenied);
    }
}
