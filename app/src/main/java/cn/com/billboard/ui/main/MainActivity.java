package cn.com.billboard.ui.main;

import android.app.smdt.SmdtManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
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
import cn.com.billboard.service.GPIOBigServiceNew;
import cn.com.billboard.ui.fragment.FragmentBigScreenPic;
import cn.com.billboard.ui.fragment.FragmentMediaPlayer;
import cn.com.billboard.ui.fragment.FragmentUpdate;
import cn.com.billboard.util.AppDownload;
import cn.com.billboard.util.Kits;
import cn.com.billboard.util.SharedPreferencesUtil;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;


public class MainActivity extends AppCompatActivity implements AppDownload.Callback,MainContract.View  {
    private static MainActivity instance;
    private Fragment updateFrag;
    private Fragment imgFrg;
    private Fragment videoFrg;
    private SmdtManager smdt;
    private String mac = "";
    private String ipAddress = "";
    public DownloadAPKDialog dialog_app;
    private Disposable mDisposable;
    private boolean isFirst = true;
    private MainContract.Presenter presenter;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        ButterKnife.bind(this);
        new MainPresenter(this);
        updateFrag = new FragmentUpdate();
        imgFrg = new FragmentBigScreenPic();
        videoFrg = new FragmentMediaPlayer();
        smdt = SmdtManager.create(this);
        smdt.smdtWatchDogEnable((char) 1);//开启看门狗
        new Timer().schedule(timerTask, 0, 5000);
        heartinterval();
//        startService(new Intent(context, GPIOBigService.class));//两个电话四个按键
        //  startService(new Intent(context, GPIOBigService2.class));//一个电话四个按键
  //      startService(new Intent(this, GPIOBigServiceNew.class));//采用了新的接线板子
        getBus();
        instance = this;

    }



    private void getBus() {
        BusProvider.getBus().toFlowable(EventMessageModel.class).observeOn(AndroidSchedulers.mainThread()).subscribe(
                messageModel -> {
                    Toast.makeText(this,messageModel.message,Toast.LENGTH_LONG).show();
                }
        );
        /**
         * 报警
         */
        BusProvider.getBus().toFlowable(AlarmRecordModel.class).observeOn(AndroidSchedulers.mainThread()).subscribe(
                (AlarmRecordModel recordModel) -> {
                    if (recordModel.isCalling) {
                        int phoneType = recordModel.phoneType;
                        presenter.uploadAlarm(mac, phoneType);
                    }
                }
        );
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
        time=2;
        mDisposable = Flowable.interval(0, time, TimeUnit.MINUTES)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> {
                    mac = smdt.smdtGetEthMacAddress();
                    ipAddress = smdt.smdtGetEthIPAddress();
                    if(TextUtils.isEmpty(mac) && TextUtils.isEmpty(ipAddress)){
                        showError("Mac地址或IP地址不能为空，请检查网络！");
                        toFragmentVideo();
                        isFirst = false;
                        return;
                    }
                    presenter.getScreenData(this,isFirst, mac, ipAddress);
                    isFirst = false;
                });
    }

    public void switchContent(Fragment to) {
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


    public int getLayoutId() {
        return R.layout.activity_fragment;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        smdt.smdtWatchDogEnable((char) 0);
        //  stopService(new Intent(this, GPIOBigService.class));
        //  stopService(new Intent(this, GPIOBigService2.class));
        stopService(new Intent(this, GPIOBigServiceNew.class));
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

    @Override
    public void setPresenter(MainContract.Presenter presenter) {
         this.presenter = presenter;
    }

    public static MainActivity instance() {
        return instance;
    }
}
