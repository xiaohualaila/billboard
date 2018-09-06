package cn.com.billboard.present;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.util.Log;

import com.blankj.utilcode.constant.TimeConstants;
import com.blankj.utilcode.util.TimeUtils;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import cn.com.billboard.R;
import cn.com.billboard.model.AccessModel;
import cn.com.billboard.model.BaseBean;
import cn.com.billboard.model.ComBean;
import cn.com.billboard.net.BillboardApi;
import cn.com.billboard.net.UserInfoKey;
import cn.com.billboard.ui.AccessDoorActivity;
import cn.com.billboard.util.AppPhoneMgr;
import cn.com.billboard.util.AppSharePreferenceMgr;
import cn.com.billboard.util.ChangeTool;
import cn.com.billboard.util.GsonProvider;
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

public class AccessPresent extends XPresent<AccessDoorActivity> {

    private MediaPlayer mediaPlayer;

    private SerialControl serialControlA, serialControlB;//串口

    public SendData sendData; //发送获取数据指令线程

    private StringBuffer stringBuffer;

    private String openDoorLastData = "";

    public void initMusic(){
        mediaPlayer = new MediaPlayer();
        startMusic(1);
    }

    /**
     * 播放音乐
     */
    private void startMusic(int select) {
        mediaPlayer = new MediaPlayer();
        AssetFileDescriptor file = getV().getResources().openRawResourceFd(select == 1 ? R.raw.dingdong : select == 2 ? R.raw.success : R.raw.fail);
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(),
                    file.getLength());
            mediaPlayer.prepare();
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            if (System.currentTimeMillis() - Long.parseLong(strings[6]) > 1000 * 300) {
                windowTip("二维码失效，请刷新二维码");
                startMusic(3);
            } else {
                checkIsOpenDoor(strings, num);
                startMusic(2);
            }
        } catch (Exception e) {
            e.printStackTrace();
            windowTip(num + "号门开门失败");
        }
    }

    /**
     * 判断是否开门
     */
    private void checkIsOpenDoor(String[] strings, int num) {
        String village = AppSharePreferenceMgr.get(getV(), UserInfoKey.OPEN_DOOR_VILLAGE_ID, "").toString();
        String directionDoor = AppSharePreferenceMgr.get(getV(), UserInfoKey.OPEN_DOOR_DIRECTION_ID, "").toString();
        String building = AppSharePreferenceMgr.get(getV(), UserInfoKey.OPEN_DOOR_BUILDING, "").toString();
        String params = AppSharePreferenceMgr.get(getV(), UserInfoKey.OPEN_DOOR_PARAMS, "[]").toString();
        List<AccessModel> list = GsonProvider.stringToList(params, AccessModel.class);
        AccessModel model = null;
        if (list.size() > 0) {
            model = getModel(list, num);
        }
        if (!TextUtils.isEmpty(village) && !TextUtils.isEmpty(directionDoor) && list.size() > 0) {
            if (!TextUtils.isEmpty(building)) {
                if (village.equals(strings[1]) && ((building + model.getDoorNum()).equals(strings[2]))) {
                    openDoor(strings, model);
                }
            } else {
                if (village.equals(strings[1])) {
                    openDoor(strings, model);
                } else {
                    windowTip("资料匹配失败，请确认小区是否正确");
                }
            }
        } else {
            getV().setAppendContent("主板未参数未设置，请设置主板参数");
        }
    }

    /**
     * 开门代码
     */
    private void openDoor(String[] strings, AccessModel model) {
        int num = model.getRelay();
        /**六个的继电器*/
//        byte[] sendArr = new byte[1];//0xFF 打开继电器指令
//        sendArr[0] =  (byte) (num == 1 ? 0x01 : num == 2 ? 0x02  : num == 3 ? 0x03  : num == 4 ? 0x04  : num == 5 ? 0x05  :  0x06 );//单个打开
//        sendArr[0] =  (byte) 0xFF; //全部打开
//        byte[] sendArr_ = new byte[1];//0x00 复位继电器指令
//        sendArr_[0] = (byte) (num == 1 ? 0xF1 : num == 2 ? 0xF2  : num == 3 ? 0xF3  : num == 4 ? 0xF4  : num == 5 ? 0xF5  :  0xF6 ); //单个复位
//        sendArr_[0] = (byte) 0x00; //全部复位

        /**四个继电器的*/
        byte[] sendArr = new byte[5];//打开继电器指令
        sendArr[0] = (byte) 0xFF;
        sendArr[1] = (byte) (num == 1 ? 0x01 : num == 2 ? 0x02 : num == 3 ? 0x03 : num == 4 ? 0x04 : 0x01); //0x25全开
        sendArr[2] = 0x01;
        sendArr[3] = (byte) (num == 1 ? 0x02 : num == 2 ? 0x03 : num == 3 ? 0x04 : num == 4 ? 0x05 : 0x02); //0x26全开
        sendArr[4] = (byte) 0xEE;
        byte[] sendArr_ = new byte[5];//复位继电器指令
        sendArr_[0] = (byte) 0xFF;
        sendArr_[1] = (byte) (num == 1 ? 0x01 : num == 2 ? 0x02 : num == 3 ? 0x03 : num == 4 ? 0x04 : 0x01);//0x25全关
        sendArr_[2] = 0x00;
        sendArr_[3] = (byte) (num == 1 ? 0x01 : num == 2 ? 0x02 : num == 3 ? 0x03 : num == 4 ? 0x04 : 0x01);//0x25全关
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
                ToastManager.showShort(getV(), strings[0].trim().equals("001") ? "Success!开门成功！" : "预约开门成功");
                uploadLog(strings, model);
            }
        });
    }

    /**
     * 上传门禁日志
     */
    private void uploadLog(String[] strings, AccessModel model) {
        String directionDoor = AppSharePreferenceMgr.get(getV(), UserInfoKey.OPEN_DOOR_DIRECTION_ID, "").toString();
        BillboardApi.getDataService().uploadLog(strings[4], strings[5], strings[1], strings[2], strings[3], directionDoor, model.getAccessible(),
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

    /**
     * 获取当前扫码盒对应的数据
     *
     * @param list 扫码盒列表
     * @param door 扫码盒号
     * @return
     */
    private AccessModel getModel(List<AccessModel> list, int door) {
        AccessModel model = null;
        for (AccessModel accessModel : list) {
            if (accessModel.getErCode() == door) {
                model = accessModel;
                break;
            }
        }
        return model;
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
            if (ComRecData.sComPort.equals("/dev/ttyS4")) {
                String call = ChangeTool.ByteArrToHex(ComRecData.bRec);
                if (call.contains("A1")) {
                    try {
                        AppPhoneMgr.callPhone(getV(), "18729903883");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else if (ComRecData.sComPort.equals("/dev/ttyS2")) {
//            } else if (ComRecData.sComPort.equals("/dev/ttyS1")) {
                String returnHex = ChangeTool.ByteArrToHex(ComRecData.bRec).replace(" ", "");
                if (ComRecData.bRec.length > 8) {
                    stringBuffer.append(returnHex);
                    if (stringBuffer.toString().length() >= 212) {
                        int doorNum = Integer.parseInt(stringBuffer.toString().substring(4, 6));
                        String openDoorData = stringBuffer.toString().substring(14, 206);
                        if (!openDoorData.equals(openDoorLastData)) {
                            openDoorLastData = openDoorData;
                            decryptData(ChangeTool.decodeHexStr(openDoorData), doorNum);
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

    //添加Toast提示
    private void windowTip(String tip) {
        getV().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ToastManager.showShort(getV(), tip);
            }
        });
    }

    /**
     * 退出页面销毁
     */
    public void onDestroy() {
        if (mediaPlayer != null)
            mediaPlayer.release();
        if (sendData != null)
            sendData.destroy();
        CloseComPort(serialControlA);
        CloseComPort(serialControlB);
    }

    /**
     * 发送取值命令
     */
    private class SendData extends Thread {
        @Override
        public void run() {
            while (true) {
                String params = AppSharePreferenceMgr.get(getV(), UserInfoKey.OPEN_DOOR_PARAMS, "[]").toString();
                List<AccessModel> list = GsonProvider.stringToList(params, AccessModel.class);
                if (list.size() > 0) {
                    for (int i = 0; i < list.size(); i++) {
                        int j = i + 1;
                        sendPortData(serialControlA, ChangeTool.makeDataChecksum("01330" + j + "2123000000000000000000000000000303000000000000060101001000000301010010000003"));
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

}
