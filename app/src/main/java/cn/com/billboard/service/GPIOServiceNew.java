package cn.com.billboard.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.bjw.utils.FuncUtil;
import com.bjw.utils.SerialHelper;

import java.io.IOException;

import cn.com.billboard.event.BusProvider;
import cn.com.billboard.model.AlarmRecordModel;
import cn.com.billboard.model.EventMessageModel;
import cn.com.billboard.model.TipModel;
import cn.com.billboard.util.ChangeTool;
import cn.com.billboard.util.SharedPreferencesUtil;


//        Hold1 摘机，发送0x21， 挂机，发送0x20；
//        KEY1  按下，发送0x11， 弹起，发送0x12；
//        KEY2  按下，发送0x13， 弹起，发送0x14；
//
//        Hold2 摘机，发送0x31， 挂机，发送0x30；
//        KEY3  按下，发送0x15， 弹起，发送0x16；
//        KEY4  按下，发送0x17， 弹起，发送0x18；

public class GPIOServiceNew extends Service {

    private static GPIOServiceNew service;

    public static GPIOServiceNew getInstance() {
        if (service == null) {
            synchronized (GPIOServiceNew.class) {
                if (service == null) {
                    service = new GPIOServiceNew();
                }
            }
        }
        return service;
    }

    private Handler handler = new Handler();

    private SerialHelper serialHelper;
    String tell;
    String tel2;
    private boolean isCalling = false;

    @Override
    public void onCreate() {
        super.onCreate();
        service = this;
        init();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("TAG", "Services onStartCommand");
        return START_STICKY;
    }

    private void init() {

        serialHelper = new SerialHelper() {
            @Override
            protected void onDataReceived(final com.bjw.bean.ComBean comBean) {
                String back = FuncUtil.ByteArrToHex(comBean.bRec);
                Log.i("aaa", back);
                if (back.equals("11")) {
                    telephone1();
                } else if (back.equals("13")) {
                    telephone2();
                } else if (back.equals("20")) {//挂机
                    if (isCalling) {
                        stopCall();
                    }
                    BusProvider.getBus().post(new TipModel(false));
                } else if (back.equals("21")) {//摘机
                    BusProvider.getBus().post(new TipModel(true));
                }

                String back_phone = ChangeTool.decodeHexStr(back);
                Log.i("xxx", back_phone);
                if (back_phone.contains("NO CARRIER") || back_phone.contains("ERROR") || back_phone.contains("NO DIALTONE")) {
                    isCalling = false;
                    BusProvider.getBus().post(new AlarmRecordModel(false, 0));
                } else if (back_phone.contains("RING")) {//不予许接外来电话
                    sendTest("ATH\r\n");
                }
            }
        };
        serialHelper.setPort("/dev/ttyS3");
        serialHelper.setBaudRate(9600);
        try {
            serialHelper.close();
            serialHelper.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void telephone1() {
        if (isCalling) {
            return;
        }
        tell = SharedPreferencesUtil.getString(this, "tell", "");
        Log.i("sss", "tel1  " + tell);

        if (TextUtils.isEmpty(tell)) {
            BusProvider.getBus().post(new EventMessageModel("没有报警电话"));
        } else {
            isCalling = true;
            sendTest("ATD" + tell + ";\r\n");
            BusProvider.getBus().post(new AlarmRecordModel(true, 1));
        }
    }

    private void telephone2() {
        if (isCalling) {
            return;
        }
        tel2 = SharedPreferencesUtil.getString(this, "tel2", "");
        Log.i("sss", "tel2  " + tel2);
        if (TextUtils.isEmpty(tel2)) {
            BusProvider.getBus().post(new EventMessageModel("没有报警电话"));
        } else {
            isCalling = true;
            sendTest("ATD" + tel2 + ";\r\n");
            BusProvider.getBus().post(new AlarmRecordModel(true, 2));
        }
    }

    /**
     * 停止打电话
     */
    private void stopCall() {
        sendTest("ATH\r\n"); //挂断电话
        BusProvider.getBus().post(new AlarmRecordModel(false, 0));
        handler.postDelayed(() -> {
            isCalling = false;
            BusProvider.getBus().post(new AlarmRecordModel(false, 0));
            Log.i("sss", "再发一次停止信号");
        }, 2000);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //发送Test
    public void sendTest(String text) {
        if (serialHelper.isOpen()) {
            serialHelper.sendTxt(text);
        } else {
            Log.i("sss", "串口都没打开！");
        }
    }

    //发送Hex
    public void sendHex(String text) {
        if (serialHelper.isOpen()) {
            serialHelper.sendHex(text);
        } else {
            Log.i("sss", "串口都没打开！");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (serialHelper.isOpen()) {
            serialHelper.close();
        }
    }

}
