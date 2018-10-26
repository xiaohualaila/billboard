package cn.com.billboard.present;


import com.google.gson.Gson;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cn.com.billboard.model.BaseBean;
import cn.com.billboard.model.ScreenDataModel;
import cn.com.billboard.model.ScreenShowModel;
import cn.com.billboard.net.BillboardApi;
import cn.com.billboard.net.UserInfoKey;
import cn.com.billboard.service.UpdateService;
import cn.com.billboard.ui.TwoScreenActivity;
import cn.com.billboard.util.AppSharePreferenceMgr;
import cn.com.billboard.util.DownloadFileUtil;
import cn.com.billboard.util.ReaderJsonUtil;
import cn.com.library.log.XLog;
import cn.com.library.mvp.XPresent;
import cn.com.library.net.ApiSubscriber;
import cn.com.library.net.NetError;
import cn.com.library.net.XApi;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class TwoScreenPresent extends XPresent<TwoScreenActivity> {

    /**
     * 回调页面展示数据、启动及时服务、上报状态
     */
    CallBack callBack = new CallBack() {
        @Override
        public void onMainChangeUI() {
            getV().showData();
            UpdateService.getInstance().startTimer();
            updateState(AppSharePreferenceMgr.get(getV(), UserInfoKey.MAIN_SCREEN_ID, "").toString());
        }

        @Override
        public void onSubChangeUI() {
            getV().showSubData();
            updateState(AppSharePreferenceMgr.get(getV(), UserInfoKey.SUB_SCREEN_ID, "").toString());
        }

        @Override
        public void onErrorChangeUI(String error) {
            getV().showToastManger(error);
        }
    };


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
                                getV().showDownFile();
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
            DownloadFileUtil.getInstance().downMainLoadPicture(getV(), lists[0], lists[1], callBack);//下载
        } else {//副屏的
            AppSharePreferenceMgr.put(getV(), UserInfoKey.SUB_SCREEN_ID, model.getSid());
            List[] lists = ReaderJsonUtil.getInstance().splitsData(new Gson().fromJson(model.getMessage(), ScreenShowModel.class));
            XLog.e("sub_lists===" + new Gson().toJson(lists));
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
     * 心跳
     */
    public void sendState(String mac){
        //10秒
        Observable.interval(10, TimeUnit.SECONDS).
                subscribeOn(Schedulers.io()).
                subscribe(new Consumer<Long>() {
                    @Override public void accept(Long num) throws Exception {



                    }
                });
    }

    /**
     * 回调
     */
    public interface CallBack {

        void onMainChangeUI();//主屏回调

        void onSubChangeUI();//副屏回调

        void onErrorChangeUI(String error);//下载失败无法下载
    }

}
