package cn.com.billboard.present;

import android.text.TextUtils;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.util.List;

import cn.com.billboard.model.BaseBean;
import cn.com.billboard.model.ScreenDataModel;
import cn.com.billboard.model.ScreenShowModel;
import cn.com.billboard.net.BillboardApi;
import cn.com.billboard.net.UserInfoKey;
import cn.com.billboard.service.UpdateService;
import cn.com.billboard.ui.TwoScreenActivity;
import cn.com.billboard.util.AppPhoneMgr;
import cn.com.billboard.util.AppSharePreferenceMgr;
import cn.com.billboard.util.DownloadFileUtil;
import cn.com.billboard.util.ReaderJsonUtil;
import cn.com.library.log.XLog;
import cn.com.library.mvp.XPresent;
import cn.com.library.net.ApiSubscriber;
import cn.com.library.net.NetError;
import cn.com.library.net.XApi;

public class TwoScreenPresent extends XPresent<TwoScreenActivity> {

    private ReadGpioState state;

    /**
     * 回调页面展示数据、启动及时服务、上报状态
     */
    CallBack callBack = new CallBack() {
        @Override
        public void onMainChangeUI() {
            getV().dialog.dismiss();
            getV().showData();
            UpdateService.getInstance().startTimer();
            updateState(AppSharePreferenceMgr.get(getV(), UserInfoKey.MAIN_SCREEN_ID, "").toString());
        }

        @Override
        public void onSubChangeUI() {
            getV().dialog.dismiss();
            getV().showSubData();
            updateState(AppSharePreferenceMgr.get(getV(), UserInfoKey.SUB_SCREEN_ID, "").toString());
        }
    };

    /**
     * 读取gpio状态
     */
    public void readGpio() {
        state = new ReadGpioState();
        state.start();
    }
    /**
     * 暂停线程
     */
    public void stopThreadStart(){
        if (state != null) {
            state.setSuspendFlag();
        }
    }
    /**
     * 唤醒线程
     */
    public void notifyThreadStart(){
        if (state != null) {
            state.setResume();
        }
    }
    /**
     * 获取数据
     */
    public void getScreenData(boolean isRefresh, String... strings) {
        for (String address : strings) {
            BillboardApi.getDataService().getData(address)
                    .compose(XApi.<BaseBean<ScreenDataModel>>getApiTransformer())
                    .compose(XApi.<BaseBean<ScreenDataModel>>getScheduler())
                    .compose(getV().<BaseBean<ScreenDataModel>>bindToLifecycle())
                    .subscribe(new ApiSubscriber<BaseBean<ScreenDataModel>>() {
                        @Override
                        protected void onFail(NetError error) {
                            getV().dialog.dismiss();
                            if (isRefresh) {
                                callBack.onMainChangeUI();
                                callBack.onSubChangeUI();
                            } else {
                                UpdateService.getInstance().startTimer();
                            }
                            getV().showError(error);
                        }

                        @Override
                        public void onNext(BaseBean<ScreenDataModel> model) {
                            if (model.isSuccess()) {
                                downloadAndSaveData(model.getMessageBody());
                            } else {
                                if (isRefresh) {
                                    if (address.equals(AppSharePreferenceMgr.get(getV(), UserInfoKey.MAIN_SCREEN_IP, "").toString()))
                                        callBack.onMainChangeUI();
                                    else
                                        callBack.onSubChangeUI();
                                } else {
                                    if (address.equals(AppSharePreferenceMgr.get(getV(), UserInfoKey.MAIN_SCREEN_IP, "").toString()))
                                        UpdateService.getInstance().startTimer();
                                }
                            }
                        }
                    });
        }

    }

