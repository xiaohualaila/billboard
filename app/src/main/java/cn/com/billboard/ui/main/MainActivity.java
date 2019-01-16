package cn.com.billboard.ui.main;

import android.app.smdt.SmdtManager;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import butterknife.ButterKnife;
import cn.com.billboard.R;
import cn.com.billboard.dialog.DownloadAPKDialog;
import cn.com.billboard.event.BusProvider;
import cn.com.billboard.model.AlarmRecordModel;
import cn.com.billboard.model.EventMessageModel;
import cn.com.billboard.service.GPIOService;
import cn.com.billboard.service.GPIOServiceNew;
import cn.com.billboard.ui.RecordvideoActivity;
import cn.com.billboard.ui.SubScreenActivity;
import cn.com.billboard.ui.fragment.FragmentMain2;
import cn.com.billboard.ui.fragment.FragmentPic;
import cn.com.billboard.ui.fragment.FragmentUpdate;
import cn.com.billboard.util.AppDownload;
import cn.com.billboard.util.Kits;
import cn.com.billboard.util.SharedPreferencesUtil;
import cn.com.billboard.util.UserInfoKey;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

import static android.os.Build.VERSION_CODES.M;


public class MainActivity extends AppCompatActivity implements AppDownload.Callback,MainContract.View  {
    private MainContract.Presenter presenter;
    private Fragment updateFrag;
    private Fragment mainFrag;
    private Fragment bigPigFrag;
    DisplayManager displayManager;//屏幕管理类
    Display[] displays;//屏幕数组

    private static SmdtManager smdt;
    private String mac = "";
    private String ipAddress = "";
    public DownloadAPKDialog dialog_app;

    private int phoneType = 1;
    private String recordId = "";
    private static MainActivity instance;

    private boolean isFirst = true;
    private Disposable mDisposable;
    private TimerTask timerTask ;
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        ButterKnife.bind(this);
        new MainPresenter(this);
        displayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
        displays = displayManager.getDisplays();
        updateFrag = new FragmentUpdate();
        mainFrag = new FragmentMain2();
        bigPigFrag = new FragmentPic();

        /**
         * 喂狗api
         */
        smdt = SmdtManager.create(this);
//        smdt.smdtWatchDogEnable((char) 1);//开启看门狗
        heartinterval();
       //   startService(new Intent(this, GPIOService.class));
        startService(new Intent(this, GPIOServiceNew.class));
        getBusDate();
        instance = this;

        mac = smdt.smdtGetEthMacAddress();
        ipAddress = smdt.smdtGetEthIPAddress();

        Runtime r = Runtime.getRuntime();
        Log.i("sss","最大可用内存:" + r.maxMemory() / M + "M");
        Log.i("sss","当前可用内存:" + r.totalMemory()/ M + "M");
        Log.i("sss","当前空闲内存:" + r.freeMemory() / M + "M");
        Log.i("sss","当前已使用内存:" + (r.totalMemory() - r.freeMemory()) / M + "M");
//        timer();//开始定时喂狗程序
        presenter.getScreenData(isFirst, mac, ipAddress,this);
        isFirst = false;
    }

    void timer(){
        timerTask = new MyTimerTask() ;
        new Timer().schedule( timerTask ,0,5000 );  // 1秒后启动一个任务
    }

    private  class MyTimerTask extends TimerTask{

        @Override
        public void run() {
            smdt.smdtWatchDogFeed();//喂狗
        }
    }


    /**
     * 报警
     */
    private void getBusDate() {
        BusProvider.getBus().toFlowable(AlarmRecordModel.class).observeOn(AndroidSchedulers.mainThread()).subscribe(
                (AlarmRecordModel recordModel) -> {
                    if (recordModel.isCalling) {
                        phoneType = recordModel.phoneType;
                      presenter.uploadAlarm(mac, phoneType);
                    }
                }
        );
        BusProvider.getBus().toFlowable(EventMessageModel.class).observeOn(AndroidSchedulers.mainThread()).subscribe(
                messageModel -> {
                    Toast.makeText(this,messageModel.message,Toast.LENGTH_LONG).show();
                }
        );
    }

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
                        Toast.makeText(this,"Mac地址或IP地址不能为空，请检查网络！",Toast.LENGTH_LONG).show();
                        toFragemntMain();
                        isFirst = false;
                        return;
                    }
                    SharedPreferencesUtil.putString(this, UserInfoKey.MAC, mac);
                    presenter.getScreenData(isFirst, mac, ipAddress,this);
                    isFirst = false;
                    Log.i("sss",">>>>>>>>>>>>>>>>>>>>>心跳");
                });

    }

    @Override
    protected void onRestart() {
        super.onRestart();

        /**
         * 上传报警信息图片，视频
         */
        String video_path =  SharedPreferencesUtil.getString(this, "videoFile", "");
        String pic_path =  SharedPreferencesUtil.getString(this, "picFile", "");
        presenter.uploadAlarmInfo(mac, recordId,video_path,pic_path);
    }

    /**
     * 展示副屏数据
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void showSubData() {
       // XLog.e("屏幕数量===" + displays.length);
        if (displays != null && displays.length > 1) {
            SubScreenActivity subScreenActivity = new SubScreenActivity(this, displays[1]);//displays[1]是副屏
            subScreenActivity.getWindow().setType(WindowManager.LayoutParams.TYPE_TOAST);
            subScreenActivity.show();
            subScreenActivity.showData();
        }
    }

    public void getAlarmId(String s) {
        if (!TextUtils.isEmpty(s)) {
            recordId = s;
            Intent intent = new Intent(this,RecordvideoActivity.class);
            intent.putExtra("mac",mac);
            intent.putExtra("phoneType",phoneType);
            startActivity(intent);
        }
    }

    public void switchContent(Fragment to) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fl_content, to)
                .commitAllowingStateLoss();
    }

    public void toFragemntUpdate() {
            switchContent(updateFrag);
    }

    public void toFragemntBigPic() {
        switchContent(bigPigFrag);
    }

    public void toFragemntMain() {
        switchContent(mainFrag);
    }

    public int getLayoutId() {
        return R.layout.activity_fragment;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        smdt.smdtWatchDogEnable((char) 0);//停止喂狗
        if ( timerTask != null ){
            timerTask.cancel() ;
        }
      //   stopService(new Intent(this, GPIOService.class));
        stopService(new Intent(this, GPIOServiceNew.class));
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
                    Uri apkUri = FileProvider.getUriForFile(this, "cn.com.billboard.fileprovider", file);
                    //在AndroidManifest中的android:authorities值
                    Intent install = new Intent(Intent.ACTION_VIEW);
                    install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);//添加这一句表示对目标应用临时授权该Uri所代表的文件
                    install.setDataAndType(apkUri, "application/vnd.android.package-archive");
                    startActivity(install);
                } else {
                    Intent install = new Intent(Intent.ACTION_VIEW);
                    install.setDataAndType(Uri.fromFile(new File(fileName)), "application/vnd.android.package-archive");
                    install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(install);
                }
            }
        }
    }

    /**
     * 请求失败返回
     */
    public void showError(String error) {
        Toast.makeText(this,error,Toast.LENGTH_LONG).show();
    }


    public static MainActivity instance() {
        return instance;
    }

    @Override
    public void setPresenter(MainContract.Presenter presenter) {
        this.presenter = presenter;
    }


}
