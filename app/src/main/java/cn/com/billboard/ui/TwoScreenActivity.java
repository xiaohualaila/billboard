package cn.com.billboard.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.VideoView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import cn.com.billboard.R;
import cn.com.billboard.model.EventModel;
import cn.com.billboard.net.UserInfoKey;
import cn.com.billboard.present.TwoScreenPresent;
import cn.com.billboard.service.UpdateService;
import cn.com.billboard.util.AppPhoneMgr;
import cn.com.billboard.util.AppSharePreferenceMgr;
import cn.com.billboard.util.GsonProvider;
import cn.com.billboard.widget.BannersAdapter;
import cn.com.billboard.widget.BaseViewPager;
import cn.com.billboard.widget.LoadingDialog;
import cn.com.library.event.BusProvider;
import cn.com.library.imageloader.ILFactory;
import cn.com.library.kit.ToastManager;
import cn.com.library.log.XLog;
import cn.com.library.mvp.XActivity;
import cn.com.library.net.NetError;
import cn.com.library.router.Router;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

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

    private int type = 0, videoIndex = 0;

    public LoadingDialog dialog;

    private List<String> images;

    private List<String> images_small;

    private List<String> videos;

    DisplayManager displayManager;//屏幕管理类

    Display[]  displays;//屏幕数组

    int height = 0;

    @Override
    public void initData(Bundle savedInstanceState) {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        getWindow().setAttributes(params);
        height = AppPhoneMgr.getInstance().getPhoneHeight(context);
        displayManager = (DisplayManager)context.getSystemService(Context.DISPLAY_SERVICE);
        displays = displayManager.getDisplays();
        dialog = new LoadingDialog(context, "数据加载中···");
        dialog.show();
        startService(new Intent(context, UpdateService.class));
        getP().readGpio();
        /**启动电话监听服务*/
//        Intent intent = new Intent(context, PhoneListenService.class);
//        intent.setAction(PhoneListenService.ACTION_REGISTER_LISTENER);
//        startService(intent);

        getP().getScreenData(true, AppSharePreferenceMgr.get(context, UserInfoKey.MAIN_SCREEN_IP, "").toString(),
                AppSharePreferenceMgr.get(context, UserInfoKey.SUB_SCREEN_IP, "").toString());
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
        images = GsonProvider.stringToList(AppSharePreferenceMgr.get(context, UserInfoKey.MAIN_PICTURE_FILE, "[]").toString(), String.class);
//        images_small = GsonProvider.stringToList(AppSharePreferenceMgr.get(context, UserInfoKey.MAIN_PICTURE_FILE_SMALL, "[]").toString(), String.class);
        videos = GsonProvider.stringToList(AppSharePreferenceMgr.get(context, UserInfoKey.MAIN_VIDEO_FILE, "[]").toString(), String.class);

        XLog.e("主屏图片===" + images);
        XLog.e("主屏视频===" + videos);
        if (images.size() > 0 && videos.size() > 0) {
            type = 3;
            playVideo();
        } else if (images.size() > 0) {
            type = 1;
            playBanner();
        } else if (videos.size() > 0) {
            type = 2;
            playVideo();
        } else {
            ToastManager.showShort(context, "暂无数据");
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

    /**video停止播放或者banner停止滚动*/
    public void stopPlayVideo(){
        if (videoView.getVisibility() == View.VISIBLE) {
            video.pause();
        } else {
            banner.stopScroll();
        }
    }

    /**video开始播放或者banner开始滚动*/
    public void startPlayVideo(){
        if (videoView.getVisibility() == View.VISIBLE) {
            video.start();
        } else {
            banner.startScroll();
        }
    }

    /**播放图片轮播*/
    private void playBanner(){
        pic_banner.stopScroll();
        videoView.setVisibility(View.GONE);
        banner.setVisibility(View.VISIBLE);
        banner.setAdapter(new BannersAdapter(initBanner(images)));
        banner.setIsOutScroll(true);
        banner.startScroll();
        banner.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == images.size() - 1 && type == 3) {
                    XLog.e("图片播放完毕,休眠图片播放时长后播放视频");
                    Observable.timer(10, TimeUnit.SECONDS, AndroidSchedulers.mainThread()).subscribe(new Observer<Long>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            XLog.e("开始倒计时");
                        }

                        @Override
                        public void onNext(Long value) {
                            playVideo();
                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onComplete() {
                            XLog.e("结束倒计时");
                        }
                    });
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
        pic_banner.setAdapter(new BannersAdapter(initBanner(images)));
        pic_banner.setIsOutScroll(true);
        pic_banner.startScroll();

    }

    /**播放视频*/
    private void playVideo(){
        banner.setVisibility(View.GONE);
        videoView.setVisibility(View.VISIBLE);
        ViewGroup.LayoutParams videoParams = video.getLayoutParams();
        XLog.e("video.getHeight====" + videoParams.height);
        ViewGroup.LayoutParams imgParams = videoImg.getLayoutParams();
        XLog.e("videoImg.getHeight====" + imgParams.height);
        ViewGroup.LayoutParams pic_banner_arams = pic_banner.getLayoutParams();
        XLog.e("videoImg.getHeight====" + pic_banner_arams.height);

        banner.stopScroll();
        //////////////////////////////
        if(images.size()>0){
            //底部图片滚动
            playSmallBanner();
        }else {
            videoImg.setVisibility(View.VISIBLE);
            pic_banner.setVisibility(View.GONE);
        }
         ////////////////////////

        video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {

            }
        });
        video.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                videoIndex++;
                if (videoIndex != videos.size()) {
                    //继续播放视频
                    playVideo();
                } else {
                    //视频播放结束  开始播放图片  复位视频索引
                    videoIndex = 0;
//                    mp.release();
                    //如果类型未全部是视频时接着循环
                    if (type == 2) {
                        playVideo();
                        return;
                    }
                    playBanner();
                }
            }
        });
        video.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                video.stopPlayback();
                return true;
            }
        });
        video.setVideoPath(videos.get(videoIndex));
        Log.i("sss",videos.get(videoIndex));
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
                new Consumer<EventModel>() {
                    @Override
                    public void accept(EventModel eventModel) throws Exception {
                        XLog.e("EventModel===" + eventModel.value);
                        getP().getScreenData(false, AppSharePreferenceMgr.get(context, UserInfoKey.MAIN_SCREEN_IP, "").toString(),
                                AppSharePreferenceMgr.get(context, UserInfoKey.SUB_SCREEN_IP, "").toString());
                    }
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
                    System.exit(0);
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
