package cn.com.billboard.ui;

import android.app.smdt.SmdtManager;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
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
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import cn.com.billboard.R;
import cn.com.billboard.dialog.DownloadAPKDialog;
import cn.com.billboard.model.AlarmRecordModel;
import cn.com.billboard.model.EventMessageModel;
import cn.com.billboard.net.UserInfoKey;
import cn.com.billboard.present.FragmentActivityPresent;

import cn.com.billboard.service.GPIOService;
import cn.com.billboard.service.UpdateService;
import cn.com.billboard.ui.fragment.FragmentBigPic;
import cn.com.billboard.ui.fragment.FragmentMain;
import cn.com.billboard.ui.fragment.FragmentUpdate;
import cn.com.billboard.ui.fragment.FragmentVideo;
import cn.com.billboard.util.AppDownload;
import cn.com.billboard.util.AppSharePreferenceMgr;
import cn.com.library.event.BusProvider;
import cn.com.library.kit.Kits;
import cn.com.library.kit.ToastManager;
import cn.com.library.log.XLog;
import cn.com.library.mvp.XActivity;
import cn.com.library.net.NetError;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class FragmentActivity extends XActivity<FragmentActivityPresent> implements AppDownload.Callback{

    private Fragment mCurrentFrag;
    private FragmentManager fm;
    private Fragment updateFrag;
    private Fragment mainFrag;
    private Fragment bigPigFrag;
    private Fragment recordVideoFrag;
    DisplayManager displayManager;//屏幕管理类
    Display[] displays;//屏幕数组

    private SmdtManager smdt;
    private String mac = "";
    private String ipAddress = "";
    public DownloadAPKDialog dialog_app;

    private int phoneType = 1;
    private String recordId = "";

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void initData(Bundle savedInstanceState) {
        displayManager = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
        displays = displayManager.getDisplays();
        fm = getSupportFragmentManager();
        updateFrag = new FragmentUpdate();
        mainFrag = new FragmentMain();
        bigPigFrag = new FragmentBigPic();
//        recordVideoFrag = new FragmentVideo(phoneType);

        /**
         * 老板子没有喂狗api
         */
        String model = Build.MODEL;
        if(model.equals("3280")){
            smdt = SmdtManager.create(this);
            smdt.smdtWatchDogEnable((char)1);//开启看门狗
            mac= smdt.smdtGetEthMacAddress();
            ipAddress= smdt.smdtGetEthIPAddress();
            AppSharePreferenceMgr.put(this,UserInfoKey.MAC,mac);
            AppSharePreferenceMgr.put(this,UserInfoKey.IPADDRESS,ipAddress);
            new Timer().schedule(timerTask,0,5000);
        }
        Log.i("mac",mac);
        if(TextUtils.isEmpty(mac)){
            ToastManager.showShort(context, "Mac地址，为空请检查网络！");
            toFragemntMain();
        }else {
           getP().getScreenData(true, mac,ipAddress);
        }
        startService(new Intent(context, GPIOService.class));
        startService(new Intent(context, UpdateService.class));
        /**
         * 报警
         */
        BusProvider.getBus().toFlowable(AlarmRecordModel.class).observeOn(AndroidSchedulers.mainThread()).subscribe(
                (AlarmRecordModel recordModel) -> {
                    if (recordModel.isCalling) {
                        phoneType = recordModel.phoneType;
                        getP().uploadAlarm(mac,phoneType);
                    }
                }
        );
        BusProvider.getBus().toFlowable(EventMessageModel.class).observeOn(AndroidSchedulers.mainThread()).subscribe(
                messageModel -> {
                    ToastManager.showShort(context, messageModel.message);
                }
        );
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        /**
         * 上传报警信息图片，视频
         */
        getP().uploadAlarmInfo(mac,recordId);
    }


    TimerTask timerTask = new TimerTask(){
        @Override
        public void run() {
            smdt.smdtWatchDogFeed();//喂狗
        }
    };

    /**展示副屏数据*/
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void showSubData(){
        XLog.e("屏幕数量===" + displays.length);
        if (displays != null && displays.length > 1) {
            SubScreenActivity subScreenActivity = new SubScreenActivity(context, displays[1]);//displays[1]是副屏
            subScreenActivity.getWindow().setType(WindowManager.LayoutParams.TYPE_TOAST);
            subScreenActivity.show();
            subScreenActivity.showData();
        }
    }

    public void getAlarmId(String s) {
        if(!TextUtils.isEmpty(s)){
            recordId = s;
        //    toFragmentVideo();
            RecordvideoActivity.launch(this, mac,phoneType);
        }
    }

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

    public void toFragemntUpdate(){
        switchContent(updateFrag);
    }

    public void toFragemntBigPic(){
        switchContent(bigPigFrag);
    }

    public void toFragemntMain(){
        switchContent(mainFrag);
    }

//    public void toFragmentVideo(){
//        switchContent(recordVideoFrag);
//    }


    @Override
    public int getLayoutId() {
        return R.layout.activity_fragment;
    }

    @Override
    public FragmentActivityPresent newP() {
         return new FragmentActivityPresent();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(context, GPIOService.class));
        stopService(new Intent(context, UpdateService.class));
        String model = Build.MODEL;
        if(model.equals("3280")) {
            smdt.smdtWatchDogEnable((char) 0);
        }
    }

    public void toUpdateVer(String apkurl, String version) {
        Kits.File.deleteFile(Environment.getExternalStorageDirectory().getAbsoluteFile() + "/download/");
        dialog_app = new DownloadAPKDialog(this);
        dialog_app.show();
        dialog_app.setCancelable(false);
        dialog_app.getFile_name().setText("室内屏apk");
        dialog_app.getFile_num().setText("版本号"+version);
        AppDownload appDownload = new AppDownload();
        appDownload.setProgressInterface(this);

        appDownload.downApk(apkurl,this);
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


    /**请求失败返回*/
    public void showError(String error) {
        ToastManager.showShort(context, error);
    }
}
