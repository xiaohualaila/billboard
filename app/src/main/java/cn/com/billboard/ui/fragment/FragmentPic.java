package cn.com.billboard.ui.fragment;


import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import java.util.ArrayList;
import java.util.List;
import butterknife.BindView;
import cn.com.billboard.R;
import cn.com.billboard.net.UserInfoKey;
import cn.com.billboard.ui.FragmentActivity;
import cn.com.billboard.util.FileUtil;
import cn.com.billboard.widget.BannersAdapter;
import cn.com.billboard.widget.BaseViewPager;
import cn.com.library.imageloader.ILFactory;

public class FragmentPic extends BaseFragment {

    @BindView(R.id.main_banner)
    BaseViewPager banner;
    private List<String> images_big;

    private Handler mHandler = new Handler();

    @Override
    public int getLayoutId() {
        return R.layout.fragment_big_pic;
    }

    @Override
    protected void init() {
        images_big = FileUtil.getFilePath(UserInfoKey.PIC_BIG_DOWM);
    }

    @Override
    public void onResume() {
        super.onResume();
        playBanner();
    }

    /**播放图片轮播*/
    private void playBanner(){
        banner.setAdapter(new BannersAdapter(initBanner(images_big)));
        banner.setIsOutScroll(true);
        banner.startScroll();
        banner.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == images_big.size() - 1) {
                    try {
                        FragmentActivity.instance().toFragemntMain();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        if(images_big.size()==1){
            mHandler.postDelayed(() -> backplay(),5000);
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if(!hidden){
            images_big = FileUtil.getFilePath(UserInfoKey.PIC_BIG_DOWM);
            if(images_big.size()==1){
                mHandler.postDelayed(() -> backplay(),5000);
            }
            banner.startScroll();
        }else {
            banner.stopScroll();
        }
        super.onHiddenChanged(hidden);
    }

    /**
     * 重复播放小图片和视频
     */
    private void backplay(){
        FragmentActivity.instance().toFragemntMain();
    }

    /**初始化banner数据*/
    private List<View> initBanner(List<String> urls) {
        List<View> bannerView = new ArrayList<View>();
        for (int i = 0; i < urls.size(); i++) {
            ImageView guidView = (ImageView) LayoutInflater.from(getActivity()).inflate(R.layout.item_image, null);
            ILFactory.getLoader().loadNet(guidView, urls.get(i), null);
            bannerView.add(guidView);
        }
        return bannerView;
    }
}
