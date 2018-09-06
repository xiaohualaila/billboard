package cn.com.billboard.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.InvalidParameterException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import cn.com.billboard.model.ComBean;
import cn.com.billboard.util.ChangeTool;
import cn.com.billboard.util.SerialPortHelper;
import cn.com.library.event.BusProvider;

public class GPIOService extends Service {

    private String strCmd = "/sys/class/gpio_xrm/gpio";
    private int gpioNum = 5;//IO口电话5，监督7，消防6
    private final int TIME = 300;
    private boolean isAuto = true;
    private static Lock lock = new ReentrantLock();

    private Thread thread;

    private SerialControl serialControlA;
    private StringBuffer stringBuffer;
    @Override
    public void onCreate() {
        super.onCreate();
        thread = new Thread(task);
        thread.start();

        serialControlA = new SerialControl();
        serialControlA.setPort("/dev/ttyS1");//836 rs232
        serialControlA.setBaudRate(9600);
        OpenComPort(serialControlA);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isAuto = false;
        CloseComPort(serialControlA);
    }

    Runnable task = new Runnable() {
        @Override
        public void run() {
            while (isAuto) {
                lock.lock();
                String  strResult="";
                try {
                 if(gpioNum == 5){
                       //电话
                       strResult = executer( "cat " + strCmd + gpioNum + "/data");
                       if(strResult.equals("1")){

                       }else {

                       }
                     Log.i("sss","当前gpioNum5是 "+strResult);
                     gpioNum = 7;
                 }else if(gpioNum == 7){
                     //监督
                       strResult = executer( "cat " + strCmd + gpioNum + "/data");
                     if(strResult.equals("0")){
                         sendPortData(serialControlA,"");
                     }
                     Log.i("sss","当前gpioNum7是 "+strResult);
                     gpioNum = 6;
                 }else {
                     //消防
                     strResult = executer( "cat " + strCmd + gpioNum + "/data");
                     if(strResult.equals("0")){

                     }
                     Log.i("sss","当前gpioNum6是 "+strResult);
                     gpioNum = 5;
                 }

                    Thread.sleep(TIME);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    lock.unlock();
                }
            }
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
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



    /**
     * 串口控制类
     */
    private class SerialControl extends SerialPortHelper {

        public SerialControl() {
            stringBuffer = new StringBuffer();
        }

        @Override
        protected void onDataReceived(ComBean ComRecData) {

                String call = ChangeTool.ByteArrToHex(ComRecData.bRec);
                if (call.contains("A1")) {
                    try {
                        //          AppPhoneMgr.callPhone(getV(), "18729903883");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
        }
     }


    //----------------------------------------------------打开串口
    private void OpenComPort(SerialPortHelper ComPort) {
        try {
            ComPort.open();
        } catch (SecurityException e) {
          //  BusProvider.getBus().post(new EventModel("打开串口失败:没有串口读/写权限!"));
        } catch (IOException e) {
            e.printStackTrace();
         //   BusProvider.getBus().post(new EventModel("打开串口失败:未知错误!"));
        } catch (InvalidParameterException e) {
           // BusProvider.getBus().post(new EventModel("打开串口失败:参数错误!"));
        }
    }


    //----------------------------------------------------串口发送
    private void sendPortData(SerialPortHelper ComPort, String sOut) {
        if (ComPort != null && ComPort.isOpen()) {
            ComPort.sendHex(sOut);
        }
    }

    //----------------------------------------------------关闭串口
    private void CloseComPort(SerialPortHelper ComPort) {
        if (ComPort != null) {
            ComPort.stopSend();
            ComPort.close();
        }
    }
}
