package cn.com.billboard.ui.fragment;

import android.media.MediaPlayer;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.io.IOException;
import java.util.List;
import butterknife.BindView;
import cn.com.billboard.R;
import cn.com.billboard.ui.main.MainActivity;
import cn.com.billboard.util.FileUtil;
import cn.com.billboard.util.UserInfoKey;

/**
 * 采用MediaPlayer播放视频
 */
public class FragmentMediaPlayer extends BaseFragment {
    @BindView(R.id.main_video)
    SurfaceView surfaceView;
    private List<String> videos;
    private List<String> images;
    private int videoIndex = 0;
    private MediaPlayer player;
    private SurfaceHolder holder;
    @Override
    public int getLayoutId() {
        return R.layout.fragment_mediaplayer;
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
            play();
        }else {
            if(images.size()>0){
                MainActivity.instance().toFragmentImg();
            }else {
                MainActivity.instance().showError("视频或图片不能为空！");
            }
        }
    }

    private void play() {
        player = new MediaPlayer();
        try {
            player.setDataSource(videos.get(videoIndex));
            holder = surfaceView.getHolder();
            holder.addCallback(new MyCallBack());
            player.prepare();
            player.setOnPreparedListener(mp -> player.start());
            player.setOnCompletionListener(mp -> {
                videoIndex++;
                if (videoIndex != videos.size()) {
                    //继续播放视频
                    play();
                } else {
                    videoIndex = 0;
                    if(images.size()>0){
                        MainActivity.instance().toFragmentImg();
                    }else {
                        play();
                    }
                }

            });
            player.setOnErrorListener((mp, what, extra) -> {
                MainActivity.instance().toFragmentImg();
                return true;
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

    @Override
    public void onPause() {
        super.onPause();
        if (player != null) {
            if(player.isPlaying()){
                player.stop();
            }
            player.release();
            player = null;
        }
    }
}
