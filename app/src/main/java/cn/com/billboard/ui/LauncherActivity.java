package cn.com.billboard.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.view.View;
import java.io.File;
import cn.com.billboard.dialog.DownloadAPKDialog;
import cn.com.billboard.model.VersionModel;
import cn.com.billboard.util.AppDownload;
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
        getP().checkPermissions();
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
                String sdcardDir = Environment.getExternalStorageDirectory().getAbsoluteFile() + "/download/zhsq.apk";
                install(sdcardDir);
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
        //承接我的代码，filename指获取到了我的文件相应路径
         if (fileName != null) {
             if (fileName.endsWith(".apk")) {
                 if(Build.VERSION.SDK_INT>=24) {//判读版本是否在7.0以上
                       File file= new File(fileName);
                       Uri apkUri = FileProvider.getUriForFile(context, "cn.com.billboard.fileprovider", file);
                       //在AndroidManifest中的android:authorities值
                        Intent install = new Intent(Intent.ACTION_VIEW);
                        install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);//添加这一句表示对目标应用临时授权该Uri所代表的文件
                              install.setDataAndType(apkUri, "application/vnd.android.package-archive");
                              context.startActivity(install);
                 } else{
                     Intent install = new Intent(Intent.ACTION_VIEW);
                     install.setDataAndType(Uri.fromFile(new File(fileName)), "application/vnd.android.package-archive");
                     install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                     context.startActivity(install);
                 }
             }
         }


    }



}
