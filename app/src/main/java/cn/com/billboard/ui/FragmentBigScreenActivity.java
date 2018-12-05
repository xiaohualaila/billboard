package cn.com.billboard.ui;

import android.app.Activity;
import android.app.smdt.SmdtManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import cn.com.billboard.R;
import cn.com.billboard.dialog.DownloadAPKDialog;
import cn.com.billboard.model.EventMessageModel;
import cn.com.billboard.model.EventModel;
import cn.com.billboard.net.UserInfoKey;
import cn.com.billboard.present.FragmentBigScreenActivityPresent;
import cn.com.billboard.service.GPIOService;
import cn.com.billboard.service.UpdateService;
import cn.com.billboard.ui.fragment.FragmentBigScreenPic;
import cn.com.billboard.ui.fragment.FragmentBigScreenVideo;
import cn.com.billboard.ui.fragment.FragmentUpdate;
import cn.com.billboard.util.AppDownload;
import cn.com.billboard.util.AppSharePreferenceMgr;
import cn.com.library.event.BusProvider;
import cn.com.library.kit.Kits;
import cn.com.library.kit.ToastManager;
import cn.com.library.log.XLog;
import cn.com.library.mvp.XActivity;
import cn.com.library.router.Router;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class FragmentBigScreenActivity extends XActivity<FragmentBigScreenActivityPresent> implements AppDownload.Callback {
    private static FragmentBigScreenActivity instance;
    private Fragment mCurrentFrag;
    private FragmentManager fm;
    private Fragment updateFrag;
    private Fragment imgFrg;
    private Fragment videoFrg;
    private SmdtManager smdt;
    private String mac = "";
    private String ipAddress = "";
    public DownloadAPKDialog dialog_app;


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void initData(Bundle savedInstanceState) {
        fm = getSupportFragmentManager();
        updateFrag = new FragmentUpdate();
        imgFrg = new FragmentBigScreenPic();
        videoFrg = new FragmentBigScreenVideo();
        String model = Build.MODEL;
     //   if (model.equals("3280")) {
            smdt = SmdtManager.create(this);
            smdt.smdtWatchDogEnable((char) 1);//开启看门狗
            mac = smdt.smdtGetEthMacAddress();
            ipAddress = smdt.smdtGetEthIPAddress();

            new Timer().schedule(timerTask, 0, 5000);
       // }
        Log.i("mac", mac);
        if (TextUtils.isEmpty(mac)) {
            ToastManager.showShort(context, "Mac地址，为空请检查网络！");
            toFragmentVideo();
        } else {
            AppSharePreferenceMgr.put(this, UserInfoKey.MAC, mac);
            if(TextUtils.isEmpty(ipAddress)){
                ToastManager.showShort(context, "IP地址为空，请检查网络！");
                toFragmentVideo();
            }else {
                AppSharePreferenceMgr.put(this, UserInfoKey.IPADDRESS, ipAddress);
                getP().getScreenData(true, mac, ipAddress);
            }
        }
        startService(new Intent(context, UpdateService.class));

        BusProvider.getBus().toFlowable(EventMessageModel.class).observeOn(AndroidSchedulers.mainThread()).subscribe(
                messageModel -> {
                    ToastManager.showShort(context, messageModel.message);
                }
        );
        BusProvider.getBus().toFlowable(EventModel.class).subscribe(
                eventModel -> {
                    XLog.e("EventModel===" + eventModel.value);
                    getP().getScreenData(false, mac, ipAddress);
                }
        );
        instance = this;
    }

    public static FragmentBigScreenActivity instance() {
        return instance;
    }

    TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            smdt.smdtWatchDogFeed();//喂狗
        }
    };

    /**
     * 动态添加fragment，不会重复创建fragment
     *
     * @param to 将要加载的fragment
     */
    public void switchContent(Fragment to) {
        if (mCurrentFrag != to) {
            if (!to.isAdded()) {// 如果to fragment没有被add则增加一个fragment
                if (mCurrentFrag != null) {
                    fm.beginTransaction().hide(mCurrentFrag).commit();
                }
                fm.beginTransaction()
                        .add(R.id.fl_content, to)
                        .commit();
            } else {
                fm.beginTransaction().hide(mCurrentFrag).show(to).commit(); // 隐藏当前的fragment，显示下一个
            }
            mCurrentFrag = to;
        }
    }

    public void switchContent2(Fragment to) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fl_content, to)
                .commit();
    }

    public void toFragmentUpdate() {
        switchContent(updateFrag);
    }

    public void toFragmentImg() {
        switchContent(imgFrg);
    }

    public void toFragmentVideo() {
        switchContent(videoFrg);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_fragment;
    }

    @Override
    public FragmentBigScreenActivityPresent newP() {
        return new FragmentBigScreenActivityPresent();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(context, GPIOService.class));
        stopService(new Intent(context, UpdateService.class));
        String model = Build.MODEL;
     //   if (model.equals("3280")) {
            smdt.smdtWatchDogEnable((char) 0);
     //   }
    }

    public void toUpdateVer(String apkurl, String version) {
        Kits.File.deleteFile(Environment.getExternalStorageDirectory().getAbsoluteFile() + "/download/");
        dialog_app = new DownloadAPKDialog(this);
        dialog_app.show();
        dialog_app.setCancelable(false);
        dialog_app.getFile_name().setText("室内屏apk");
        dialog_app.getFile_num().setText("版本号" + version);
        AppDownload appDownload = new AppDownload();
        appDownload.setProgressInterface(this);

        appDownload.downApk(apkurl, this);
    }

    @Override
    public void callProgress(int progress) {
        if (progress >= 100) {
            runOnUiThread(() -> {
                dialog_app.dismiss();
                String sdcardDir = Environment.getExternalStorageDirectory().getAbsoluteFile() + "/download/zhsq.apk";
                install(sdcardDir);
            });

        } else {
            runOnUiThread(() -> {
                dialog_app.getSeekBar().setProgress(progress);
                dialog_app.getNum_progress().setText(progress + "%");
            });
        }
    }

    /**
     * 开启安装过程
     *
     * @param fileName
     */
    private void install(String fileName) {
        //承接我的代码，filename指获取到了我的文件相应路径
        if (fileName != null) {
            if (fileName.endsWith(".apk")) {
                if (Build.VERSION.SDK_INT >= 24) {//判读版本是否在7.0以上
                    File file = new File(fileName);
                    Uri apkUri = FileProvider.getUriForFile(context, "cn.com.billboard.fileprovider", file);
                    //在AndroidManifest中的android:authorities值
                    Intent install = new Intent(Intent.ACTION_VIEW);
                    install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);//添加这一句表示对目标应用临时授权该Uri所代表的文件
                    install.setDataAndType(apkUri, "application/vnd.android.package-archive");
                    context.startActivity(install);
                } else {
                    Intent install = new Intent(Intent.ACTION_VIEW);
                    install.setDataAndType(Uri.fromFile(new File(fileName)), "application/vnd.android.package-archive");
                    install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(install);
                }
            }
        }
    }


    /**
     * 请求失败返回
     */
    public void showError(String error) {
        ToastManager.showShort(context, error);
    }

    public static void launch(Activity activity) {
        Router.newIntent(activity)
                .to(FragmentBigScreenActivity.class)
                .launch();
    }
}
