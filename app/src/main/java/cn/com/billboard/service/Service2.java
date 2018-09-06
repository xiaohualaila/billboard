package cn.com.billboard.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.cmm.rkadcreader.adcNative;
import com.cmm.rkgpiocontrol.rkGpioControlNative;
import com.decard.NDKMethod.BasicOper;


import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import ug.newopendoor.R;
import ug.newopendoor.rx.RxBus;
import ug.newopendoor.usbtest.ComBean;
import ug.newopendoor.usbtest.ConstUtils;
import ug.newopendoor.usbtest.M1CardListener;
import ug.newopendoor.usbtest.M1CardModel;
import ug.newopendoor.usbtest.MDSEUtils;
import ug.newopendoor.usbtest.SPUtils;
import ug.newopendoor.usbtest.SectorDataBean;
import ug.newopendoor.usbtest.SerialHelper;
import ug.newopendoor.usbtest.UltralightCardListener;
import ug.newopendoor.usbtest.UltralightCardModel;
import ug.newopendoor.usbtest.Utils;
import ug.newopendoor.util.ByteUtil;
import ug.newopendoor.util.SharedPreferencesUtil;
import ug.newopendoor.util.Ticket;


public class Service2 extends Service {
    private final int TIME = 300;

    private boolean isAuto = true;
    private static Lock lock = new ReentrantLock();

    private Thread thread;

    @Override
    public void onCreate() {
        super.onCreate();




    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isAuto = false;



    }

    Runnable task = new Runnable() {
        @Override
        public void run() {
            while (isAuto) {
                lock.lock();
                try {


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


}
