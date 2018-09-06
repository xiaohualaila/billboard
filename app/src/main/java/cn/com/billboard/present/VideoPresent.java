package cn.com.billboard.present;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import cn.com.billboard.R;
import cn.com.billboard.net.UserInfoKey;
import cn.com.billboard.ui.VideoActivity;
import cn.com.billboard.util.AppDateMgr;
import cn.com.billboard.video.client.RecorderClient;
import cn.com.billboard.video.core.listener.IVideoChange;
import cn.com.billboard.video.filter.image.DrawMultiImageFilter;
import cn.com.billboard.video.filter.softaudiofilter.SetVolumeAudioFilter;
import cn.com.billboard.video.model.MediaConfig;
import cn.com.billboard.video.model.RecordConfig;
import cn.com.billboard.video.model.Size;
import cn.com.billboard.widget.AspectTextureView;
import cn.com.library.kit.ToastManager;
import cn.com.library.log.XLog;
import cn.com.library.mvp.XPresent;

public class VideoPresent extends XPresent<VideoActivity> {

    private RecorderClient mRecorderClient;

    private RecordConfig recordConfig;

    private AspectTextureView mTextureView;
    /**开始录制*/
    public void startRecorder() {
        mRecorderClient.startRecording();
    }
    /**停止录制*/
    public void stopRecorder() {
        mRecorderClient.stopRecording();
        ToastManager.showShort(getV(), "视频文件已保存");
    }
    /**初始化*/
    public void initView(AspectTextureView aspectTextureView) {
        this.mTextureView = aspectTextureView;
        mTextureView.setKeepScreenOn(true);
        mTextureView.setSurfaceTextureListener(surfaceTextureListener);
        mRecorderClient = new RecorderClient();
        recordConfig = RecordConfig.obtain();
//        recordConfig.setTargetVideoSize(new Size(640, 480));
        recordConfig.setSquare(true);
        recordConfig.setBitRate(750 * 1024);
        recordConfig.setVideoFPS(20);
        recordConfig.setVideoGOP(1);
        recordConfig.setRenderingMode(MediaConfig.Rending_Model_OpenGLES);
        //camera
        recordConfig.setDefaultCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
        int frontDirection, backDirection;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_FRONT, cameraInfo);
        frontDirection = cameraInfo.orientation;
        Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, cameraInfo);
        backDirection = cameraInfo.orientation;
        if (getV().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            recordConfig.setFrontCameraDirectionMode((frontDirection == 90 ? MediaConfig.DirectionMode.FLAG_DIRECTION_ROATATION_270 : MediaConfig.DirectionMode.FLAG_DIRECTION_ROATATION_90) | MediaConfig.DirectionMode.FLAG_DIRECTION_FLIP_HORIZONTAL);
            recordConfig.setBackCameraDirectionMode((backDirection == 90 ? MediaConfig.DirectionMode.FLAG_DIRECTION_ROATATION_90 : MediaConfig.DirectionMode.FLAG_DIRECTION_ROATATION_270));
        } else {
            recordConfig.setBackCameraDirectionMode((backDirection == 90 ? MediaConfig.DirectionMode.FLAG_DIRECTION_ROATATION_0 : MediaConfig.DirectionMode.FLAG_DIRECTION_ROATATION_180));
            recordConfig.setFrontCameraDirectionMode((frontDirection == 90 ? MediaConfig.DirectionMode.FLAG_DIRECTION_ROATATION_180 : MediaConfig.DirectionMode.FLAG_DIRECTION_ROATATION_0) | MediaConfig.DirectionMode.FLAG_DIRECTION_FLIP_HORIZONTAL);
        }
        //save video
        File folder = new File(UserInfoKey.BILLBOARD_VIDEO_PATH);
        if (!folder.exists()){
            folder.mkdirs();
        }
        recordConfig.setSaveVideoPath(UserInfoKey.BILLBOARD_VIDEO_PATH + "/" + AppDateMgr.todayYyyyMmDdHhMmSsTrim() + ".mp4");

        if (!mRecorderClient.prepare(getV(), recordConfig)) {
            mRecorderClient = null;
            Log.e("RecordingActivity", "prepare,failed!!");
            ToastManager.showShort(getV(), "StreamingClient prepare failed");
            getV().finish();
            return;
        }
        //resize textureview
        Size s = mRecorderClient.getVideoSize();
        mTextureView.setAspectRatio(AspectTextureView.MODE_INSIDE, ((double) s.getWidth()) / s.getHeight());
        mRecorderClient.setVideoChangeListener(iVideoChange);
        mRecorderClient.setSoftAudioFilter(new SetVolumeAudioFilter());
    }
    /**设置水印*/
    public void onSetFilters() {
        ArrayList<DrawMultiImageFilter.ImageDrawData> infos = new ArrayList<>();
        DrawMultiImageFilter.ImageDrawData data = new DrawMultiImageFilter.ImageDrawData();
//        data.resId = R.drawable.t;
        data.rect = new Rect(0, 0, 420, 40);
        data.bitmap = textToBitmap();
        infos.add(data);
        mRecorderClient.setHardVideoFilter(new DrawMultiImageFilter(getV(), infos));
    }

    private Bitmap textToBitmap(){
        Bitmap bitmap = Bitmap.createBitmap(1080, 190, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.TRANSPARENT);
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setTextSize(60f);
        canvas.drawText("远洋科技是水印", 0f, 180f, paint);
        return bitmap;
    }

    public TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
            if (mRecorderClient != null) {
                mRecorderClient.startPreview(surfaceTexture, width, height);
            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
            if (mRecorderClient != null) {
                mRecorderClient.updatePreview(width, height);
            }
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            if (mRecorderClient != null) {
                mRecorderClient.stopPreview(true);
            }
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

        }
    };

    public IVideoChange iVideoChange = new IVideoChange() {
        @Override
        public void onVideoSizeChanged(int width, int height) {
            mTextureView.setAspectRatio(AspectTextureView.MODE_INSIDE, ((double) width) / height);
        }
    };


    public void onDestroy() {
        if (mRecorderClient != null) {
            mRecorderClient.destroy();
        }
    }

}
