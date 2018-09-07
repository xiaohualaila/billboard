package cn.com.billboard.ui;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Presentation;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.com.billboard.App;
import cn.com.billboard.R;
import cn.com.billboard.net.UserInfoKey;
import cn.com.billboard.util.AppSharePreferenceMgr;
import cn.com.billboard.util.GsonProvider;
import cn.com.billboard.widget.BannersAdapter;
import cn.com.billboard.widget.BaseViewPager;
import cn.com.billboard.widget.MyVideoView;
import cn.com.library.kit.ToastManager;
import cn.com.library.log.XLog;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class SubScreenActivity extends Presentation {

    @BindView(R.id.sub_tip)
    TextView subTip;
    @BindView(R.id.sub_video_view)
    View videoView;
    @BindView(R.id.sub_video)
    MyVideoView video;
    @BindView(R.id.sub_banner)
    BaseViewPager banner;

    private int type = 0, videoIndex = 0;

    private List<String> images;

    private List<String> videos;

    public SubScreenActivity(Context outerContext, Display display) {
        super(outerContext, display);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_screen);
        ButterKnife.bind(this);
    }
    /**展示副屏数据*/
    public void showData() {
        images = GsonProvider.stringToList(AppSharePreferenceMgr.get(App.getContext(), UserInfoKey.SUB_PICTURE_FILE, "[]").toString(), String.class);
        videos = GsonProvider.stringToList(AppSharePreferenceMgr.get(App.getContext(), UserInfoKey.SUB_VIDEO_FILE, "[]").toString(), String.class);
        XLog.e("副屏图片===" + images);
        XLog.e("副屏视频===" + videos);
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
            subTip.setVisibility(View.VISIBLE);
            ToastManager.showShort(App.getContext(), "暂无数据");
        }
    }
    /**播放图片轮播*/
    private void playBanner(){
        subTip.setVisibility(View.GONE);
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
    /**播放视频*/
    private void playVideo(){
        subTip.setVisibility(View.GONE);
        banner.setVisibility(View.GONE);
        videoView.setVisibility(View.VISIBLE);
        banner.stopScroll();
//        startPropertyAnim();
//        video.setRotation(-90);
//        video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//            @Override
//            public void onPrepared(MediaPlayer mp) {
////                XLog.e("mp.width== " + mp.getVideoWidth() + "\nmp.height====" + mp.getVideoHeight());
////                video.onMeasureSize(mp.getVideoWidth(), mp.getVideoHeight());
//                mp.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
//                    @Override
//                    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
////                        //FixMe 获取视频资源的宽度
////                        mVideoWidth = mp.getVideoWidth();
////                        //FixMe 获取视频资源的高度
////                        mVideoHeight = mp.getVideoHeight();
//                        XLog.e("mp.width== " + mp.getVideoWidth() + "\nmp.height====" + mp.getVideoHeight());
////                        mp.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);
////                        video.getHolder().setFixedSize(mp.getVideoWidth(), mp.getVideoHeight());
//                        video.onMeasureSize(mp.getVideoWidth(), mp.getVideoHeight());
//
//                    }
//                });
//            }
//        });
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
        video.start();
    }
    /**初始banner视图*/
    private List<View> initBanner(List<String> urls) {
        List<View> bannerView = new ArrayList<View>();
        for (int i = 0; i < urls.size(); i++) {
            ImageView guidView = (ImageView) LayoutInflater.from(App.getContext()).inflate(R.layout.item_image, null);
            Matrix matrix = new Matrix();
            Bitmap bitmap = BitmapFactory.decodeFile(urls.get(i));
         //   matrix.setRotate(-90);// 设置旋转角度
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),bitmap.getHeight(), matrix, true);// 重新绘制Bitmap
            guidView.setImageBitmap(bitmap);
//            ILFactory.getLoader().loadNet(guidView, urls.get(i), null);
            bannerView.add(guidView);
        }
        return bannerView;
    }

    /**
     * 控制控件大小
     */
    public void setViewSize(View v, int width, int height) {
        ViewGroup.LayoutParams params = v.getLayoutParams();
        if (width != 0) params.width = width;
        if (height != 0) params.height = height;
        v.setLayoutParams(params);
    }

    // 动画实际执行
    private void startPropertyAnim() {
        // 第二个参数"rotation"表明要执行旋转
        // 0f -> 360f，从旋转360度，也可以是负值，负值即为逆时针旋转，正值是顺时针旋转。
        ObjectAnimator anim = ObjectAnimator.ofFloat(videoView, "rotation", 0f, -90f);

        // 动画的持续时间，执行多久？
        anim.setDuration(5000);

        // 回调监听
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (Float) animation.getAnimatedValue();
                XLog.d(value + "========value");
            }
        });

        // 正式开始启动执行动画
        anim.start();
    }

}
