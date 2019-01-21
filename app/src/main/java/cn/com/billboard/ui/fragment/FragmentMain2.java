package cn.com.billboard.ui.fragment;

import android.media.MediaPlayer;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import butterknife.BindView;
import cn.com.billboard.R;
import cn.com.billboard.ui.main.MainActivity;
import cn.com.billboard.util.FileUtil;
import cn.com.billboard.util.UserInfoKey;
import cn.com.billboard.widget.BannersAdapter;
import cn.com.billboard.widget.BaseViewPager;

/**
 * 采用MediaPlayer播放视频
 */

public class FragmentMain2 extends BaseFragment {
    @BindView(R.id.main_video)
    SurfaceView surfaceView;
    @BindView(R.id.pic_banner)
    BaseViewPager pic_banner;

    private List<String> images_small;
    private List<String> videos;
    private List<String> images_big;
    private int videoIndex = 0;
    private boolean pic_finish = false;
    private boolean video_finish = false;
    private MediaPlayer player;
    private SurfaceHolder holder;


    @Override
    public int getLayoutId() {
        return R.layout.fragment_main2;
    }

    @Override
    protected void init() {
        videos = FileUtil.getFilePath(UserInfoKey.VIDEO);
        images_small = FileUtil.getFilePath(UserInfoKey.PIC_SMALL_DOWN);
        images_big = FileUtil.getFilePath(UserInfoKey.PIC_BIG_DOWM);
        if (videos.size() == 0 || images_small.size() == 0) {
            MainActivity.instance().showError("视频或图片不能为空！");
            return;
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        videoIndex = 0;
        pic_finish = false;
        video_finish = false;
        playBanner();
        play();
    }

    private void play() {
        if(videos.size()==0){
            return;
        }
        player = new MediaPlayer();
        try {
            player.setDataSource(videos.get(videoIndex));
            holder = surfaceView.getHolder();
            holder.addCallback(new MyCallBack());
            player.prepare();
            player.setOnPreparedListener(mp -> player.start());
            player.setOnCompletionListener(mp -> {
                videoIndex++;
                // 在播放完毕被回调
                if (videoIndex != videos.size()) {
                    //继续播放视频
                    play();
                } else {
                    //视频播放结束  开始播放图片  复位视频索引
                    videoIndex = 0;
                    video_finish = true;
                    if (images_big.size() > 0 && pic_finish) {
                        MainActivity.instance().toFragemntBigPic();
                    } else {
                        play();
                    }
                }

            });
            player.setOnErrorListener((mp, what, extra) -> {
                MainActivity.instance().toFragemntBigPic();
                return false;
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class MyCallBack implements SurfaceHolder.Callback {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            player.setDisplay(holder);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }
    }


    /**
     * 播放图片轮播,小图片轮播
     */
    private void playBanner() {
        if(images_small.size() ==0){
            return;
        }
        pic_banner.setVisibility(View.VISIBLE);
        pic_banner.setAdapter(new BannersAdapter(initBanner(images_small)));
        pic_banner.setIsOutScroll(true);
        pic_banner.startScroll();
        pic_banner.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == images_small.size() - 1) {
                    pic_finish = true;
                    if (images_big.size() > 0 && video_finish) {
                        MainActivity.instance().toFragemntBigPic();
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        if (images_small.size() == 1) {
            pic_finish = true;
        }
    }


    /**
     * 初始化banner数据
     */
    private List<View> initBanner(List<String> urls) {
        List<View> bannerView = new ArrayList<View>();
        for (int i = 0; i < urls.size(); i++) {
            ImageView guidView = (ImageView) LayoutInflater.from(getActivity()).inflate(R.layout.item_image, null);
            Glide.with(this).load(urls.get(i)).into(guidView);
            bannerView.add(guidView);
        }
        return bannerView;
    }

    @Override
    public void onPause() {
        super.onPause();
        pic_banner.stopScroll();
        if (player != null) {
            if(player.isPlaying()){
                player.stop();
            }
            player.release();
            player = null;
        }
    }
}
