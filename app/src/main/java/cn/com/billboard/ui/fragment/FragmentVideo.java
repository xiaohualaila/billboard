package cn.com.billboard.ui.fragment;

import android.annotation.SuppressLint;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import java.io.File;
import butterknife.BindView;
import cn.com.billboard.R;
import cn.com.billboard.model.AlarmRecordModel;
import cn.com.billboard.net.UserInfoKey;
import cn.com.billboard.ui.FragmentActivity;
import cn.com.billboard.util.AppSharePreferenceMgr;
import cn.com.billboard.util.FileUtil;
import cn.com.billboard.util.MyUtil;
import cn.com.library.event.BusProvider;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class FragmentVideo extends BaseFragment implements SurfaceHolder.Callback {
    private static final String TAG = "FragmentVideo";

    @BindView(R.id.surfaceview)
    SurfaceView mSurfaceview;
    @BindView(R.id.bottom_pic)
    ImageView bottom_pic;
    private boolean mStartedFlg = false;//是否正在录像
    private MediaRecorder mRecorder;
    private SurfaceHolder mSurfaceHolder;
    private Camera camera;
    private MediaPlayer mediaPlayer;
    private String path;

    private int text = 0;
    private Handler handler = new Handler();

    private int phoneType;
    private boolean isClose = false;//是否是因为挂断电话停止的视频

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_record_video;
    }

    @Override
    protected void init() {
        SurfaceHolder holder = mSurfaceview.getHolder();
        holder.addCallback(this);

        phoneType = 1;//1消防 2监督


        BusProvider.getBus().toFlowable(AlarmRecordModel.class).observeOn(AndroidSchedulers.mainThread()).subscribe(
                model -> {
                    if(!model.isCalling){
                        isClose = true;
                        stopRecordVideo();
                    }
                }
        );
        if(phoneType==1){
            bottom_pic.setImageResource(R.drawable.police110);
        }else {
            bottom_pic.setImageResource(R.drawable.police);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mRecorder = new MediaRecorder();
        handler.postDelayed(() -> {
            startRecord();
            handler.postDelayed(runnable, 1000);
        },500);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    /**
     * 当 Fragment 调用 hide() 、 show() 时回调
     * @param hidden
     */
    @Override
    public void onHiddenChanged(boolean hidden) {
        if(!hidden){
            if(phoneType==1){
                bottom_pic.setImageResource(R.drawable.police110);
            }else {
                bottom_pic.setImageResource(R.drawable.police);
            }
        }
        super.onHiddenChanged(hidden);
    }

    public FragmentVideo() {
    }

    @SuppressLint("ValidFragment")
    public FragmentVideo(int phoneType) {
        this.phoneType = phoneType;
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

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            text++;
            if(text==11){
                stopRecordVideo();
                return;
            }
            handler.postDelayed(this, 1000);
        }
    };

    /**
     * 录制视频
     */
    public void startRecord(){
        if (mRecorder == null) {
            mRecorder = new MediaRecorder();
        }
        mRecorder.reset();
        camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
        if (camera != null) {
            camera.setDisplayOrientation(0);
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
            mRecorder.setOrientationHint(0);//设置输出的视频播放的方向提示。
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
                if (file.exists()) {
                    AppSharePreferenceMgr.put(getActivity(),"videoFile",path);
                }else {
                    AppSharePreferenceMgr.put(getActivity(),"videoFile","");
                }
                FragmentActivity activity = (FragmentActivity ) getActivity();
                if(!isClose){
                    activity.toFragemntMain();
                }else {
                   Log.i("sss","sssssssssssssssssssss");
                    activity.toFragemntMain();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mStartedFlg = false;
    }


}



