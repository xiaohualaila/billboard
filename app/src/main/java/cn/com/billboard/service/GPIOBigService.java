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
    private boolean isCalling1 = false;
    private boolean isCalling2 = false;
    String  strResult_5="";
    String  strResult_2="";
    private SerialHelper serialHelper;
    String tell;
    String tel2;
    String tel3;
    String tel4;

    @Override
    public void onCreate() {
        super.onCreate();
        service = this;
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
            isCalling1 =false;
            isCalling2 = false;
        }else if(back.contains("RING")){//不予许接外来电话
                sendTest("ATH\r\n");
        }else if(back.contains("ERROR")){
            isCalling1 = false;
            isCalling2 = false;
        }
        Log.i("xxx", ChangeTool.decodeHexStr(FuncUtil.ByteArrToHex(comBean.bRec)));
    }

    Runnable task = () -> {
        while (isAuto) {
            lock.lock();
            String  strResult;

            try {
             if(gpioNum == 5){//1表示可以打电话，0表示不能打
                   //电话
                 strResult_5 = GpioUtill.executer( "cat " + strCmd + gpioNum + "/data");
                // Log.i("xxx","+++++++++++++ gpioNum ++  "+ gpioNum +" strResult_5  "+ strResult_5);
                   if(strResult_5.equals("0")){
                       if(isCalling1 && !isCalling2){
                            sendTest("ATH\r\n"); //挂断电话
                           isCalling1 = false;
                       }
                   }
                 gpioNum = 6;
             }else if(gpioNum == 6){//KEY2 IO6 物业 tell
                 if(!isCalling1 && !isCalling2){
                     strResult = GpioUtill.executer( "cat " + strCmd + gpioNum + "/data");
                  //   Log.i("sss","+++++++++++++ gpioNum ++  "+ gpioNum +" strResult  "+ strResult);
                         if(strResult.equals("0")){//打电话
                             if(strResult_5.equals("1")) {
                                 tell =  SharedPreferencesUtil.getString(this,"tel1","");
                                 if(TextUtils.isEmpty(tell)){
                                     BusProvider.getBus().post(new EventMessageModel("没有报警电话"));
                                 }else {
                                     isCalling1 = true;
                                     sendTest("ATD"+tell+";\r\n");
                                     BusProvider.getBus().post(new AlarmRecordModel(true, 1));
                                 }
                             }
                         }
                     }
                 gpioNum = 7;
             }else if (gpioNum == 7){//key io7 物业 tel2
                 //监督
                 if(!isCalling1 && !isCalling2){
                     strResult = GpioUtill.executer( "cat " + strCmd + gpioNum + "/data");
                   //  Log.i("sss","+++++++++++++ gpioNum ++  "+ gpioNum +" strResult  "+ strResult);
                     if(strResult.equals("0")){
                         if(strResult_5.equals("1")){
                             tel2 =  SharedPreferencesUtil.getString(this,"tel2","");
                             if(TextUtils.isEmpty(tel2)){
                                 BusProvider.getBus().post(new EventMessageModel("没有报警电话"));
                             }else {
                                 isCalling1 = true;
                                 sendTest("ATD"+tel2+";\r\n");
                                 BusProvider.getBus().post(new AlarmRecordModel(true, 2));
                             }
                         }
                     }
                 }
                 gpioNum = 2;
             } else if(gpioNum == 2){//挂上电话是0，拿下电话是 1
                 strResult_2 = GpioUtill.executer( "cat " + strCmd + gpioNum + "/data");
                // Log.i("xxx","+++++++++++++ gpioNum ++  "+ gpioNum +" strResult_2  "+ strResult_2);
                    if(strResult_2.equals("0")){
                        if(!isCalling1 && isCalling2){
                                sendTest("ATH\r\n"); //挂断电话
                                isCalling2 = false;
                        }
                    }
                    gpioNum = 3;
                }else if(gpioNum == 3){//KEY3 IO4
                    if(!isCalling2 && !isCalling1){//监督
                        strResult = GpioUtill.executer( "cat " + strCmd + gpioNum + "/data");
                      //  Log.i("sss","+++++++++++++ gpioNum ++  "+ gpioNum +" strResult  "+ strResult);
                        if(strResult.equals("0")){//打电话
                            if(strResult_2.equals("1")) {
                                tel3 =  SharedPreferencesUtil.getString(this,"tel3","");
                                if(TextUtils.isEmpty(tel3)){
                                    BusProvider.getBus().post(new EventMessageModel("没有报警电话"));
                                }else {
                                    isCalling2 = true;
                                    sendTest("ATD"+tel3+";\r\n");
                                    BusProvider.getBus().post(new AlarmRecordModel(true, 3));
                                }
                            }
                        }
                    }
                    gpioNum = 4;
                }else {
                    if(!isCalling2 && !isCalling1){//片警 tel4
                        strResult = GpioUtill.executer( "cat " + strCmd + gpioNum + "/data");
                      //  Log.i("sss","+++++++++++++ gpioNum ++  "+ gpioNum +" strResult  "+ strResult);
                        if(strResult.equals("0")){
                            if(strResult_2.equals("1")){
                                tel4 =  SharedPreferencesUtil.getString(this,"tel4","");
                                if(TextUtils.isEmpty(tel4)){
                                    BusProvider.getBus().post(new EventMessageModel("没有报警电话"));
                                }else {
                                    isCalling2 = true;
                                    sendTest("ATD"+tel4+";\r\n");
                                    BusProvider.getBus().post(new AlarmRecordModel(true, 4));
                                }
                            }
                        }
                    }
                    gpioNum = 5;
                }

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
