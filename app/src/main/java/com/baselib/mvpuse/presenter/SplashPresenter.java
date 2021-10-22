package com.baselib.mvpuse.presenter;

import android.app.Activity;

import com.baselib.instant.manager.GlobalManager;
import com.baselib.instant.mvp.BasePresenter;
import com.baselib.instant.permission.PermissionsManager;
import com.baselib.instant.util.LogUtils;
import com.baselib.mvpuse.model.SplashModel;
import com.baselib.mvpuse.view.SplashView;

import java.util.Arrays;

public class SplashPresenter extends BasePresenter<SplashView, SplashModel> {
    @Override
    public SplashModel initModel() {
        return new SplashModel();
    }

    public String[] getPermissions() {
        return getModel().getPermissions();
    }

    public void checkPermissions(Activity activity, int reqCode) {
        final PermissionsManager manager =
            (PermissionsManager) GlobalManager.Companion.getManager(GlobalManager.PERMISSION_SERVICE);

        PermissionsManager.IPermissionsCheckCallback checkCallback =
            new PermissionsManager.IPermissionsCheckCallback() {

                @Override
                public void onCheckedFinish(String[] permissionsBeDenied) {
                    if (permissionsBeDenied == null || permissionsBeDenied.length == 0) {
                        getView().delayedToJump();
                    } else {
                        LogUtils.d("被拒绝的权限如下" + Arrays.toString(permissionsBeDenied));
                        repeatPermissionReq(activity,manager, permissionsBeDenied);
                    }
                }
            };
        manager.checkPermissions(activity, getPermissions(), reqCode, checkCallback);
    }

    private void repeatPermissionReq(Activity activity, PermissionsManager manager,
                                     String[] permissionsBeDenied) {
        PermissionsManager.IPermissionsCheckCallback checkCallback =
            new PermissionsManager.IPermissionsCheckCallback() {

                @Override
                public void onCheckedFinish(String[] permissionsBeDenied) {
                    if (permissionsBeDenied == null || permissionsBeDenied.length == 0) {
                        getView().delayedToJump();
                    }
                }
            };
        manager.notifyReqPermission(activity, permissionsBeDenied, checkCallback);
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        final PermissionsManager manager =
            (PermissionsManager) GlobalManager.Companion.getManager(GlobalManager.PERMISSION_SERVICE);
        manager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
