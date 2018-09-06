package cn.com.billboard.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.TextureView;
import android.view.View;

import butterknife.BindView;
import butterknife.OnClick;
import cn.com.billboard.R;
import cn.com.billboard.present.CameraPresent;
import cn.com.library.mvp.XActivity;
import cn.com.library.router.Router;

public class CameraActivity extends XActivity<CameraPresent> {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.texture_view)
    TextureView textureView;

    @Override
    public void initData(Bundle savedInstanceState) {
        initToolbar();
        textureView.setSurfaceTextureListener(getP().listener);
    }

    /**
     * 设置title
     */
    private void initToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("调用摄像头拍照");
    }

    @OnClick({R.id.take_picture, R.id.take_video})
    public void clickEvent(View view){
        switch (view.getId()){
            case R.id.take_picture:
                getP().takeCamera();//拍照，保存图片
                break;
            case R.id.take_video:
                getP().takeVideo();//视频，保存视频
                break;
        }
    }

    public static void launch(Activity activity) {
        Router.newIntent(activity)
                .to(CameraActivity.class)
                .launch();
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_camera;
    }

    @Override
    public CameraPresent newP() {
        return new CameraPresent();
    }
}
