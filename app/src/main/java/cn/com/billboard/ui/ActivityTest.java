package cn.com.billboard.ui;

import android.app.smdt.SmdtManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import java.util.Timer;
import java.util.TimerTask;
import cn.com.billboard.R;
import cn.com.library.log.XLog;

public class ActivityTest extends AppCompatActivity{
    private SmdtManager smdt;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        XLog.e("sss",">>>>>>>>>>>>>>>>>>");
        smdt = SmdtManager.create(this);
        smdt.smdtWatchDogEnable((char)1);//开启看门狗
        new Timer().schedule(timerTask,0,5000);

    }

    TimerTask timerTask = new TimerTask(){
        @Override
        public void run() {
            smdt.smdtWatchDogFeed();//喂狗
                 XLog.e("sss",">>>>>>>>>>>>>>>>>>>喂狗");
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        smdt.smdtWatchDogEnable((char)0);
    }
}
