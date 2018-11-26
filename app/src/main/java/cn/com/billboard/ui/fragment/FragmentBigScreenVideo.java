package cn.com.billboard.ui.fragment;

import android.widget.VideoView;
import java.util.List;
import butterknife.BindView;
import cn.com.billboard.R;
import cn.com.billboard.net.UserInfoKey;
import cn.com.billboard.ui.FragmentBigScreenActivity;
import cn.com.billboard.util.FileUtil;

public class FragmentBigScreenVideo extends BaseFragment {
    @BindView(R.id.main_video)
    VideoView video;
    private List<String> videos;
    private List<String> images;
    private int videoIndex = 0;
    @Override
    public int getLayoutId() {
        return R.layout.fragment_big_screen_video;
    }

    @Override
    protected void init() {
        videos =  FileUtil.getFilePath(UserInfoKey.BIG_VIDEO);
        images = FileUtil.getFilePath(UserInfoKey.PIC_BIG_IMAGE_DOWN);
        if(videos.size()==0 && images.size()==0){
            FragmentBigScreenActivity.instance().showError("没有要播放的视频和图片！");
            return;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(videos.size()>0){
            playVideo();
        }else {
            if(images.size()>0){
                FragmentBigScreenActivity.instance().toFragmentImg();
            }else {
                FragmentBigScreenActivity.instance().showError("视频或图片不能为空！");
            }
        }
    }

    /**
     * 当 Fragment 调用 hide() 、 show() 时回调
     * @param hidden
     */
    @Override
    public void onHiddenChanged(boolean hidden) {
        if(!hidden){
            videoIndex = 0;
            playVideo();
        }
        super.onHiddenChanged(hidden);
    }

    /**播放视频*/
    private void playVideo(){
        video.setOnPreparedListener(mp -> {

        });
        video.setOnCompletionListener(mp -> {
            videoIndex++;
            if (videoIndex != videos.size()) {
                //继续播放视频
                playVideo();
            } else {
                    if(images.size()>0){
                        FragmentBigScreenActivity.instance().toFragmentImg();
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
        video.start();
    }

}
