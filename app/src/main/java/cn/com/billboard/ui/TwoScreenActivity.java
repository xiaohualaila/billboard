package cn.com.billboard.ui;

import android.app.Activity;
import android.app.smdt.SmdtManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import butterknife.BindView;
import cn.com.billboard.R;
import cn.com.billboard.model.EventModel;
import cn.com.billboard.model.EventRecordVideoModel;
import cn.com.billboard.model.ProgressModel;
import cn.com.billboard.net.UserInfoKey;
import cn.com.billboard.present.TwoScreenPresent;
import cn.com.billboard.service.GPIOService;
import cn.com.billboard.service.UpdateService;
import cn.com.billboard.util.AppPhoneMgr;
import cn.com.billboard.util.AppSharePreferenceMgr;
import cn.com.billboard.util.FileUtil;
import cn.com.billboard.widget.BannersAdapter;
import cn.com.billboard.widget.BaseViewPager;
import cn.com.library.event.BusProvider;
import cn.com.library.imageloader.ILFactory;
import cn.com.library.kit.ToastManager;
import cn.com.library.log.XLog;
import cn.com.library.mvp.XActivity;
import cn.com.library.net.NetError;
import cn.com.library.router.Router;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class TwoScreenActivity extends XActivity<TwoScreenPresent> {

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

    private List<String> images;

    private List<String> images_small;

    private List<String> images_big;
    private List<String> videos;

    DisplayManager displayManager;//屏幕管理类

    Display[]  displays;//屏幕数组

    int height = 0;

    private String file_name = "";
    private String  file_num = "";
    private int file_pre ;

    private Handler mHandler = new Handler();

    private boolean isUPdate = true;

    private SmdtManager smdt;

    private String mac = "";


    private boolean isVideoAgain = false;
    private boolean isSmallPicFis = false;
    @Override
    public void initData(Bundle savedInstanceState) {
        height = AppPhoneMgr.getInstance().getPhoneHeight(context);
        displayManager = (DisplayManager)context.getSystemService(Context.DISPLAY_SERVICE);
        displays = displayManager.getDisplays();


        rl_pro.setVisibility(View.VISIBLE);
        startService(new Intent(context, UpdateService.class));
        startService(new Intent(context, GPIOService.class));


        getP().getScreenData(true, AppSharePreferenceMgr.get(context, UserInfoKey.MAIN_SCREEN_IP, "").toString(),
                AppSharePreferenceMgr.get(context, UserInfoKey.SUB_SCREEN_IP, "").toString());
        images_big = new ArrayList<>();
        images_small = new ArrayList<>();

        BusProvider.getBus().toFlowable(ProgressModel.class).observeOn(AndroidSchedulers.mainThread()).subscribe(
                progressModel -> {

                    int pp = (int) ((float)progressModel.progress/(float)progressModel.total*100);
                    file_pre = pp;
                    file_num = progressModel.index+"/"+progressModel.num;
                    file_name= progressModel.type+progressModel.fileName;
                    if(isUPdate){
                        isUPdate = false;
                        mHandler.postDelayed(runnable,50);
                    }
                }
        );

        BusProvider.getBus().toFlowable(EventRecordVideoModel.class).observeOn(AndroidSchedulers.mainThread()).subscribe(
                model -> {
                    if(model.isCalling){
                     RecordvideoActivity.launch(this,mac,model.phoneType);
//                        OpenCVCameraActivity.launch(this,mac,model.phoneType);
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

            new Timer().schedule(timerTask,0,5000);
        }

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        showData();
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

    @Override
    protected void onResume() {
        super.onResume();
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
                    ToastManager.showShort(context, "其他异常");
                    break;
            }
        }
    }
    /**展示主屏数据*/
    public void showData() {
        mHandler.removeCallbacks(runnable);
        rl_pro.setVisibility(View.GONE);
        videos =  FileUtil.getFilePath(UserInfoKey.FILE_MAIN_VIDEO);
        images = FileUtil.getFilePath(UserInfoKey.FILE_MAIN_PICTURE);
        selectPic(images);

        if (images_big.size() > 0 && videos.size() > 0) {
            type = 3;
            playVideo();
        } else if (images_big.size() > 0) {
            type = 1;
            playBanner();
        } else if (videos.size() > 0) {
            type = 2;
            playVideo();
        } else {
            ToastManager.showShort(context, "暂无数据");
        }
    }

    public void showDownFile(){
        isUPdate = true;
        rl_pro.setVisibility(View.VISIBLE);
        videoView.setVisibility(View.GONE);
        banner.setVisibility(View.GONE);
    }


    private void selectPic(List<String> urls) {
        images_big = new ArrayList<>();
        images_small = new ArrayList<>();
        for (int i = 0; i < urls.size(); i++) {

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;//这个参数设置为true才有效，
            Bitmap bmp = BitmapFactory.decodeFile(urls.get(i), options);//这里的bitmap是个空
            if(bmp==null){
                Log.e("通过options获取到的bitmap为空","===");
            }

            float image_height = options.outHeight;
            float image_widht = options.outWidth;

            if(image_height/image_widht > 1.2){
                images_big.add(urls.get(i));
            }else {
                images_small.add(urls.get(i));
            }
        }
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
        isSmallPicFis = false;
        isVideoAgain = false;
        pic_banner.stopScroll();
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
                if (position == images_big.size() - 1 && type == 3) {
                    banner.stopScroll();
                    //图片播放完毕,休眠图片播放时长后播放视频
                    mHandler.postDelayed(() ->
                                    playVideo()
                            ,5000);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
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
               int n = position+1;
                if(n == images_small.size()){
                    isSmallPicFis = true;
                    if(isVideoAgain){//判断视频是否重复播放了,如果视频重复播放了，直接播放大图片
                        playBanner();
                    }
                }
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    /**播放视频*/
    private void playVideo(){
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
                if (type == 2) {
                    playVideo();
                    return;
                }
                //判断视频下方的小图片是否播放完成，没有继续播放视频
                if(!isSmallPicFis){
                    playVideo();
                    isVideoAgain = true;
                    return;
                }
                playBanner();
            }
        });
        video.setOnErrorListener((mp, what, extra) -> {
            video.stopPlayback();
            return true;
        });
        video.setVideoPath(videos.get(videoIndex));
        video.start();
        playDownPic();
    }

   private void  playDownPic(){
       if(images_small.size()>0){
           //底部图片滚动
           if(!isVideoAgain){//如果视频不否是重复播放的视频，是说明下面小图片没有播放完毕
               playSmallBanner();
           }
       }else {
           videoImg.setVisibility(View.VISIBLE);
           pic_banner.setVisibility(View.GONE);
       }
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
                    getP().getScreenData(false, AppSharePreferenceMgr.get(context, UserInfoKey.MAIN_SCREEN_IP, "").toString(),
                            AppSharePreferenceMgr.get(context, UserInfoKey.SUB_SCREEN_IP, "").toString());
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
}