    /**
     * 下载并保存数据
     */
    private void downloadAndSaveData(ScreenDataModel model) {
        if (model.getScreenip().equals(AppSharePreferenceMgr.get(getV(), UserInfoKey.MAIN_SCREEN_IP, "").toString())) {//主屏的
            AppSharePreferenceMgr.put(getV(), UserInfoKey.MAIN_SCREEN_ID, model.getSid());
            List[] lists = ReaderJsonUtil.getInstance().splitsData(new Gson().fromJson(model.getMessage(), ScreenShowModel.class));
            XLog.e("main_lists===" + new Gson().toJson(lists));
            AppSharePreferenceMgr.put(getV(), UserInfoKey.MAIN_SHOW_PICTURE_URL, new Gson().toJson(lists[0]));
       //     AppSharePreferenceMgr.put(getV(), UserInfoKey.MAIN_PICTURE_FILE_SMALL, new Gson().toJson(lists[0]));//临时
            AppSharePreferenceMgr.put(getV(), UserInfoKey.MAIN_SHOW_VIDEO_URL, new Gson().toJson(lists[1]));
            DownloadFileUtil.getInstance().downMainLoadPicture(getV(), lists[0], lists[1], callBack);
        } else {//副屏的
            AppSharePreferenceMgr.put(getV(), UserInfoKey.SUB_SCREEN_ID, model.getSid());
            List[] lists = ReaderJsonUtil.getInstance().splitsData(new Gson().fromJson(model.getMessage(), ScreenShowModel.class));
            XLog.e("sub_lists===" + new Gson().toJson(lists));
            AppSharePreferenceMgr.put(getV(), UserInfoKey.SUB_SHOW_PICTURE_URL, new Gson().toJson(lists[0]));
            AppSharePreferenceMgr.put(getV(), UserInfoKey.SUB_SHOW_VIDEO_URL, new Gson().toJson(lists[1]));
            DownloadFileUtil.getInstance().downSubLoadPicture(getV(), lists[0], lists[1], callBack);
        }
    }

    /**
     * 上报状态
     */
    private void updateState(String screenId) {
        BillboardApi.getDataService().upState(screenId)
                .compose(XApi.<BaseBean>getApiTransformer())
                .compose(XApi.<BaseBean>getScheduler())
                .compose(getV().<BaseBean>bindToLifecycle())
                .subscribe(new ApiSubscriber<BaseBean>() {
                    @Override
                    protected void onFail(NetError error) {
                        getV().showError(error);
                    }

                    @Override
                    public void onNext(BaseBean model) {
                        if (model.isSuccess()) {
                            XLog.e("状态上报成功");
                        } else {
                            XLog.e("状态上报失败");
                        }
                    }
                });
    }

    /**
     * 回调
     */
    public interface CallBack {

        void onMainChangeUI();//主屏回调

        void onSubChangeUI();//副屏回调
    }

    private class ReadGpioState extends Thread {

        private boolean isReadState = true;

        @Override
        public void run() {
            super.run();
            while (isReadState) {
//                XLog.e("gpio0" + readState("cat /sys/class/gpio_xrm/gpio0/data"));
                if (!TextUtils.isEmpty(readState("cat /sys/class/gpio_xrm/gpio0/data")) && Integer.parseInt(readState("cat /sys/class/gpio_xrm/gpio0/data")) == 0) {
                    setSuspendFlag();
                    getV().stopPlayVideo();
                    try {
                        AppPhoneMgr.callPhone(getV(), "18729903883");
//                        AppPhoneMgr.callPhone(getV(), "18291409525");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
//                XLog.e("gpio1" + readState("cat /sys/class/gpio_xrm/gpio1/data"));
                if (!TextUtils.isEmpty(readState("cat /sys/class/gpio_xrm/gpio1/data")) && Integer.parseInt(readState("cat /sys/class/gpio_xrm/gpio1/data")) == 0) {
                    setSuspendFlag();
                    getV().stopPlayVideo();
                    try {
                        AppPhoneMgr.callPhone(getV(), "18729903883");
//                        AppPhoneMgr.callPhone(getV(), "18291409525");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }

            }
        }

        //线程暂停
        public void setSuspendFlag() {
            this.isReadState = false;
        }

        //唤醒线程
        public synchronized void setResume() {
            this.isReadState = true;
            notify();
        }

    }

    //获取GPIO状态
    private String readState(String command) {
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
            while ((line = reader.readLine()) != null) {
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
        if (output.toString().equals("")) {
            return "";
        }
        String response = output.toString().trim().substring(0, output.length() - 1);
        return response;
    }

}
