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
import java.util.concurrent.TimeUnit;
import cn.com.billboard.R;
import cn.com.billboard.dialog.DownloadAPKDialog;
import cn.com.billboard.model.AlarmRecordModel;
import cn.com.billboard.model.EventMessageModel;
import cn.com.billboard.present.FragmentBigScreenActivityPresent;
import cn.com.billboard.service.GPIOBigService;
import cn.com.billboard.service.GPIOBigService2;
import cn.com.billboard.ui.fragment.FragmentBigScreenPic;
import cn.com.billboard.ui.fragment.FragmentBigScreenVideo;
import cn.com.billboard.ui.fragment.FragmentUpdate;
import cn.com.billboard.util.AppDownload;
import cn.com.billboard.util.SharedPreferencesUtil;
import cn.com.library.event.BusProvider;
import cn.com.library.kit.Kits;
import cn.com.library.kit.ToastManager;
import cn.com.library.mvp.XActivity;
import cn.com.library.router.Router;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

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
    private Disposable mDisposable;
    private boolean isFirst = true;
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void initData(Bundle savedInstanceState) {
        fm = getSupportFragmentManager();
        updateFrag = new FragmentUpdate();
        imgFrg = new FragmentBigScreenPic();
        videoFrg = new FragmentBigScreenVideo();
        smdt = SmdtManager.create(this);
        smdt.smdtWatchDogEnable((char) 1);//开启看门狗
        new Timer().schedule(timerTask, 0, 5000);
        heartinterval();
       // startService(new Intent(context, GPIOBigService.class));//两个电话四个按键
       // startService(new Intent(context, GPIOBigService2.class));//一个电话四个按键
        getBus();
        instance = this;
    }

    private void getBus() {
        BusProvider.getBus().toFlowable(EventMessageModel.class).observeOn(AndroidSchedulers.mainThread()).subscribe(
                messageModel -> {
                    ToastManager.showShort(context, messageModel.message);
                }
        );
        /**
         * 报警
         */
        BusProvider.getBus().toFlowable(AlarmRecordModel.class).observeOn(AndroidSchedulers.mainThread()).subscribe(
                (AlarmRecordModel recordModel) -> {
                    if (recordModel.isCalling) {
                        int phoneType = recordModel.phoneType;
                        getP().uploadAlarm(mac, phoneType);
                    }
                }
        );
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
     * 发送心跳数据
     */
    private void heartinterval() {
        int time =  SharedPreferencesUtil.getInt(this, "time", 10);
        mDisposable = Flowable.interval(0, time, TimeUnit.MINUTES)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> {
                    mac = smdt.smdtGetEthMacAddress();
                    ipAddress = smdt.smdtGetEthIPAddress();
                    if(TextUtils.isEmpty(mac) && TextUtils.isEmpty(ipAddress)){
                        ToastManager.showShort(context, "Mac地址或IP地址不能为空，请检查网络！");
                        toFragmentVideo();
                        isFirst = false;
                        return;
                    }
                    getP().getScreenData(isFirst, mac, ipAddress);
                    isFirst = false;
                });
    }

    /**
     * 动态添加fragment，不会重复创建fragment
     *
     * @param to 将要加载的fragment
     */
    public void switchContent(Fragment to) {
        try {
            if (mCurrentFrag != to) {
                if (!to.isAdded()) {// 如果to fragment没有被add则增加一个fragment
                    if (mCurrentFrag != null) {
                        fm.beginTransaction().hide(mCurrentFrag).commit();
                    }
                    fm.beginTransaction()
                            .add(R.id.fl_content, to)
                            .commit();
                } else {
                    fm.beginTransaction().hide(mCurrentFrag).show(to).commitAllowingStateLoss(); // 隐藏当前的fragment，显示下一个
                }
                mCurrentFrag = to;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        smdt.smdtWatchDogEnable((char) 0);
      //  stopService(new Intent(context, GPIOBigService.class));
      //  stopService(new Intent(context, GPIOBigService2.class));
        if (mDisposable != null) {
            mDisposable.dispose();
        }
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
