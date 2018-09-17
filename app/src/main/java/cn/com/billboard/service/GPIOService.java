package cn.com.billboard.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.bjw.utils.FuncUtil;
import com.bjw.utils.SerialHelper;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import cn.com.billboard.util.ChangeTool;

public class GPIOService extends Service {

    private String strCmd = "/sys/class/gpio_xrm/gpio";
    private int gpioNum = 5;//IO口电话5，监督7，消防6
    private final int TIME = 200;
    private boolean isAuto = true;
    private static Lock lock = new ReentrantLock();

    private Thread thread;


    private int gpioNum_ = 1;
    private String isScanPhone = "0";

    private boolean isCalling = false;
    private int send_type = 0;

    String  strResult_5="";
    private SerialHelper serialHelper;
    @Override
    public void onCreate() {
        super.onCreate();

        iniview();
        executer("busybox echo " + 1 + " > " + strCmd + gpioNum_ + "/data");//打开sim800
        isScanPhone = executer( "cat " + strCmd + gpioNum_ + "/data");//判断是否打开sim800
        thread = new Thread(task);
        thread.start();

    }

    private void iniview() {
        serialHelper = new SerialHelper() {
            @Override
            protected void onDataReceived(final com.bjw.bean.ComBean comBean) {
                String back= ChangeTool.decodeHexStr(FuncUtil.ByteArrToHex(comBean.bRec));
                if(back.contains("NO CARRIER")){
                    isCalling = false;
                }else if(back.contains("RING")){//不予许接外来电话
                    sendTest("ATH\r\n");
                }

    //           Log.i("sss", ChangeTool.decodeHexStr(FuncUtil.ByteArrToHex(comBean.bRec)));

            }
        };
        serialHelper.setPort("/dev/ttyS3");
        serialHelper.setBaudRate(9600);

        try {
            serialHelper.close();
            serialHelper.open();

        } catch (IOException e) {
            e.printStackTrace();
    //        Log.i("sss",e.getMessage());
            //Toast.makeText(getBaseContext(),"串口打开失败", Toast.LENGTH_SHORT).show();
        }
    }


    Runnable task = () -> {
        while (isAuto) {
            lock.lock();
            String  strResult="";

            try {
             if(gpioNum == 5){//挂上电话是0，拿下电话是 1
                   //电话
                 strResult_5 = executer( "cat " + strCmd + gpioNum + "/data");
                   if(strResult_5.equals("0")){
                       if(isCalling){
                           sendTest("ATH\r\n"); //挂断电话
                           send_type = 0;
                           isCalling = false;
                       }
                   }
                Log.i("sss","当前gpioNum5是 "+strResult);
                 gpioNum = 7;
             }else if(gpioNum == 7){
                 if(!isCalling){
                     //消防
                     strResult = executer( "cat " + strCmd + gpioNum + "/data");
                         if(strResult.equals("0")){//打电话
                             if(strResult_5.equals("1")) {
                                 sendTest("ATD17682301987;\r\n");
                                 send_type = 1;
                                 isCalling = true;
                             }
                         }
                     }
     //            Log.i("sss","当前gpioNum7是 "+strResult);
                 gpioNum = 6;
             }else {
                 //监督
                 if(!isCalling){
                     strResult = executer( "cat " + strCmd + gpioNum + "/data");
                     if(strResult.equals("0")){
                         if(strResult_5.equals("1")){
                             sendTest("ATD17682301987;\r\n");
                             send_type = 2;
                             isCalling = true;
                         }
                     }
                 }
    //             Log.i("sss","当前gpioNum6是 "+strResult);
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

        serialHelper.sendTxt(text);
    }

    //发送Hex
    public void sendHest(String text){
        serialHelper.sendHex(text);
    }

    private String executer(String command) {

        StringBuffer output = new StringBuffer();
        Process process = null;
        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(command + "\n");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = "";
            while ((line = reader.readLine())!= null) {
                output.append(line + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (os != null)
                    os.close();
                process.destroy();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (output.toString().equals(""))
        {
            return "";
        }
        String response = output.toString().trim().substring(0, output.length() - 1);
        return response;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isAuto = false;
        executer("busybox echo " + 0 + " > " + strCmd + gpioNum_ + "/data");//关闭sim800
        serialHelper.close();
    }




}
