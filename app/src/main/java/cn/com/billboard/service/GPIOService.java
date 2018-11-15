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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import cn.com.billboard.model.AlarmRecordModel;
import cn.com.billboard.model.EventMessageModel;
import cn.com.billboard.model.EventModel;
import cn.com.billboard.util.AppSharePreferenceMgr;
import cn.com.billboard.util.ChangeTool;
import cn.com.billboard.util.GpioUtill;
import cn.com.library.event.BusProvider;
import cn.com.library.log.XLog;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class GPIOService extends Service {

    private static GPIOService service;

    public static GPIOService getInstance() {
        if (service == null) {
            synchronized (GPIOService.class) {
                if (service == null) {
                    service = new GPIOService();
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
    private String isScanPhone = "0";

    private boolean isCalling = false;
    private int send_type = 0;

    String  strResult_5="";
    private SerialHelper serialHelper;
    private SerialHelper serialHelperLight;
    String tell;
    String tel2;

    @Override
    public void onCreate() {
        super.onCreate();
        init();
        GpioUtill.executer("busybox echo " + 1 + " > " + strCmd + gpioNum_ + "/data");//打开sim800
        isScanPhone = GpioUtill.executer( "cat " + strCmd + gpioNum_ + "/data");//判断是否打开sim800
        thread = new Thread(task);
        thread.start();
    }

    private void init() {
        serialHelper = new SerialHelper() {
            @Override
            protected void onDataReceived(final com.bjw.bean.ComBean comBean) {
                String back= ChangeTool.decodeHexStr(FuncUtil.ByteArrToHex(comBean.bRec));
                if(back.contains("NO CARRIER")){
                    stopCall();

                }else if(back.contains("RING")){//不予许接外来电话
                    sendTest("ATH\r\n");
                }
               Log.i("sss", ChangeTool.decodeHexStr(FuncUtil.ByteArrToHex(comBean.bRec)));
            }
        };
        serialHelper.setPort("/dev/ttyS3");
        serialHelper.setBaudRate(9600);

        serialHelperLight = new SerialHelper() {
            @Override
            protected void onDataReceived(final com.bjw.bean.ComBean comBean) {
            }
        };
        //控制警灯
        serialHelperLight.setPort("/dev/ttyXRM0");
        serialHelperLight.setBaudRate(9600);
        try {
            serialHelper.close();
            serialHelper.open();
            serialHelperLight.close();
            serialHelperLight.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 停止打电话
     */
    private void stopCall() {
        BusProvider.getBus().post(new AlarmRecordModel(false, 0));
        isCalling = false;
        sendHex("f1");
    }

    Runnable task = () -> {
        while (isAuto) {
            lock.lock();
            String  strResult="";

            try {
             if(gpioNum == 5){//挂上电话是0，拿下电话是 1
                   //电话
                 strResult_5 = GpioUtill.executer( "cat " + strCmd + gpioNum + "/data");
                 Log.i("sss","+++++++++++++++"+strResult_5);
                   if(strResult_5.equals("0")){
                       if(isCalling){
                            sendTest("ATH\r\n"); //挂断电话
                            stopCall();
                       }
                   }
                 gpioNum = 6;
             }else if(gpioNum == 6){
                 if(!isCalling){
                     //消防
                     strResult = GpioUtill.executer( "cat " + strCmd + gpioNum + "/data");
                         if(strResult.equals("0")){//打电话
                             if(strResult_5.equals("1")) {
                                 tell = (String) AppSharePreferenceMgr.get(this,"tell","");
                                 if(TextUtils.isEmpty(tel2)){
                                     BusProvider.getBus().post(new EventMessageModel("没有报警电话"));
                                 }else {
                                     sendTest("ATD"+tell+";\r\n");
                                     send_type = 1;
                                     BusProvider.getBus().post(new AlarmRecordModel(true, send_type));
                                     isCalling = true;
                                     sendHex("01");
                                 }
                             }
                         }
                     }
                 gpioNum = 7;
             }else {
                 //监督
                 if(!isCalling){
                     strResult = GpioUtill.executer( "cat " + strCmd + gpioNum + "/data");
                     if(strResult.equals("0")){
                         if(strResult_5.equals("1")){
                             tel2 = (String) AppSharePreferenceMgr.get(this,"tel2","");
                             if(TextUtils.isEmpty(tel2)){
                                 BusProvider.getBus().post(new EventMessageModel("没有报警电话"));
//                                 break;
                             }else {
                                 sendTest("ATD"+tel2+";\r\n");
                                 send_type = 2;
                                 BusProvider.getBus().post(new AlarmRecordModel(true, send_type));
                                 isCalling = true;
                                 sendHex("01");
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

    //发送Hex
    public void sendHex(String text){
        if(serialHelperLight.isOpen()){
            serialHelperLight.sendHex(text);
        }else {
            Log.i("sss","串口都没打开！");
        }
    }


    /**
     * 10分钟请求一次服务器更新数据
     */
    public void startTimer(){
        Observable.timer(10, TimeUnit.MINUTES, AndroidSchedulers.mainThread()).subscribe(new Observer<Long>() {

            @Override
            public void onSubscribe(Disposable d) {
                XLog.e("更新数据倒计时开始");
            }

            @Override
            public void onNext(Long value) {
                BusProvider.getBus().post(new EventModel("refreshData", "refreshData"));
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {
                XLog.e("倒计时结束，开始获取数据");
            }
        });
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        isAuto = false;
        GpioUtill.executer("busybox echo " + 0 + " > " + strCmd + gpioNum_ + "/data");//关闭sim800
        if(serialHelper.isOpen()){
            serialHelper.close();
        }
        if(serialHelperLight.isOpen()){
            serialHelperLight.close();
        }
    }
}
