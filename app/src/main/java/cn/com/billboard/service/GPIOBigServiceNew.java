package cn.com.billboard.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.bjw.utils.FuncUtil;
import com.bjw.utils.SerialHelper;

import java.io.IOException;

import cn.com.billboard.model.AlarmRecordModel;
import cn.com.billboard.model.EventMessageModel;
import cn.com.billboard.util.ChangeTool;
import cn.com.billboard.util.SharedPreferencesUtil;
import cn.com.library.event.BusProvider;

//        Hold1 挂机，发送0x21， 摘机，发送0x20；
//        KEY1  按下，发送0x11， 弹起，发送0x12；
//        KEY2  按下，发送0x13， 弹起，发送0x14；
//
//        Hold2 挂机，发送0x31， 摘机，发送0x30；
//        KEY3  按下，发送0x15， 弹起，发送0x16；
//        KEY4  按下，发送0x17， 弹起，发送0x18；

public class GPIOBigServiceNew extends Service {

    private static GPIOBigServiceNew service;

    public static GPIOBigServiceNew getInstance() {
        if (service == null) {
            synchronized (GPIOBigServiceNew.class) {
                if (service == null) {
                    service = new GPIOBigServiceNew();
                }
            }
        }
        return service;
    }

    private SerialHelper serialHelper;
    String tell;
    String tel2;
    String tel3;
    String tel4;
    private boolean isCalling = false;
    @Override
    public void onCreate() {
        super.onCreate();
        service = this;
        init();
    }

    private void init() {
        serialHelper = new SerialHelper() {
            @Override
            protected void onDataReceived(final com.bjw.bean.ComBean comBean) {
                String back = FuncUtil.ByteArrToHex(comBean.bRec);
                Log.i("xxx",back);

                if(back.equals("21")){
                    stopCall();
                }else if (back.equals("11")){
                    telephone1();
                }else if(back.equals("13")){
                    telephone2();
                }

                if(back.equals("31")){
                    stopCall();
                }else if (back.equals("15")){
                    telephone3();
                }else if(back.equals("17")){
                    telephone4();
                }

                String back_phone = ChangeTool.decodeHexStr(back);
                Log.i("xxx",back_phone);
                if(back_phone.contains("NO CARRIER")||back_phone.contains("ERROR")||back_phone.contains("NO DIALTONE")){
                    isCalling =false;
                }else if(back_phone.contains("RING")){//不予许接外来电话
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
        if(isCalling){
            return;
        }
        tell =  SharedPreferencesUtil.getString(this,"tell","");
        Log.i("sss","tel1  "+tell);
        if(TextUtils.isEmpty(tell)){
            BusProvider.getBus().post(new EventMessageModel("没有报警电话"));
        }else {
            isCalling = true;
            sendTest("ATD"+tell+";\r\n");
            BusProvider.getBus().post(new AlarmRecordModel(true, 1));
        }
    }

    private void telephone2() {
        if(isCalling){
            return;
        }
        tel2 =  SharedPreferencesUtil.getString(this,"tel2","");
        Log.i("sss","tel2  "+tel2);
        if(TextUtils.isEmpty(tel2)){
            BusProvider.getBus().post(new EventMessageModel("没有报警电话"));
        }else {
            isCalling = true;
            sendTest("ATD"+tel2+";\r\n");
            BusProvider.getBus().post(new AlarmRecordModel(true, 2));
        }
    }

    private void telephone3() {
        if(isCalling){
            return;
        }
        tel3 =  SharedPreferencesUtil.getString(this,"tel3","");
        Log.i("sss","tel3  "+tel3);
        if(TextUtils.isEmpty(tel3)){
            BusProvider.getBus().post(new EventMessageModel("没有报警电话"));
        }else {
            isCalling = true;
            sendTest("ATD"+tel3+";\r\n");
            BusProvider.getBus().post(new AlarmRecordModel(true, 3));
        }
    }

    private void telephone4() {
        if(isCalling){
            return;
        }
        tel4 =  SharedPreferencesUtil.getString(this,"tel4","");
        Log.i("sss","tel4  "+tel4);
        if(TextUtils.isEmpty(tel4)){
            BusProvider.getBus().post(new EventMessageModel("没有报警电话"));
        }else {
            isCalling = true;
            sendTest("ATD"+tel4+";\r\n");
            BusProvider.getBus().post(new AlarmRecordModel(true, 4));
        }
    }

    /**
     * 停止打电话
     */
    private void stopCall() {
        sendTest("ATH\r\n"); //挂断电话
        isCalling = false;
        BusProvider.getBus().post(new AlarmRecordModel(false, 0));
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //发送Test
    public void sendTest(String text){
        if(serialHelper.isOpen()){
            serialHelper.sendTxt(text);
        }else {
            Log.i("sss","串口都没打开！");
        }
    }

    //发送Hex
    public void sendHex(String text){
        if(serialHelper.isOpen()){
            serialHelper.sendHex(text);
        }else {
            Log.i("sss","串口都没打开！");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(serialHelper.isOpen()){
            serialHelper.close();
        }
    }

}
