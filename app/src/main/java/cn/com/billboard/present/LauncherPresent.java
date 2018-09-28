package cn.com.billboard.present;

import android.Manifest;
import android.app.ProgressDialog;

import com.blankj.utilcode.util.PermissionUtils;
import com.google.gson.Gson;
import com.tbruyelle.rxpermissions2.RxPermissions;

import cn.com.billboard.download.DownLoadObserver;
import cn.com.billboard.download.DownloadInfo;
import cn.com.billboard.download.DownloadManager;
import cn.com.billboard.model.BaseBean;
import cn.com.billboard.model.VersionModel;
import cn.com.billboard.net.BillboardApi;
import cn.com.billboard.net.UserInfoKey;
import cn.com.billboard.ui.CreateParamsActivity;
import cn.com.billboard.ui.FaceActivity;
import cn.com.billboard.ui.LauncherActivity;
import cn.com.billboard.ui.VideoActivity;
import cn.com.billboard.util.APKVersionCodeUtils;
import cn.com.billboard.util.AppSharePreferenceMgr;
import cn.com.billboard.util.PermissionsUtil;
import cn.com.billboard.util.SDCardUtil;
import cn.com.library.kit.ToastManager;
import cn.com.library.log.XLog;
import cn.com.library.mvp.XPresent;
import cn.com.library.net.ApiSubscriber;
import cn.com.library.net.NetError;
import cn.com.library.net.XApi;

public class LauncherPresent extends XPresent<LauncherActivity> {

    /**权限申请*/
    public void checkPermissions(){
        PermissionsUtil.requestPermission(mPermission, new RxPermissions(getV()),
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO);
    }
    /**权限申请回调*/
    private PermissionsUtil.RequestPermission mPermission = new PermissionsUtil.RequestPermission() {
        @Override
        public void onRequestPermissionSuccess() {
            getV().nextAction();

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
    /**获取数据*/
    public void loadData(int version) {
        BillboardApi.getDataService().checkVersion(version)
                .compose(XApi.<BaseBean<VersionModel>>getApiTransformer())
                .compose(XApi.<BaseBean<VersionModel>>getScheduler())
                .compose(getV().<BaseBean<VersionModel>>bindToLifecycle())
                .subscribe(new ApiSubscriber<BaseBean<VersionModel>>() {
                    @Override
                    protected void onFail(NetError error) {
                        getV().showError(error);
                    }

                    @Override
                    public void onNext(BaseBean<VersionModel> model) {
                        getV().dialog.dismiss();
                        if (model.isSuccess()) {
//                            XLog.e("model========" + new Gson().toJson(model));
//                            getV().showData(model.getMessageBody());
                            VersionModel model1 = model.getMessageBody();
                            int v_no = APKVersionCodeUtils.getVersionCode(getV());
                            if(model1.getBuild()> v_no){
                                   getV().updateVersion(model1);
                            }else {
                                checkVersion(model.getMessageBody());
                            }
                        } else {
                            ToastManager.showShort(getV(), model.getDescribe());
                            checkVersion(model.getMessageBody());
                        }
                    }
                });
    }
    /**检查版本*/
    public void checkVersion(VersionModel model){
        XLog.e(SDCardUtil.getStoragePath(getV()));
       if (((int) AppSharePreferenceMgr.get(getV(), UserInfoKey.SCREEN_NUM, -1)) == 3)  {
            VideoActivity.launch(getV());
            getV().finish();
        } else {
            CreateParamsActivity.launch(getV());
            getV().finish();
        }

    }
    /**版本更新*/
    private void downloadApp(String downloadUrl){
        XLog.e("下载AppUrl===" + downloadUrl);
        ProgressDialog dialog = new ProgressDialog(getV());
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);// 设置水平进度条
        dialog.setCancelable(false);// 设置是否可以通过点击Back键取消
        dialog.setCanceledOnTouchOutside(false);// 设置在点击Dialog外是否取消Dialog进度条
        dialog.setTitle("软件更新中……");
        dialog.show();
        DownloadManager.getInstance().download(downloadUrl, UserInfoKey.FILE_APK, new DownLoadObserver() {
            @Override
            public void onNext(DownloadInfo value) {
                super.onNext(value);
                dialog.setMax((int) value.getTotal());
                dialog.setProgress((int) value.getProgress());
            }

            @Override
            public void onComplete() {
                if(downloadInfo != null){
                    dialog.dismiss();
                    ToastManager.showShort(getV(), UserInfoKey.FILE_APK + "/" + downloadInfo.getFileName() + "下载完成！");
                }
            }
        });
    }

}
