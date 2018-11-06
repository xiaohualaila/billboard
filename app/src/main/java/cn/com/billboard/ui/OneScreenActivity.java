package cn.com.billboard.ui;

import android.app.Activity;
import android.app.smdt.SmdtManager;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import cn.com.billboard.R;
import cn.com.billboard.dialog.DownloadDialog;
import cn.com.billboard.model.EventModel;
import cn.com.billboard.model.ProgressModel;
import cn.com.billboard.net.UserInfoKey;
import cn.com.billboard.present.OneScreenPresent;
import cn.com.billboard.util.AppSharePreferenceMgr;
import cn.com.billboard.util.FileUtil;
import cn.com.billboard.widget.BannersAdapter;
import cn.com.billboard.widget.BaseViewPager;
import cn.com.billboard.widget.MyVideoView;
import cn.com.library.event.BusProvider;
import cn.com.library.imageloader.ILFactory;
import cn.com.library.kit.ToastManager;
import cn.com.library.log.XLog;
import cn.com.library.mvp.XActivity;
import cn.com.library.net.NetError;
import cn.com.library.router.Router;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class OneScreenActivity extends XActivity<OneScreenPresent> {

    @BindView(R.id.big_video_view)
    View videoView;
    @BindView(R.id.big_video)
    MyVideoView video;
    @BindView(R.id.big_banner)
    BaseViewPager banner;

    private int type = 0, videoIndex = 0;

    //public LoadingDialog dialog;
    public DownloadDialog dialog;

    private List<String> images;

    private List<String> videos;
    private SmdtManager smdt;

    private String file_name = "";
    private String  file_num = "";
    private int file_pre ;
    private boolean isUPdate = true;
    private Handler mHandler = new Handler();
    @Override
    public void initData(Bundle savedInstanceState) {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        getWindow().setAttributes(params);
        dialog = new DownloadDialog(context);
        dialog.show();
        startService(new Intent(context, UpdateService.class));
        getP().getScreenData(AppSharePreferenceMgr.get(context, UserInfoKey.BIG_SCREEN_IP, "").toString(), true);
        String model = Build.MODEL;
        if(model.equals("3280")) {
            smdt = SmdtManager.create(this);
            smdt.smdtWatchDogEnable((char) 1);//开启看门狗
            new Timer().schedule(timerTask, 0, 5000);
        }
        BusProvider.getBus().toFlowable(ProgressModel.class).observeOn(AndroidSchedulers.mainThread()).subscribe(
                progressModel -> {
                    int pp = (int) ((float)progressModel.progress/(float)progressModel.total*100);
                    file_pre = pp;
                    file_num = progressModel.index+"/"+progressModel.num;
                    file_name= progressModel.type+progressModel.fileName;
                    //   Log.i("xxx"," 进度>>>>>>>>" + progressModel.progress +" 总进度>>>>>>>>" +progressModel.total+" progress>>>>>>>>" + pp );
                    if(isUPdate){
                        isUPdate = false;
                        mHandler.postDelayed(runnable,500);
                    }
                }
        );
    }

    Runnable runnable =  new Runnable() {
        @Override
        public void run()
        {
            dialog.getFile_name().setText(file_name);
            dialog.getFile_num().setText(file_num);
            dialog.getSeekBar().setProgress(file_pre);
            dialog.getNumProBar().setText(file_pre+"%");
            mHandler.postDelayed(runnable, 50);
        }
    };

    TimerTask timerTask = new TimerTask(){
        @Override
        public void run() {
            smdt.smdtWatchDogFeed();//喂狗
        }
    };

    /**请求返回错误*/
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

    public void showToastManger(String error){
        ToastManager.showShort(context, error);
    }


    /**展示数据*/
    public void showData() {
        videos =  FileUtil.getFilePath(UserInfoKey.FILE_BIG_VIDEO);
        images = FileUtil.getFilePath(UserInfoKey.FILE_BIG_PICTURE);
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

    /**播放图片轮播*/
    private void playBanner(){
        videoView.setVisibility(View.GONE);
        banner.setVisibility(View.VISIBLE);
        banner.setAdapter(new BannersAdapter(initBanner(images)));
        banner.setIsOutScroll(true);
        banner.startScroll();
        banner.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (position == images.size() - 1 && type == 3) {
                    banner.stopScroll();
                    mHandler.postDelayed(() -> playVideo(),10000);
                }
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
        video.setOnPreparedListener(mp -> mp.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
            @Override
            public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
//                        //FixMe 获取视频资源的宽度
//                        mVideoWidth = mp.getVideoWidth();
//                        //FixMe 获取视频资源的高度
//                        mVideoHeight = mp.getVideoHeight();
                XLog.e("mp.width== " + mp.getVideoWidth() + "\nmp.height====" + mp.getVideoHeight());
                video.onMeasureSize(mp.getVideoWidth(), mp.getVideoHeight());
            }
        }));
        video.setOnCompletionListener(mp -> {
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
        });
        video.setOnErrorListener((mp, what, extra) -> {
            video.stopPlayback();
            return true;
        });
        video.setVideoPath(videos.get(videoIndex));
        video.start();
    }
    /**初始化banner视图*/
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
                    getP().getScreenData(AppSharePreferenceMgr.get(context, UserInfoKey.BIG_SCREEN_IP, "").toString(), false);
                }
        );
        return true;
    }

    public static void launch(Activity activity) {
        Router.newIntent(activity)
                .to(OneScreenActivity.class)
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
        return R.layout.activity_one_screen;
    }

    @Override
    public OneScreenPresent newP() {
        return new OneScreenPresent();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(context, UpdateService.class));
        String model = Build.MODEL;
        if(model.equals("3280")) {
            smdt.smdtWatchDogEnable((char)0);
        }
    }

}
