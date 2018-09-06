package cn.com.billboard.util;

import com.google.gson.Gson;
import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.ArrayList;
import java.util.List;

import cn.com.library.log.XLog;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class PermissionsUtil {

    public interface RequestPermission {
        /**
         * 权限请求成功
         */
        void onRequestPermissionSuccess();

        /**
         * 用户拒绝了权限请求, 权限请求失败, 但还可以继续请求该权限
         */
        void onRequestPermissionFailure();

        /**
         * 用户拒绝了权限请求并且用户选择了以后不再询问, 权限请求失败, 这时将不能继续请求该权限, 需要提示用户进入设置页面打开该权限
         */
        void onRequestPermissionFailureWithAskNeverAgain();
    }

    public static void requestPermission(RequestPermission requestPermission, RxPermissions rxPermissions, String... permissions) {
        if (permissions == null || permissions.length == 0) return;
        int permissionsNum = permissions.length;
        List<Permission> permissionList = new ArrayList<>();
        List<String> needRequest = new ArrayList<>();
        for (String permission : permissions) { //过滤调已经申请过的权限
            if (!rxPermissions.isGranted(permission)) {
                needRequest.add(permission);
            }
        }
        if (needRequest.isEmpty()) {//全部权限都已经申请过，直接执行操作
            requestPermission.onRequestPermissionSuccess();
        } else {//没有申请过,则开始申请
            rxPermissions.requestEach(permissions)
                    .subscribe(new Observer<Permission>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            XLog.e("onSubscribe==" + new Gson().toJson(d));
                        }

                        @Override
                        public void onNext(Permission value) {
                            permissionList.add(value);
                            if (permissionList.size() == permissionsNum){
                                XLog.e("permissionList === " + permissionList);
                                for (Permission p : permissionList) {
                                    if (!p.granted) {
                                        if (p.shouldShowRequestPermissionRationale) {
                                            requestPermission.onRequestPermissionFailure();
                                            return;
                                        } else {
                                            requestPermission.onRequestPermissionFailureWithAskNeverAgain();
                                            return;
                                        }
                                    }
                                }
                                requestPermission.onRequestPermissionSuccess();
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                            requestPermission.onRequestPermissionFailure();
                        }

                        @Override
                        public void onComplete() {
                            XLog.e("权限申请");
                        }
                    });
        }

    }

}
