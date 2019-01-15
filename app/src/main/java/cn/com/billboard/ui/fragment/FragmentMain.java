package cn.com.billboard.ui.fragment;

import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.VideoView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;
import butterknife.BindView;
import cn.com.billboard.R;
import cn.com.billboard.ui.main.MainActivity;
import cn.com.billboard.util.FileUtil;
import cn.com.billboard.util.UserInfoKey;
import cn.com.billboard.widget.BannersAdapter;
import cn.com.billboard.widget.BaseViewPager;


public class FragmentMain extends BaseFragment {
    @BindView(R.id.main_video)
    VideoView video;
    @BindView(R.id.pic_banner)
    BaseViewPager pic_banner;

    private List<String> images_small;
    private List<String> videos;
    private List<String> images_big;
    private int videoIndex = 0;
    private boolean pic_finish = false;
    private boolean video_finish = false;
    @Override
    public int getLayoutId() {
        return R.layout.fragment_main;
    }

    @Override
    protected void init() {
        videos =  FileUtil.getFilePath(UserInfoKey.VIDEO);
        images_small = FileUtil.getFilePath(UserInfoKey.PIC_SMALL_DOWN);
        images_big = FileUtil.getFilePath(UserInfoKey.PIC_BIG_DOWM);
        if(videos.size()==0||images_small.size()==0){
            MainActivity.instance().showError("视频或图片不能为空！");
            return;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        playVideo();
        playBanner();
    }


    /**播放视频*/
    private void playVideo(){
        if(videos.size()==0){
            return;
        }
        video.setOnPreparedListener(mp -> { });
        video.setOnCompletionListener(mp -> {
            videoIndex++;
            if (videoIndex != videos.size()) {
                //继续播放视频
                playVideo();
            } else {
                //视频播放结束  开始播放图片  复位视频索引
                videoIndex = 0;
                video_finish = true;
                    if(images_big.size()>0 && pic_finish){
                        MainActivity.instance().toFragemntBigPic();
                    }else {
                        playVideo();
                    }
            }
        });
        video.setOnErrorListener((mp, what, extra) -> {
            video.stopPlayback();
            return true;
        });
        video.setVideoPath(videos.get(videoIndex));
        video.requestFocus();
        video.start();
    }

    /**播放图片轮播,小图片轮播*/
    private void playBanner(){
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
                if(position == images_small.size()-1){
                    pic_finish = true;
                    if(images_big.size()>0 && video_finish){
                        MainActivity.instance().toFragemntBigPic();
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        if(images_small.size()==1){
            pic_finish = true;
        }
    }


    /**初始化banner数据*/
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
        video.stopPlayback();
    }
}