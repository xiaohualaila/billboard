package cn.com.billboard.present;

import android.Manifest;
import com.blankj.utilcode.util.PermissionUtils;
import com.tbruyelle.rxpermissions2.RxPermissions;
import cn.com.billboard.model.BaseBean;
import cn.com.billboard.model.VersionModel;
import cn.com.billboard.net.BillboardApi;
import cn.com.billboard.ui.CreateParamsActivity;
import cn.com.billboard.ui.LauncherActivity;
import cn.com.billboard.ui.TwoScreenActivity;
import cn.com.billboard.util.APKVersionCodeUtils;
import cn.com.billboard.util.NetStateUtil;
import cn.com.billboard.util.PermissionsUtil;
import cn.com.library.kit.ToastManager;
import cn.com.library.mvp.XPresent;
import cn.com.library.net.ApiSubscriber;
import cn.com.library.net.NetError;
import cn.com.library.net.XApi;

public class LauncherPresent extends XPresent<LauncherActivity> {
    /**权限申请*/
    public void checkPermissions(){
        toActivity();
//        PermissionsUtil.requestPermission(mPermission, new RxPermissions(getV()),
//                Manifest.permission.ACCESS_FINE_LOCATION,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                Manifest.permission.READ_EXTERNAL_STORAGE,
//                Manifest.permission.READ_PHONE_STATE,
//                Manifest.permission.CAMERA,
//                Manifest.permission.RECORD_AUDIO);
    }
    /**权限申请回调*/
    private PermissionsUtil.RequestPermission mPermission = new PermissionsUtil.RequestPermission() {
        @Override
        public void onRequestPermissionSuccess() {
            toActivity();
        }

        @Override
        public void onRequestPermissionFailure() {
            ToastManager.showShort(getV(), "请授权后重启软件");
            PermissionUtils.launchAppDetailsSettings();
            getV().finish();
        }

        @Override
        public void onRequestPermissionFailureWithAskNeverAgain() {
            ToastManager.showShort(getV(), "请授权后重启软件");
            PermissionUtils.launchAppDetailsSettings();
            getV().finish();
        }
    };


    public void toActivity(){
        TwoScreenActivity.launch(getV());
        getV().finish();
    }



}
