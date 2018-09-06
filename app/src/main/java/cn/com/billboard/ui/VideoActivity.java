package cn.com.billboard.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import butterknife.BindView;
import butterknife.OnClick;
import cn.com.billboard.R;
import cn.com.billboard.present.VideoPresent;
import cn.com.billboard.widget.AspectTextureView;
import cn.com.library.mvp.XActivity;
import cn.com.library.router.Router;

public class VideoActivity extends XActivity<VideoPresent> {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.surface_view)
    AspectTextureView surfaceView;

    @Override
    public void initData(Bundle savedInstanceState) {
        initToolbar();
    }

    /**
     * 设置title
     */
    private void initToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("调用摄像头录制视频");
        getP().initView(surfaceView);
        getP().onSetFilters();
    }

    @OnClick({R.id.start_video, R.id.stop_video})
    public void clickEvent(View view){
        switch (view.getId()) {
            case R.id.start_video:
                getP().startRecorder();
                break;
            case R.id.stop_video:
                getP().stopRecorder();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getP().onDestroy();
    }

    public static void launch(Activity activity) {
        Router.newIntent(activity)
                .to(VideoActivity.class)
                .launch();
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_video;
    }

    @Override
    public VideoPresent newP() {
        return new VideoPresent();
    }
}
