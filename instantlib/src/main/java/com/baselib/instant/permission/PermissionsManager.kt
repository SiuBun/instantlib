package com.baselib.instant.permission

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.PermissionChecker
import android.support.v7.app.AlertDialog
import com.baselib.instant.manager.IManager
import com.baselib.instant.util.LogUtils
import com.baselib.instant.util.SystemUtil
import java.util.*
import kotlin.collections.HashMap

/**
 * 权限管理者
 *
 * @author wsb
 */
class PermissionsManager : IManager {
    companion object {
        /**
         * 提示重新授权所用请求码,项目中其他权限请求码避开使用
         */
        private const val NOTIFY_REQ_CODE = 1002
    }

    var mCallbackMap: HashMap<Int, IPermissionsCheckCallback> = HashMap()

    /**
     * 使用功能前针对权限对象进行检测，检测后执行回调内容
     *
     * @param activity    上下文
     * @param permissions 权限内容
     * @param requestCode 请求内容
     * @param callback    回调[IPermissionsCheckCallback]
     */
    fun checkPermissions(activity: Activity, permissions: Array<String>, requestCode: Int, callback: IPermissionsCheckCallback) {
        if (NOTIFY_REQ_CODE == requestCode) {
            LogUtils.d("请不要使用该权限请求码,否则会出现权限重新请求错乱问题")
            return
        }
        mCallbackMap.put(requestCode, callback)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val permissionList = ArrayList<String>()

            //逐个判断所申请权限是否之前已经通过
            for (permission in permissions) {
                if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionList.add(permission)
                }
            }

            if (permissionList.isEmpty()) {
                checkPermissionSuccess(requestCode)
            } else {
                ActivityCompat.requestPermissions(activity, permissions, requestCode)
            }

        } else {
            checkPermissionSuccess(requestCode)
        }
    }

    /**
     * 对应activity生命周期
     *
     *
     * 请求权限回调里执行各请求码对应的响应对象回调,并将已经拒绝的权限内容返回
     *
     * @param requestCode  请求码
     * @param permissions  权限组
     * @param grantResults 授权结果
     */
    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray?) {
        val resultNull = grantResults != null && grantResults.isNotEmpty()
        //        尚未通过授权的权限,本次授权申请的内容就是该列表里的权限
        val list = ArrayList<String>()
        if (resultNull && mCallbackMap.containsKey(requestCode)) {
            for (i in grantResults!!.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    list.add(permissions[i])
                }
            }

            if (list.isEmpty()) {
                checkPermissionSuccess(requestCode)
            } else {
                val strings = list.toTypedArray()
                checkPermissionFinish(requestCode, strings)
            }

        } else {
            LogUtils.d("本次权限请求响应并非权限管理工具所发起的权限请求")
        }
    }

    /**
     * 提醒授权
     *
     * 当用户拒绝首次授权后可以使用该方法提醒用户再次授权.如果被拒绝的权限里有打勾禁止询问的,将引导至设置页要求手动授权
     *
     * @param activity            展示弹窗的上下文
     * @param permissionsBeDenied 需要重新授权的权限内容
     */
    fun notifyReqPermission(activity: Activity, permissionsBeDenied: Array<String>, callback: IPermissionsCheckCallback) {
        //        已勾选禁止,不再询问的权限将保存到该列表
        val list = ArrayList<String>()
        for (permission in permissionsBeDenied) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                list.add(permission)
            }
        }

        if (list.isEmpty()) {
            val positive = DialogInterface.OnClickListener { dialog, _ ->
                dialog.dismiss()
                mCallbackMap[NOTIFY_REQ_CODE] = callback
                ActivityCompat.requestPermissions(activity, permissionsBeDenied, NOTIFY_REQ_CODE)
            }
            val negative = DialogInterface.OnClickListener { dialog, _ -> dialog.dismiss() }
            AlertDialog.Builder(activity).setMessage("已关闭相关权限,可能影响部分功能,是否重新授权").setPositiveButton("重新授权", positive).setNegativeButton("拒绝", negative).create().show()
        } else {
            showSystemPermissionsSettingDialog(activity, permissionsBeDenied)
        }

    }

    /**
     * 提醒用户手动开启对应权限
     *
     * @param activity            上下文打开应用设置打开
     * @param permissionsBeDenied 所有被拒绝的权限(无论是否勾选了禁止询问)
     */
    private fun showSystemPermissionsSettingDialog(activity: Activity, permissionsBeDenied: Array<String>) {
        val mPackName = activity.packageName

        AlertDialog.Builder(activity)
                .setMessage("已禁用相关权限如下:\n \t " + Arrays.toString(permissionsBeDenied) + "\n 请进入应用权限页手动授予再打开应用")
                .setPositiveButton("设置") { dialog, _ ->
                    dialog.dismiss()

                    val packageURI = Uri.parse("package:$mPackName")
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    activity.startActivity(intent)
                    activity.finish()
                }
                .setNegativeButton("取消") { dialog, _ ->
                    //关闭页面或者做其他操作
                    dialog.dismiss()
                }
                .create().show()
    }

    /**
     * 请求所有目标权限成功
     *
     * @param requestCode 本次权限申请对应的请求码
     */
    private fun checkPermissionSuccess(requestCode: Int) {
        checkPermissionFinish(requestCode, null)
    }

    /**
     * 请求目标权限完成
     *
     * @param requestCode 对应的请求码
     * @param permissions 被拒绝的权限
     */
    private fun checkPermissionFinish(requestCode: Int, permissions: Array<String>?) {
        val checkCallback = mCallbackMap[requestCode]
        checkCallback?.let {
            checkCallback.onCheckedFinish(permissions)
            mCallbackMap.remove(requestCode)

        }
    }

    override fun detach() {
        mCallbackMap.clear()

    }

    /**
     * 权限检查回调
     */
    interface IPermissionsCheckCallback {
        /**
         * 权限检查完毕
         *
         * @param permissionsBeDenied 被拒绝的权限内容
         */
        fun onCheckedFinish(permissionsBeDenied: Array<String>?)
    }

    /**
     * 判断是否拥有某项权限
     *
     *
     * For Android < Android M, self permissions are always granted.
     *
     * @param context 上下文
     * @param permission 权限内容
     * @return true代表网络可用
     */
    fun selfPermissionGranted(context: Context, permission: String): Boolean {
        var result = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            result = if (SystemUtil.getTargetSdkVersion(context) >= Build.VERSION_CODES.M) {
                // targetSdkVersion >= Android M, we can
                // use Context#checkSelfPermission
                context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
            } else {
                // targetSdkVersion < Android M, we have to use
                // PermissionChecker
                PermissionChecker.checkSelfPermission(context, permission) == PermissionChecker.PERMISSION_GRANTED
            }
        }
        return result
    }

}