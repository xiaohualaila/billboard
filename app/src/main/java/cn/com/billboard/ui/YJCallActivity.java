package cn.com.billboard.ui;

import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.doormaster.vphone.config.DMCallState;
import com.doormaster.vphone.inter.DMModelCallBack.DMCallStateListener;
import com.doormaster.vphone.inter.DMVPhoneModel;

import butterknife.BindView;
import cn.com.billboard.R;

public class YJCallActivity extends Activity {

    private static String TAG = "DmCallActivity";
    private LinearLayout mTalkingLayout = null;
    private TextView textViewCallState = null;
    private TextView textViewCountDown = null;
    private SurfaceView mVideoView;
    private SurfaceView mCaptureView;
    private TimeCount time;
    private ImageView bottom_pic;
    private float mZoomFactor = 1.f;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_yj_call);

        DMVPhoneModel.addCallStateListener(callStateListener);

        bottom_pic = findViewById(R.id.bottom_pic);
        mVideoView = findViewById(R.id.videoSurface);
        mCaptureView =  findViewById(R.id.videoCaptureSurface);
        mCaptureView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        time = new TimeCount(60000, 1000);

        // set this flag so this activity will stay in front of the keyguard
        int flags = WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON;
        getWindow().addFlags(flags);

        DMVPhoneModel.fixZOrder(mVideoView, mCaptureView);
        mVideoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("YJCallActivity", "mVideoView onClick");
                if (mZoomFactor == 1.f) {
                    mZoomFactor = 1.5f;
                    DMVPhoneModel.zoomVideo(mZoomFactor, 0.5f, 0.5f);
                } else {
                    mZoomFactor = 1.f;
                    DMVPhoneModel.zoomVideo(mZoomFactor, 0.5f, 0.5f);
                }
            }
        });
        bottom_pic.setImageResource(R.drawable.police110);
    }



    @Override
    protected void onResume() {

        DMVPhoneModel.onVideoResume();
        DMVPhoneModel.addCallStateListener(callStateListener);
        String displayName = DMVPhoneModel.getDisplayName(this);
        Log.i("sss","displayName" + displayName);
        super.onResume();
    }

    @Override
    protected void onPause() {
        DMVPhoneModel.onVideoPause();
        DMVPhoneModel.removeCallStateListener(callStateListener);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        DMVPhoneModel.onVideoDestroy();
        stopCountDownTimer();
        super.onDestroy();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    private void decline() {
        DMVPhoneModel.refuseCall();
        finish();
    }





    private class TimeCount extends CountDownTimer {

        TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            // 计时过程
            String str = String.valueOf(millisUntilFinished / 1000);
            if (str.length() == 1) {
                str = "0" + str;
            }
            textViewCountDown.setText( "00:" +str );
        }

        @Override
        public void onFinish() {
            // 计时完毕
            decline();
        }
    }

    private void stopCountDownTimer()
    {
        if (time != null )
        {
            time.cancel();
        }
        time = null;
    }

    DMCallStateListener callStateListener = new DMCallStateListener() {
        @Override
        public void callState(DMCallState state, String message) {
            Log.d("CallStateLis calling","value=" + state.value() + ",message=" + message);
            if (state == DMCallState.Connected|| state == DMCallState.OutgoingRinging) {
                mTalkingLayout.setVisibility(View.VISIBLE);
                textViewCallState.setText(R.string.calling);
                time.start();

            } else if (state == DMCallState.StreamsRunning) {
            } else {
                if (state == DMCallState.Error) {
                    //通话结束
                    finish();
                } else if (state == DMCallState.CallEnd) {
                    //通话结束
                    finish();
                }
            }
        }
    };
}
