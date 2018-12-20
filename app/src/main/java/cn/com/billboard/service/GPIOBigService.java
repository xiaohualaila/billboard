package cn.com.billboard.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.bjw.bean.ComBean;
import com.bjw.utils.FuncUtil;
import com.bjw.utils.SerialHelper;
import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import cn.com.billboard.model.AlarmRecordModel;
import cn.com.billboard.model.EventMessageModel;
import cn.com.billboard.util.ChangeTool;
import cn.com.billboard.util.GpioUtill;
import cn.com.billboard.util.SharedPreferencesUtil;
import cn.com.library.event.BusProvider;

public class GPIOBigService extends Service {

    private static GPIOBigService service;

    public static GPIOBigService getInstance() {
        if (service == null) {
            synchronized (GPIOBigService.class) {
                if (service == null) {
                    service = new GPIOBigService();
                }
            }
        }
        return service;
    }

    private String strCmd = "/sys/class/gpio_xrm/gpio";
    private int gpioNum = 5;//IO口电话5，监督6，消防7
    private final int TIME = 100;
    private boolean isAuto = true;
    private static Lock lock = new ReentrantLock();
    private Thread thread;
    private int gpioNum_ = 1;
    private boolean isCalling = false;
    String  strResult_5="";
    private SerialHelper serialHelper;
    String tell;
    String tel2;
    String tel3;
    String tel4;

    @Override
    public void onCreate() {
        super.onCreate();
        init();
        GpioUtill.executer("busybox echo " + 1 + " > " + strCmd + gpioNum_ + "/data");//打开sim800
        GpioUtill.executer( "cat " + strCmd + gpioNum_ + "/data");//判断是否打开sim800
        thread = new Thread(task);
        thread.start();
    }

    private void init() {
        serialHelper = new SerialHelper() {
            @Override
            protected void onDataReceived(final com.bjw.bean.ComBean comBean) {
                dealPhone(comBean);
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

    private void dealPhone(ComBean comBean) {
        String back= ChangeTool.decodeHexStr(FuncUtil.ByteArrToHex(comBean.bRec));
        if(back.contains("NO CARRIER")){
                isCalling = false;
        }else if(back.contains("RING")){//不予许接外来电话
                sendTest("ATH\r\n");
        }
        Log.i("sss", ChangeTool.decodeHexStr(FuncUtil.ByteArrToHex(comBean.bRec)));
    }

    Runnable task = () -> {
        while (isAuto) {
            lock.lock();
            String  strResult;

            try {
             if(gpioNum == 5){//挂上电话是0，拿下电话是 1
                   //电话
                 strResult_5 = GpioUtill.executer( "cat " + strCmd + gpioNum + "/data");
                 Log.i("sss","+++++++++++++ gpioNum ++"+ gpioNum +" "+ strResult_5);
                   if(strResult_5.equals("0")){
                       if(isCalling){
                            sendTest("ATH\r\n"); //挂断电话
                           isCalling = false;
                       }
                   }
                 gpioNum = 6;
             }else if(gpioNum == 6){
                 if(!isCalling){
                     //消防
                     strResult = GpioUtill.executer( "cat " + strCmd + gpioNum + "/data");
                         if(strResult.equals("0")){//打电话
                             if(strResult_5.equals("1")) {
                                 tell =  SharedPreferencesUtil.getString(this,"tell","");
                                 if(TextUtils.isEmpty(tell)){
                                     BusProvider.getBus().post(new EventMessageModel("没有报警电话"));
                                 }else {
                                     isCalling = true;
                                     sendTest("ATD"+tell+";\r\n");
                                     BusProvider.getBus().post(new AlarmRecordModel(true, 1));
                                 }
                             }
                         }
                     }
                 gpioNum = 7;
             }else if (gpioNum == 7){
                 //监督
                 if(!isCalling){
                     strResult = GpioUtill.executer( "cat " + strCmd + gpioNum + "/data");
                     if(strResult.equals("0")){
                         if(strResult_5.equals("1")){
                             tel2 =  SharedPreferencesUtil.getString(this,"tel2","");
                             if(TextUtils.isEmpty(tel2)){
                                 BusProvider.getBus().post(new EventMessageModel("没有报警电话"));
                             }else {
                                 isCalling = true;
                                 sendTest("ATD"+tel2+";\r\n");
                             }
                         }
                     }
                 }
                 gpioNum = 2;
             } else if(gpioNum == 2){//挂上电话是0，拿下电话是 1
                    //电话
                    strResult_5 = GpioUtill.executer( "cat " + strCmd + gpioNum + "/data");
                    Log.i("sss","+++++++++++++ gpioNum ++"+ gpioNum +" "+ strResult_5);
                    if(strResult_5.equals("0")){
                        if(isCalling){
                            sendTest("ATH\r\n"); //挂断电话
                            isCalling = false;
                        }
                    }
                    gpioNum = 3;
                }else if(gpioNum == 3){
                    if(!isCalling){
                        //消防
                        strResult = GpioUtill.executer( "cat " + strCmd + gpioNum + "/data");
                        if(strResult.equals("0")){//打电话
                            if(strResult_5.equals("1")) {
                                tel3 =  SharedPreferencesUtil.getString(this,"tel3","");
                                if(TextUtils.isEmpty(tel3)){
                                    BusProvider.getBus().post(new EventMessageModel("没有报警电话"));
                                }else {
                                    isCalling = true;
                                    sendTest("ATD"+tel3+";\r\n");
                                    BusProvider.getBus().post(new AlarmRecordModel(true, 1));
                                }
                            }
                        }
                    }
                    gpioNum = 4;
                }else {
                    //监督
                    if(!isCalling){
                        strResult = GpioUtill.executer( "cat " + strCmd + gpioNum + "/data");
                        if(strResult.equals("0")){
                            if(strResult_5.equals("1")){
                                tel2 =  SharedPreferencesUtil.getString(this,"tel4","");
                                if(TextUtils.isEmpty(tel4)){
                                    BusProvider.getBus().post(new EventMessageModel("没有报警电话"));
                                }else {
                                    isCalling = true;
                                    sendTest("ATD"+tel4+";\r\n");
                                }

                            }
                        }
                    }
                    gpioNum = 5;
                }
                Log.i("sss","+++++++++++++++"+gpioNum);
                Thread.sleep(TIME);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }
    };

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


    @Override
    public void onDestroy() {
        super.onDestroy();
        isAuto = false;
        GpioUtill.executer("busybox echo " + 0 + " > " + strCmd + gpioNum_ + "/data");//关闭sim800
        if(serialHelper.isOpen()){
            serialHelper.close();
        }
    }
}
