package cn.com.billboard.present;


import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import cn.com.billboard.model.BaseBean;
import cn.com.billboard.model.CallBack;
import cn.com.billboard.model.TwoScreenModel;
import cn.com.billboard.net.BillboardApi;
import cn.com.billboard.net.UserInfoKey;
import cn.com.billboard.service.GPIOService;
import cn.com.billboard.ui.TwoScreenActivity;
import cn.com.billboard.util.APKVersionCodeUtils;
import cn.com.billboard.util.AppSharePreferenceMgr;
import cn.com.billboard.util.DownloadFileUtil;
import cn.com.library.kit.Kits;
import cn.com.library.log.XLog;
import cn.com.library.mvp.XPresent;
import cn.com.library.net.ApiSubscriber;
import cn.com.library.net.NetError;
import cn.com.library.net.XApi;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;


public class TwoScreenPresent extends XPresent<TwoScreenActivity> {

    /**
     * 回调页面展示数据、启动及时服务、上报状态
     */
    CallBack callBack = new CallBack() {
        @Override
        public void onMainChangeUI() {
            getV().showData();
        }

        @Override
        public void onMainUpdateUI() {
            getV().showMainUpdateData();
        }

        @SuppressLint("NewApi")
        @Override
        public void onSubChangeUI() {
            getV().showSubData();
        }


        @Override
        public void onErrorChangeUI(String error) {
            getV().showToastManger(error);
        }
    };


    /**
     * 获取数据
     */
    public void getScreenData(boolean isRefresh,String mac,String ipAddress) {
            BillboardApi.getDataService().getData(mac,ipAddress)
                    .compose(XApi.<BaseBean<TwoScreenModel>>getApiTransformer())
                    .compose(XApi.<BaseBean<TwoScreenModel>>getScheduler())
                    .compose(getV().<BaseBean<TwoScreenModel>>bindToLifecycle())
                    .subscribe(new ApiSubscriber<BaseBean<TwoScreenModel>>() {
                        @Override
                        protected void onFail(NetError error) {
                            if (isRefresh) {
                                callBack.onMainChangeUI();
                                callBack.onSubChangeUI();
                            }else {
                                callBack.onMainUpdateUI();
                                callBack.onSubChangeUI();
                            }
                            GPIOService.getInstance().startTimer();
                            getV().showError(error);
                        }

                        @Override
                        public void onNext(BaseBean<TwoScreenModel> model) {
                            if (model.isSuccess()) {

                                dealData(model.getMessageBody(),isRefresh);
                                updateState(AppSharePreferenceMgr.get(getV(), UserInfoKey.MAC, "").toString());
                            } else {
                                if (isRefresh) {
                                        callBack.onMainChangeUI();
                                        callBack.onSubChangeUI();
                                        getV().toastL(model.getDescribe());
                                }
                            }
                            GPIOService.getInstance().startTimer();
                        }
                    });
    }

    private void dealData(TwoScreenModel model,boolean isRefresh){
           String s_version= model.getBuild();
           if(s_version != null){
               int v_no = APKVersionCodeUtils.getVersionCode(getV());
               int a = Integer.parseInt(s_version);
               if(a > v_no){
                   //更新app
                   getV().toUpdateVer(model.getApkurl(),s_version);
               }else {
                   getV().showDownFile();
                   downloadAndSaveData(model,isRefresh);
               }
           }


    }

