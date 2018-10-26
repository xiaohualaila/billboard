package cn.com.billboard.ui;

import android.content.Intent;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;
import java.io.File;
import java.util.Date;
import butterknife.BindView;
import cn.com.billboard.R;
import cn.com.billboard.model.EventRecordVideoModel;
import cn.com.billboard.net.UserInfoKey;
import cn.com.billboard.present.RecordvideoScreenPresent;
import cn.com.billboard.util.MyUtil;
import cn.com.library.event.BusProvider;
import cn.com.library.mvp.XActivity;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class RecordvideoActivity  extends XActivity<RecordvideoScreenPresent> implements SurfaceHolder.Callback {

    private static final String TAG = "RecordvideoActivity";

     @BindView(R.id.surfaceview)
     SurfaceView mSurfaceview;
     @BindView(R.id.text)
     TextView textView;

    private boolean mStartedFlg = false;//是否正在录像
    private MediaRecorder mRecorder;
    private SurfaceHolder mSurfaceHolder;
    private Camera camera;
    private MediaPlayer mediaPlayer;
    private String path;

    private int text = 0;
    private Date beginDate;
    private Date endDate;
    private Handler handler = new Handler();

    private String mac="";
    private int phoneType;
    @Override
    public void initData(Bundle savedInstanceState) {
        SurfaceHolder holder = mSurfaceview.getHolder();
        holder.addCallback(this);

        Intent intent = getIntent();
        phoneType = intent.getIntExtra("phoneType",0);
        mac = intent.getStringExtra("mac");
        mRecorder = new MediaRecorder();
        handler.postDelayed(() -> {
            startRecord();
            handler.postDelayed(runnable, 1000);
        },1000);

        BusProvider.getBus().toFlowable(EventRecordVideoModel.class).observeOn(AndroidSchedulers.mainThread()).subscribe(
                model -> {
                    if(!model.isCalling){
                        stopRecordVideo();
                        finish();
                    }
                }
        );
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            text++;
            textView.setText(text + "");
            handler.postDelayed(this, 1000);
        }
    };

    @Override
    public int getLayoutId() {
        return R.layout.activity_record_video;
    }

    @Override
    public RecordvideoScreenPresent newP() {
        return new RecordvideoScreenPresent();
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        mSurfaceHolder = surfaceHolder;
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
// 将holder，这个holder为开始在onCreate里面取得的holder，将它赋给mSurfaceHolder
        mSurfaceHolder = surfaceHolder;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mSurfaceview = null;
        mSurfaceHolder = null;
        handler.removeCallbacks(runnable);
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
            Log.d(TAG, "surfaceDestroyed release mRecorder");
        }
        if (camera != null) {
            camera.release();
            camera = null;
        }
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    /**
     * 录制视频
     */
    public void startRecord(){
        beginDate = new Date();
        if (mRecorder == null) {
            mRecorder = new MediaRecorder();
        }
         mRecorder.reset();
         camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
        if (camera != null) {
            camera.setDisplayOrientation(90);
            camera.unlock();
            mRecorder.setCamera(camera);
        }

        try {
            // 这两项需要放在setOutputFormat之前
            mRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
            mRecorder.setVideoSource(MediaRecorder.AudioSource.MIC);

            // Set output file format//设置文件输出格式
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

            // 这两项需要放在setOutputFormat之后
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);//设置音频编码方式
            mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);//设置视频编码方式

            mRecorder.setVideoSize(640, 480);//设置要拍摄的宽度和视频的高度。
            mRecorder.setVideoFrameRate(30);//设置录制视频的捕获帧速率。
            mRecorder.setVideoEncodingBitRate(3 * 1024 * 1024);//设置所录制视频的编码位率。
            mRecorder.setOrientationHint(270);//设置输出的视频播放的方向提示。
            //设置记录会话的最大持续时间（毫秒）
            mRecorder.setMaxDuration(30 * 1000);
            mRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());//设置使用哪个SurfaceView来显示视频预览。

                File dir = new File(UserInfoKey.RECORD_VIDEO_PATH);
                if (!dir.exists()) {
                    dir.mkdir();
                }
                path = dir + "/" + MyUtil.getDate() + ".mp4";
                mRecorder.setOutputFile(path);//设置录制的音频文件的保存位置。
                mRecorder.prepare();
                mRecorder.start();
                mStartedFlg = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopRecordVideo(){
        //stop
        endDate = new Date();
        if (mStartedFlg) {
            try {

                handler.removeCallbacks(runnable);
                if(mRecorder != null){
                    mRecorder.stop();
                    mRecorder.reset();
                    mRecorder.release();
                    mRecorder = null;
                }
                if (camera != null) {
                    camera.release();
                    camera = null;
                }
                File file = new File(path);
                if (!file.exists()) {
                    getP().uploadVideo(mac,beginDate,endDate,phoneType,"two",file);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mStartedFlg = false;
    }
}
