package cn.com.billboard.present;

import java.util.Timer;
import java.util.TimerTask;

import cn.com.billboard.net.UserInfoKey;
import cn.com.billboard.ui.CreateParamsActivity;
import cn.com.billboard.ui.OneScreenActivity;
import cn.com.billboard.ui.TwoScreenActivity;
import cn.com.billboard.util.AppSharePreferenceMgr;
import cn.com.library.kit.ToastManager;
import cn.com.library.log.XLog;
import cn.com.library.mvp.XPresent;

public class ParamsPresent extends XPresent<CreateParamsActivity> {

    private int time = 5;

    private Timer timer = null;

    public void startTimer(){
        time = 5;
        timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                time--;
                XLog.e("time===" + time);
                if (time <= 0) {
                    timer.cancel();
                    toActivity();
                }
            }
        };
        timer.schedule(task, 0, 1000);

    }

    public void stopTimer(){
        if (timer != null) {
            timer.cancel();
            time = 5;
        }
    }

    private void toActivity(){
        int screenNum = (int) AppSharePreferenceMgr.get(getV(), UserInfoKey.SCREEN_NUM, -1);
        if (screenNum == 0) {
            TwoScreenActivity.launch(getV());
            getV().finish();
        } else if (screenNum == 1) {
            OneScreenActivity.launch(getV());
            getV().finish();
        } else {
            ToastManager.showShort(getV(), "设备类型未知");
        }
    }

}
