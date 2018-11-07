package cn.com.billboard.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.smdt.SmdtManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.display.DisplayManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewPager;
import android.telecom.TelecomManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import cn.com.billboard.R;
import cn.com.billboard.dialog.DownloadAPKDialog;
import cn.com.billboard.model.EventModel;
import cn.com.billboard.model.EventRecordVideoModel;
import cn.com.billboard.model.ProgressModel;
import cn.com.billboard.net.UserInfoKey;
import cn.com.billboard.present.TwoScreenPresent;
import cn.com.billboard.service.GPIOService;
import cn.com.billboard.util.AppDownload;
import cn.com.billboard.util.AppPhoneMgr;
import cn.com.billboard.util.AppSharePreferenceMgr;
import cn.com.billboard.util.FileUtil;

import cn.com.billboard.widget.BannersAdapter;
import cn.com.billboard.widget.BaseViewPager;
import cn.com.library.event.BusProvider;
import cn.com.library.imageloader.ILFactory;
import cn.com.library.kit.Kits;
import cn.com.library.kit.ToastManager;
import cn.com.library.log.XLog;
import cn.com.library.mvp.XActivity;
import cn.com.library.net.NetError;
import cn.com.library.router.Router;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class TwoScreenActivity extends XActivity<TwoScreenPresent> implements AppDownload.Callback {
    @BindView(R.id.video_view)
    View videoView;
    @BindView(R.id.main_video)
    VideoView video;
    @BindView(R.id.main_banner)
    BaseViewPager banner;
    @BindView(R.id.pic_banner)
    BaseViewPager pic_banner;
    @BindView(R.id.video_img)
    ImageView videoImg;
    @BindView(R.id.rl_pro)
    RelativeLayout rl_pro;

    @BindView(R.id.progressBarHorizontal)
    ProgressBar progressBarHorizontal;
    @BindView(R.id.loading_file_name)
    TextView loading_file_name;
    @BindView(R.id.loading_num)
    TextView loading_num;
    @BindView(R.id.loading_pro)
    TextView loading_pro;
    private int type = 0, videoIndex = 0;

    private List<String> images_small;
    private List<String> images_big;
    private List<String> videos;

    DisplayManager displayManager;//屏幕管理类
    Display[] displays;//屏幕数组

    int height = 0;

    private String file_name = "";
    private String file_num = "";
    private int file_pre;

    private boolean isUPdate = true;

    private SmdtManager smdt;

    private String mac = "";
    private String ipAddress = "";
    private boolean isVideoAgain = false;
    private boolean isSmallPicFis = false;

    public DownloadAPKDialog dialog_app;

    private Handler mHandler = new Handler();

    private int phoneType = 1;

    private boolean isPlayVideo = false;
    private boolean isNotPlayedBigPic = true;
    @SuppressLint("NewApi")
    @Override
    public void initData(Bundle savedInstanceState) {
        height = AppPhoneMgr.getInstance().getPhoneHeight(context);
        displayManager = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
        displays = displayManager.getDisplays();

        rl_pro.setVisibility(View.VISIBLE);

        images_big = new ArrayList<>();
        images_small = new ArrayList<>();
        BusProvider.getBus().toFlowable(ProgressModel.class).observeOn(AndroidSchedulers.mainThread()).subscribe(
                progressModel -> {

                    int pp = (int) ((float) progressModel.progress / (float) progressModel.total * 100);
                    file_pre = pp;
                    file_num = progressModel.index + "/" + progressModel.num;
                    file_name = progressModel.type + progressModel.fileName;
                    if (isUPdate) {
                        isUPdate = false;
                        mHandler.postDelayed(runnable, 30);
                    }
                }
        );

        BusProvider.getBus().toFlowable(EventRecordVideoModel.class).observeOn(AndroidSchedulers.mainThread()).subscribe(
                model -> {
                    if (model.isCalling) {
                        phoneType = model.phoneType;
                        RecordvideoActivity.launch(this, mac, model.phoneType);
                    }
                }
        );

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
            new Timer().schedule(timerTask,0,5000);
        }
        Log.i("mac",mac);
        getP().getScreenData(true, mac,ipAddress);
        startService(new Intent(context, GPIOService.class));
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        showData();
        String video_path = (String) AppSharePreferenceMgr.get(this, "videoFile", "");
        String pic_path = (String) AppSharePreferenceMgr.get(this, "picFile", "");
        if(!TextUtils.isEmpty(video_path)){
            File file = new File(video_path);
            if (file.exists()) {
                getP().uploadVideo(mac, phoneType, file);
            }
        }
        if(!TextUtils.isEmpty(pic_path)){
            File file = new File(pic_path);
            if (file.exists()) {
                getP().uploadFacePic(mac, phoneType, file);
            }
        }
    }

    TimerTask timerTask = new TimerTask(){
        @Override
        public void run() {
             smdt.smdtWatchDogFeed();//喂狗
        }
    };

    public void showToastManger(String error){
        ToastManager.showShort(context, error);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(context, GPIOService.class));
        String model = Build.MODEL;
        if(model.equals("3280")) {
            smdt.smdtWatchDogEnable((char) 0);
        }
    }

    Runnable runnable =  new Runnable() {
        @Override
        public void run()
        {
            loading_file_name.setText(file_name);
            loading_num.setText(file_num);
            progressBarHorizontal.setProgress(file_pre);
            loading_pro.setText(file_pre+"%");
            mHandler.postDelayed(runnable, 100);
        }
    };

    public void toastL(String msg){
        ToastManager.showLong(context, msg);
    }


    @Override
    protected void onResume() {
        super.onResume();
    }


    /**展示主屏数据*/
    public void showData() {
        mHandler.removeCallbacks(runnable);
        rl_pro.setVisibility(View.GONE);
        videos =  FileUtil.getFilePath(UserInfoKey.VIDEO);
        images_small = FileUtil.getFilePath(UserInfoKey.PIC_SMALL_DOWN);
        images_big = FileUtil.getFilePath(UserInfoKey.PIC_BIG_DOWM);

        if (images_big.size()>0){
            playVideo();
        }
        if(images_small.size()>0){
            playSmallBanner();
        }else {
            videoImg.setVisibility(View.VISIBLE);
            pic_banner.setVisibility(View.GONE);
        }
    }

    public void showDownFile(){
        isUPdate = true;
        rl_pro.setVisibility(View.VISIBLE);
        videoView.setVisibility(View.GONE);
        banner.setVisibility(View.GONE);
    }

    /**展示副屏数据*/
    public void showSubData(){
        XLog.e("屏幕数量===" + displays.length);
        if (displays != null && displays.length > 1) {
            SubScreenActivity subScreenActivity = new SubScreenActivity(context, displays[1]);//displays[1]是副屏
            subScreenActivity.getWindow().setType(WindowManager.LayoutParams.TYPE_TOAST);
            subScreenActivity.show();
            subScreenActivity.showData();
        }
    }

    /**播放图片轮播*/
    private void playBanner(){
        pic_banner.stopScroll();
        isSmallPicFis = false;
        isVideoAgain = false;
        videoView.setVisibility(View.GONE);
        banner.setVisibility(View.VISIBLE);
        banner.setAdapter(new BannersAdapter(initBanner(images_big)));
        banner.setIsOutScroll(true);
        banner.startScroll();
        banner.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                Log.i("sss","图片播放完毕,休眠图片播放时长后播放视频 " +position );
                if (position == images_big.size() - 1) {
                    if(isPlayVideo){
                        return;
                    }
                    //图片播放完毕,休眠图片播放时长后播放视频
                    mHandler.postDelayed(() ->
                        backplay()
                            ,5000);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void backplay(){
        isPlayVideo = true;
        banner.stopScroll();
        banner.setVisibility(View.GONE);
        playVideo();
        if(images_small.size()>0){
            pic_banner.startScroll();
            videoImg.setVisibility(View.GONE);
            pic_banner.setVisibility(View.VISIBLE);
        }else {
            videoImg.setVisibility(View.VISIBLE);
            pic_banner.setVisibility(View.GONE);
        }

    }


    /**播放图片轮播,小图片轮播*/
    private void playSmallBanner(){
        pic_banner.setVisibility(View.VISIBLE);
        videoImg.setVisibility(View.GONE);
        pic_banner.setAdapter(new BannersAdapter(initBanner(images_small)));
        pic_banner.setIsOutScroll(true);
        pic_banner.startScroll();
        pic_banner.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                Log.i("ssss","position  " +position );

                if(position == images_small.size()-1){
                    isSmallPicFis = true;
                    if(isVideoAgain){//判断视频是否重复播放了,如果视频重复播放了，直接播放大图片
                        if(images_big.size()==0){
                            return;
                        }
                        isPlayVideo = false;
                        isSmallPicFis = false;
                        isVideoAgain = false;
                        videoView.setVisibility(View.GONE);
                        banner.setVisibility(View.VISIBLE);
                        if(isNotPlayedBigPic){
                            isNotPlayedBigPic = false;
                            playBanner();
                        }else {
                            banner.startScroll();
                        }


                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    /**播放视频*/
    private void playVideo(){
        isPlayVideo = true;
        banner.setVisibility(View.GONE);
        videoView.setVisibility(View.VISIBLE);

        video.setOnPreparedListener(mp -> {

        });
        video.setOnCompletionListener(mp -> {
            videoIndex++;
            if (videoIndex != videos.size()) {
                //继续播放视频
                playVideo();
            } else {
                //视频播放结束  开始播放图片  复位视频索引
                videoIndex = 0;
                //如果类型未全部是视频时接着循环
                if (images_big.size() == 0) {
                    playVideo();
                    return;
                }
                //判断视频下方的小图片是否播放完成，没有继续播放视频
                if(!isSmallPicFis){
                    playVideo();
                    isVideoAgain = true;
                    return;
                }
                isPlayVideo = false;
                isSmallPicFis = false;
                isVideoAgain = false;
                videoView.setVisibility(View.GONE);
                banner.setVisibility(View.VISIBLE);
                if(isNotPlayedBigPic){
                    isNotPlayedBigPic = false;
                    playBanner();
                }else {
                    banner.startScroll();
                }
            }
        });
        video.setOnErrorListener((mp, what, extra) -> {
            video.stopPlayback();
            return true;
        });
        video.setVideoPath(videos.get(videoIndex));
        video.start();
    }



    /**初始化banner数据*/
    private List<View> initBanner(List<String> urls) {
        List<View> bannerView = new ArrayList<View>();
        for (int i = 0; i < urls.size(); i++) {
            ImageView guidView = (ImageView) LayoutInflater.from(context).inflate(R.layout.item_image, null);
            ILFactory.getLoader().loadNet(guidView, urls.get(i), null);
            bannerView.add(guidView);
        }
        return bannerView;
    }

    /**重新获取数据*/
    @Override
    public boolean useEventBus() {
        BusProvider.getBus().toFlowable(EventModel.class).subscribe(
                eventModel -> {
                    XLog.e("EventModel===" + eventModel.value);
                    getP().getScreenData(false, mac,ipAddress);
                }
        );
        return true;
    }

    public static void launch(Activity activity) {
        Router.newIntent(activity)
                .to(TwoScreenActivity.class)
                .launch();
    }

    //记录用户首次点击返回键的时间
    private long firstTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                long secondTime = System.currentTimeMillis();
                if (secondTime - firstTime > 2000) {
                    ToastManager.showShort(context, "再按一次退出");
                    firstTime = secondTime;
                    return true;
                } else {
                   finish();
                }
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_two_screen;
    }

    @Override
    public TwoScreenPresent newP() {
        return new TwoScreenPresent();
    }

    public void toUpdateVer(String apkurl, String version){
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
    public void showError(NetError error) {
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
                    ToastManager.showShort(context, "请求失败");
                    break;
            }
        }
    }

}
