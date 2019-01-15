package cn.com.billboard.ui.fragment;


import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import cn.com.billboard.R;
import cn.com.billboard.net.UserInfoKey;
import cn.com.billboard.ui.FragmentBigScreenActivity;
import cn.com.billboard.util.FileUtil;
import cn.com.billboard.widget.BannersAdapter;
import cn.com.billboard.widget.BaseViewPager;
import cn.com.library.imageloader.ILFactory;

public class FragmentBigScreenPic extends BaseFragment {

    @BindView(R.id.main_banner)
    BaseViewPager banner;
    private List<String> images_big;
    private List<String> videos;
    private Handler mHandler = new Handler();

    @Override
    public int getLayoutId() {
        return R.layout.fragment_big_screen_pic;
    }

    @Override
    protected void init() {
        images_big = FileUtil.getFilePath(UserInfoKey.PIC_BIG_IMAGE_DOWN);
        videos = FileUtil.getFilePath(UserInfoKey.BIG_VIDEO);
    }

    @Override
    public void onResume() {
        super.onResume();
        playBanner();
    }

    /**
     * 播放图片轮播
     */
    private void playBanner() {
        banner.setAdapter(new BannersAdapter(initBanner(images_big)));
        banner.setIsOutScroll(true);
        banner.startScroll();
        banner.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
            //    Log.i("sss", "图片播放完毕,休眠图片播放时长后播放视频 " + position);
                if (position == images_big.size() - 1) {
                    if(videos.size()>0){
                        mHandler.postDelayed(() -> backplay(), 2000);
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        if (images_big.size() == 1) {
            if(videos.size()>0){
                mHandler.postDelayed(() -> backplay(), 2000);
            }
        }
    }

    /**
     * 重复播放小图片和视频
     */
    private void backplay() {
        FragmentBigScreenActivity.instance().toFragmentVideo();
    }

    /**
     * 初始化banner数据
     */
    private List<View> initBanner(List<String> urls) {
        List<View> bannerView = new ArrayList<View>();
        for (int i = 0; i < urls.size(); i++) {
            ImageView guidView = (ImageView) LayoutInflater.from(getActivity()).inflate(R.layout.item_image, null);
            ILFactory.getLoader().loadNet(guidView, urls.get(i), null);
            bannerView.add(guidView);
        }
        return bannerView;
    }

    @Override
    public void onPause() {
        super.onPause();
        banner.stopScroll();
    }
}
