package cn.com.billboard.present;

import android.media.MediaPlayer;
import android.text.TextUtils;
import android.util.Log;

import com.blankj.utilcode.constant.TimeConstants;
import com.blankj.utilcode.util.TimeUtils;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.concurrent.TimeUnit;

import cn.com.billboard.R;
import cn.com.billboard.model.BaseBean;
import cn.com.billboard.model.ComBean;
import cn.com.billboard.net.BillboardApi;
import cn.com.billboard.net.UserInfoKey;
import cn.com.billboard.ui.OpenDoorActivity;
import cn.com.billboard.util.AppPhoneMgr;
import cn.com.billboard.util.AppSharePreferenceMgr;
import cn.com.billboard.util.ChangeTool;
import cn.com.billboard.util.SerialPortHelper;
import cn.com.library.encrpt.Base64Utils;
import cn.com.library.encrpt.TDESUtils;
import cn.com.library.kit.ToastManager;
import cn.com.library.log.XLog;
import cn.com.library.mvp.XPresent;
import cn.com.library.net.ApiSubscriber;
import cn.com.library.net.NetError;
import cn.com.library.net.XApi;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class OpenDoorPresent extends XPresent<OpenDoorActivity> {

    private MediaPlayer mediaPlayer;

    public String type;//二维码的业务类型

    public String villageId;//小区编号

    public String directionDoor;//朝向

    public String openDoor;//进出

    public String unitId;//单元号

    public String roomId;//房间号

    public String phoneNum;//业主电话号码

    public String phoneNum_Guest;//访客电话号码

    public long time = 0;

    private int doorNum = 0; //几号门

    private StringBuffer stringBuffer;

    private SerialControl serialControlA, serialControlB;//串口

    public SendData sendData; //发送获取数据指令线程

    private String openDoorLastData = "";

    /**
     * 播放音乐
     */
    public void startMusic() {
        mediaPlayer = MediaPlayer.create(getV(), R.raw.dingdong);
        mediaPlayer.start();
    }
    /**
     * 打开串口、获取数据
     */
    public void startOpenSerialPort() {
        serialControlA = new SerialControl();
        serialControlB = new SerialControl();
//        serialControlA.setPort("/dev/ttyS1");//3288E
        serialControlA.setPort("/dev/ttyS2");//836 rs232
        serialControlA.setBaudRate(9600);
        OpenComPort(serialControlA);
//        serialControlB.setPort("/dev/ttyS4");//3288E
        serialControlB.setPort("/dev/ttyS3");//836 ttl232
        serialControlB.setBaudRate(9600);
        OpenComPort(serialControlB);
        sendData = new SendData();
        sendData.start();
//        openDoor("001", 1);
    }

    /**
     * 解密数据、解析数据、
     */
    private void decryptData(String doorData, int num) {
        try {
            XLog.e("doorData====" + doorData);
            String data = new String(TDESUtils.decrypt(Base64Utils.decodeString2Byte(doorData), Base64Utils.decodeString2Byte("5kxi7J1zqHBAxAiwQ2GJwnVUH8JoFrqn")), "UTF-8");//身份证号
            XLog.e("data====" + data);//data 001,610103001,610103,001126,18392393600,00000000000,1532505747025
            String[] strings = data.split(",");
            type = strings[0];
            villageId = strings[1];
            unitId = strings[2];
            roomId = strings[3];
            phoneNum = strings[4];
            phoneNum_Guest = strings[5];
            time = Long.parseLong(strings[6]);
            XLog.e("time===" + time);
            directionDoor = AppSharePreferenceMgr.get(getV(), UserInfoKey.OPEN_DOOR_DIRECTION_ID, "").toString();
            long ls = TimeUtils.getTimeSpanByNow(time, TimeConstants.MIN);
            if (ls > 5) {
                windowTip("二维码失败，请刷新二维码");
            } else {
                checkIsOpenDoor(type, num);
            }
        } catch (Exception e) {
            e.printStackTrace();
            windowTip(num + "号门开门失败");
        }
    }

    /**
     * 判断是否开门
     */
    private void checkIsOpenDoor(String type, int num) {
        String village = AppSharePreferenceMgr.get(getV(), UserInfoKey.OPEN_DOOR_VILLAGE_ID, "").toString();
        String unit = AppSharePreferenceMgr.get(getV(), UserInfoKey.OPEN_DOOR_UNIT_ID, "").toString();
        String room = AppSharePreferenceMgr.get(getV(), UserInfoKey.OPEN_DOOR_ROOM_ID, "").toString();
        switch (type) {
            case "001":
                if (!TextUtils.isEmpty(village) && !TextUtils.isEmpty(unit) && !TextUtils.isEmpty(room)) {
                    if (village.equals(villageId) && unit.equals(unitId) && room.equals(roomId)) {
                        openDoor(type, num);
                    } else {
                        if (village.equals(villageId)) {
                            openDoor(type, num);
                        } else {
                            windowTip("资料匹配失败，请确认小区是否正确");
                        }
                    }
                } else {
                    getV().setAppendContent("主板未参数未设置，请设置主板参数");
                }
                break;
            case "002":
                if (!TextUtils.isEmpty(village)) {
                    if (village.equals(villageId)) {
                        openDoor(type, num);
                    } else {
                        if (village.equals(villageId)) {
                            openDoor(type, num);
                        } else {
                            getV().setAppendContent("资料匹配失败，请确认小区是否正确");
                        }
                    }
                } else {
                    getV().setAppendContent("主板未参数未设置，请设置主板参数");
                }
                break;
        }
    }
    /**开门代码*/
    private void openDoor(String type, int num) {
        /**六个的继电器*/
//        byte[] sendArr = new byte[1];//0xFF 打开继电器指令
//        sendArr[0] =  (byte) (num == 1 ? 0x01 : num == 2 ? 0x02  : num == 3 ? 0x03  : num == 4 ? 0x04  : num == 5 ? 0x05  :  0x06 );//单个打开
//        sendArr[0] =  (byte) 0xFF; //全部打开
//        byte[] sendArr_ = new byte[1];//0x00 复位继电器指令
//        sendArr_[0] = (byte) (num == 1 ? 0xF1 : num == 2 ? 0xF2  : num == 3 ? 0xF3  : num == 4 ? 0xF4  : num == 5 ? 0xF5  :  0xF6 ); //单个复位
//        sendArr_[0] = (byte) 0x00; //全部复位

            /**四个继电器的*/
        byte[] sendArr = new byte[5];//打开继电器指令

//        sendArr[0] = (byte) 0xFF;
//        sendArr[1] =  (byte) 0x01 ; //0x25全开
//        sendArr[2] =  0x01;
//        sendArr[3] =  (byte) 0x02;
//        sendArr[4] = (byte) 0xEE;

        sendArr[0] = (byte) 0xFF;
        sendArr[1] =  (byte) (num == 1 ? 0x01 : num == 2 ? 0x02  : num == 3 ? 0x03  : num == 4 ? 0x04 : 0x01 ); //0x25全开
        sendArr[2] =  0x01;
        sendArr[3] =  (byte) (num == 1 ? 0x02 : num == 2 ? 0x03  : num == 3 ? 0x04  : num == 4 ? 0x05 : 0x02 ); //0x26全开
        sendArr[4] = (byte) 0xEE;

        byte[] sendArr_ = new byte[5];//复位继电器指令

//        sendArr_[0] = (byte) 0xFF;
//        sendArr_[1] = (byte) ( 0x01 );//0x25全关
//        sendArr_[2] = 0x00;
//        sendArr_[3] = (byte) (0x01 );//0x25全关
//        sendArr_[4] = (byte) 0xEE;

        sendArr_[0] = (byte) 0xFF;
        sendArr_[1] = (byte) (num == 1 ? 0x01 : num == 2 ? 0x02  : num == 3 ? 0x03  : num == 4 ? 0x04 :  0x01 );//0x25全关
        sendArr_[2] = 0x00;
        sendArr_[3] = (byte) (num == 1 ? 0x01 : num == 2 ? 0x02  : num == 3 ? 0x03  : num == 4 ? 0x04 :  0x01 );//0x25全关
        sendArr_[4] = (byte) 0xEE;

        serialControlB.send(sendArr);//打开继电器
        Observable.timer(300, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread()).subscribe(new Observer<Long>() {
            @Override
            public void onSubscribe(Disposable d) {
                XLog.e("对继电器复位");
            }

            @Override
            public void onNext(Long value) {
                serialControlB.send(sendArr_);
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {
                ToastManager.showShort(getV(), type.trim().equals("001") ? "Success!开门成功！" : "预约开门成功");
                uploadLog(type, num);
            }
        });
    }
    /**上传门禁日志*/
    private void uploadLog(String type, int num){
        BillboardApi.getDataService().uploadLog(phoneNum,  phoneNum_Guest, villageId, unitId, roomId, directionDoor, num % 2 == 0 ? "进" : "出",
                "", "", "").compose(XApi.<BaseBean>getApiTransformer())
                .compose(XApi.<BaseBean>getScheduler())
                .compose(getV().<BaseBean>bindToLifecycle())
                .subscribe(new ApiSubscriber<BaseBean>() {
                    @Override
                    protected void onFail(NetError error) {
                        ToastManager.showShort(getV(), "上传日志失败！");
                    }

                    @Override
                    public void onNext(BaseBean model) {
                        if (model.isSuccess()) {
                            ToastManager.showShort(getV(), "上传日志成功！");
                        } else {
                            ToastManager.showShort(getV(), model.getDescribe());
                        }
                    }
                });
    }

    /**串口控制类*/
    private class SerialControl extends SerialPortHelper {

        public SerialControl(){
            stringBuffer = new StringBuffer();
        }

        @Override
        protected void onDataReceived(ComBean ComRecData) {
            if (ComRecData.sComPort.equals("/dev/ttyS4")){
                XLog.e(ComRecData.sComPort + "==============================" + ChangeTool.ByteArrToHex(ComRecData.bRec));
                String call = ChangeTool.ByteArrToHex(ComRecData.bRec);
                if (call.contains("A1")){
                    try {
                        AppPhoneMgr.callPhone(getV(), "18729903883");
//                        AppPhoneMgr.callPhone(getV(), "18291409525");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else if (ComRecData.sComPort.equals("/dev/ttyS2")) {
//            } else if (ComRecData.sComPort.equals("/dev/ttyS1")) {
                String returnHex = ChangeTool.ByteArrToHex(ComRecData.bRec).replace(" ", "");
                XLog.e(ComRecData.sComPort + "==============================" + returnHex);
                if (ComRecData.bRec.length > 8) {
                    stringBuffer.append(returnHex);
                    if (stringBuffer.toString().length() >= 212) {
                        doorNum = Integer.parseInt(stringBuffer.toString().substring(4, 6));
                        String openDoorData = stringBuffer.toString().substring(14, 206);
                        XLog.e("doorNum======" + doorNum + "openDoorData========" + openDoorData);
                        if (!openDoorData.equals(openDoorLastData)) {
                            openDoorLastData = openDoorData;
                            XLog.e((doorNum % 4 == 0 ? 4 : doorNum % 4) + "号门准备开门");
                            decryptData(ChangeTool.decodeHexStr(openDoorData), doorNum % 4 == 0 ? 4 : doorNum % 4);
                        }
                        stringBuffer.delete(0, stringBuffer.length());
                    }
                } else {
                    stringBuffer.delete(0, stringBuffer.length());
                }
            }
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

    //----------------------------------------------------打开串口
    private void OpenComPort(SerialPortHelper ComPort) {
        try {
            ComPort.open();
        } catch (SecurityException e) {
            ToastManager.showShort(getV(), "打开串口失败:没有串口读/写权限!");
        } catch (IOException e) {
            e.printStackTrace();
            ToastManager.showShort(getV(), "打开串口失败:未知错误!");
        } catch (InvalidParameterException e) {
            ToastManager.showShort(getV(), "打开串口失败:参数错误!");
        }
    }

    private void windowTip(String tip){
        getV().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ToastManager.showShort(getV(), tip);
            }
        });
    }
    /**退出页面销毁*/
    public void onDestroy(){
        if (mediaPlayer != null)
            mediaPlayer.release();
        if (sendData != null)
            sendData.destroy();
        CloseComPort(serialControlA);
        CloseComPort(serialControlB);
    }
    /**发送取值命令*/
    private class SendData extends Thread{
        @Override
        public void run() {
            while (true) {
                for (int i=0; i<5;i++) {
                    int j = i + 1;
                    Log.e("send","发送指令" + j);
                    sendPortData(serialControlA, "01330" + j + "2123000000000000000000000000000303000000000000060101001000000301010010000003" + (j == 1 ? "AF" : j == 2 ? "B0" : j == 3 ? "B1" : j == 4 ? "B2" : "B3") + "04");
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

}