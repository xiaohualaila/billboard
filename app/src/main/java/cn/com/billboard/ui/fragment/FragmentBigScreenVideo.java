package cn.com.billboard.ui.fragment;

import android.widget.VideoView;

import java.util.List;

import butterknife.BindView;
import cn.com.billboard.R;
import cn.com.billboard.ui.main.MainActivity;
import cn.com.billboard.util.FileUtil;
import cn.com.billboard.util.UserInfoKey;

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
            MainActivity.instance().showError("没有要播放的视频和图片！");
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
                MainActivity.instance().toFragmentImg();
            }else {
                MainActivity.instance().showError("视频或图片不能为空！");
            }
        }
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
                    videoIndex = 0;
                    if(images.size()>0){
                        MainActivity.instance().toFragmentImg();
                    }else {
                        playVideo();
                    }
            }
        });
        video.setOnErrorListener((mp, what, extra) -> {
            MainActivity.instance().toFragmentImg();
            return true;
        });
        video.setVideoPath(videos.get(videoIndex));
        video.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        video.stopPlayback();
    }
}
