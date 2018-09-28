package cn.com.billboard.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.view.View;
import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;

import java.io.File;

import cn.com.billboard.dialog.DownloadAPKDialog;
import cn.com.billboard.dialog.DownloadDialog;
import cn.com.billboard.model.VersionModel;
import cn.com.billboard.net.UserInfoKey;
import cn.com.billboard.util.AppDownload;
import cn.com.billboard.util.AppSharePreferenceMgr;
import cn.com.billboard.R;
import cn.com.billboard.present.LauncherPresent;
import cn.com.billboard.widget.LoadingDialog;
import cn.com.library.kit.ToastManager;
import cn.com.library.mvp.XActivity;
import cn.com.library.net.NetError;

public class LauncherActivity extends XActivity<LauncherPresent> implements AppDownload.Callback{

    public LoadingDialog dialog;
    public DownloadAPKDialog dialog_app;

    @Override
    public void initData(Bundle savedInstanceState) {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        dialog = new LoadingDialog(context, "请稍后···");
//        XLog.e("getAllDevices====" + new Gson().toJson(new SerialPortFinder().getAllDevices()));
//        XLog.e("getAllDevicesPath====" + new Gson().toJson(new SerialPortFinder().getAllDevicesPath()));
//        LocationUtil.getInstance().startLocation(context);
        getP().checkPermissions();
    }

    /**
     * 判断是否选择过屏
     */
    public void nextAction() {
        if (((int) AppSharePreferenceMgr.get(context, UserInfoKey.SCREEN_NUM, -1)) == -1)
            selectScreenNum();
        else {
            dialog.show();
            getP().loadData((int) AppSharePreferenceMgr.get(context, UserInfoKey.SCREEN_NUM, -1));
        }
    }

    /**
     * 选择屏幕
     */
    private void selectScreenNum() {
        new AlertView("选择屏幕", null, null, null, new String[]{"室内双屏", "室外大屏",  "拍照", "视频"}, this, AlertView.Style.ActionSheet,
                (o, position) -> {
                    dialog.show();
                    ToastManager.showShort(context, position == 0 ? "室内双屏" : position == 1 ? "室外大屏" : position == 2 ?  "拍照" : "视频");
                    AppSharePreferenceMgr.put(context, UserInfoKey.SCREEN_NUM, position);
                    getP().loadData(position);
                }).show();
    }

    /**
     * 请求返回错误
     */
    public void showError(NetError error) {
        dialog.dismiss();
        if (error != null) {
            switch (error.getType()) {
                case NetError.ParseError:
                    ToastManager.showShort(context, "数据解析异常");
                    break;

                case NetError.AuthError:
                    ToastManager.showShort(context, "身份验证异常");
                    break;

                case NetError.BusinessError:
                    ToastManager.showShort(context, "业务异常");
                    break;

                case NetError.NoConnectError:
                    ToastManager.showShort(context, "网络无连接");
                    break;

                case NetError.NoDataError:
                    ToastManager.showShort(context, "数据为空");
                    break;

                case NetError.OtherError:
                    ToastManager.showShort(context, "其他异常");
                    break;
            }

                CreateParamsActivity.launch(context);
                finish();
        }
    }

    public void updateVersion(VersionModel model){
        dialog_app = new DownloadAPKDialog(this);
        dialog_app.show();
        dialog_app.setCancelable(false);
        dialog_app.getFile_name().setText(model.getVdetails());
        dialog_app.getFile_num().setText(model.getVnumber());
        AppDownload appDownload = new AppDownload();
        appDownload.setProgressInterface(this);
        appDownload.downApk(model.getDownload(),this);
    }


    @Override
    public int getLayoutId() {
        return R.layout.activity_launcher;
    }

    @Override
    public LauncherPresent newP() {
        return new LauncherPresent();
    }

    @Override
    public void callProgress(int progress) {
        if (progress >= 100) {
            runOnUiThread(() -> {
                dialog_app.dismiss();
                String path = "/storage/emulated/0/download/" + "终端.apk";
                install(path);
            });

        }else {
            runOnUiThread(() -> {
                dialog_app.getSeekBar().setProgress( progress );
                dialog_app.getNum_progress().setText(progress+"%");
            });
        }
    }

    /**
     * 开启安装过程
     * @param fileName
     */
    private void install(String fileName) {

        File file = new File(fileName);
        Intent intent = new Intent();
        intent.setAction( Intent.ACTION_VIEW );
        intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
        //判读版本是否在7.0以上
        Uri apkUri = null;
        if (Build.VERSION.SDK_INT >= 24) {
            intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK );
            apkUri = FileProvider.getUriForFile( this, "cn.com.billboard.fileprovider", file );
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            apkUri = Uri.fromFile(file);

        }
        intent.setDataAndType( apkUri, "application/vnd.android.package-archive" );
        startActivity( intent );
    }

}