    /**
     * 下载并保存数据
     */
    private void downloadAndSaveData(TwoScreenModel model,boolean isRefresh) {
        String tell = model.getTel1();
        String tel2 = model.getTel2();
        AppSharePreferenceMgr.put(getV(),"tell",tell);
        AppSharePreferenceMgr.put(getV(),"tel2",tel2);
        //下屏小图片
        List<String> lists_pic_small_dowm = new ArrayList<>();
        List<TwoScreenModel.HalfdowndisplayBean> halfdowndisplayBeanList =  model.getHalfdowndisplay();
          if(halfdowndisplayBeanList!=null){
              if(halfdowndisplayBeanList.size()>0){
                  for(int i=0;i<halfdowndisplayBeanList.size();i++){
                      lists_pic_small_dowm.add(halfdowndisplayBeanList.get(i).getUrl());
                  }
              }
          }

        //下屏大图片
        List<String> lists_pic_big_dowm = new ArrayList<>();
        List<TwoScreenModel.DowndisplayBean> downdisplayBean =  model.getDowndisplay();
           if(downdisplayBean!=null){
               if(downdisplayBean.size()>0){
                   for(int i=0;i<downdisplayBean.size();i++){
                       lists_pic_big_dowm.add(downdisplayBean.get(i).getUrl());
                   }
               }
           }

        //上屏图片
        List<String> lists_pic_up = new ArrayList<>();
        List<TwoScreenModel.UpdisplayBean> updisplayBean =  model.getUpdisplay();
          if(updisplayBean!=null){
              if(updisplayBean.size()>0){
                  for(int i=0;i<updisplayBean.size();i++){
                      lists_pic_up.add(updisplayBean.get(i).getUrl());
                  }
              }
          }

        //下屏视频
        List<String> lists_video = new ArrayList<>();
        List<TwoScreenModel.HalfupdisplayBean> halfupdisplayBean =  model.getHalfupdisplay();
          if(halfupdisplayBean!=null){
              if(halfupdisplayBean.size()>0){
                  for(int i=0;i<halfupdisplayBean.size();i++){
                      lists_video.add(halfupdisplayBean.get(i).getUrl());
                  }
               //   Log.i("sss","下屏视频===" + new Gson().toJson(lists_video));
              }
          }

        DownloadFileUtil.getInstance().downMainLoadPicture(getV(), lists_pic_small_dowm,lists_pic_big_dowm,lists_pic_up,lists_video, callBack,isRefresh);//下载
    }


    /**
     * 上报状态
     */
    private void updateState(String mac) {
        BillboardApi.getDataService().upState(mac)
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
//    public void sendState(String mac){
//        //10秒
//        Observable.interval(10, TimeUnit.SECONDS).
//                subscribeOn(Schedulers.io()).
//                subscribe(new Consumer<Long>() {
//                    @Override public void accept(Long num) throws Exception {
//
//
//
//                    }
//                });
//    }

    /**
     * 上传报警
     */
    public void uploadAlarm(String macAddress,int telkey) {

        BillboardApi.getDataService().uploadAlarm(macAddress,telkey)
                .compose(XApi.<BaseBean>getApiTransformer())
                .compose(XApi.<BaseBean>getScheduler())
                .compose(getV().<BaseBean>bindToLifecycle())
                .subscribe(new ApiSubscriber<BaseBean>() {
                    @Override
                    protected void onFail(NetError error) {
                        XLog.e("状态上报失败");

                    }

                    @Override
                    public void onNext(BaseBean model) {
                        if (model.isSuccess()) {
                            XLog.e("状态上报成功");
                        } else {
                            XLog.e("状态上报失败");
                        }
                        getV().getAlarmId("");//返回报警ID
                    }
                });
    }


    /**
     * 上传打电话人员的视频
     */
    public void uploadAlarmInfo(String macAddress,String recordId) {

        String video_path = (String) AppSharePreferenceMgr.get(getV(), "videoFile", "");
        String pic_path = (String) AppSharePreferenceMgr.get(getV(), "picFile", "");
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        if(!TextUtils.isEmpty(video_path)){
            File v_file =new File(video_path);
            if(v_file.exists()){
                RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), v_file);
                builder.addFormDataPart("video", v_file.getName(), requestBody);
            }
        }
        if(!TextUtils.isEmpty(pic_path)){
            File p_file =new File(pic_path);
            if (p_file.exists()){
                RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), pic_path);
                builder.addFormDataPart("file", p_file.getName(), requestBody);
            }
        }
        BillboardApi.getDataService().uploadAlarmInfo(macAddress,recordId,builder.build().parts())
                .compose(XApi.<BaseBean>getApiTransformer())
                .compose(XApi.<BaseBean>getScheduler())
                .compose(getV().<BaseBean>bindToLifecycle())
                .subscribe(new ApiSubscriber<BaseBean>() {
                    @Override
                    protected void onFail(NetError error) {
                        XLog.e("状态上报失败");
                        Kits.File.deleteFile(UserInfoKey.RECORD_VIDEO_PATH);
                        Kits.File.deleteFile(UserInfoKey.BILLBOARD_PICTURE_FACE_PATH);
                    }

                    @Override
                    public void onNext(BaseBean model) {
                        if (model.isSuccess()) {
                            XLog.e("状态上报成功");
                        } else {
                            XLog.e("状态上报失败");
                        }
                        Kits.File.deleteFile(UserInfoKey.RECORD_VIDEO_PATH);
                        Kits.File.deleteFile(UserInfoKey.BILLBOARD_PICTURE_FACE_PATH);
                    }
                });
    }




}
