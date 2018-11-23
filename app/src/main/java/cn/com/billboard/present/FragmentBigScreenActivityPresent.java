package cn.com.billboard.present;


import java.util.ArrayList;
import java.util.List;
import cn.com.billboard.model.BaseBean;
import cn.com.billboard.model.BigScreenCallBack;
import cn.com.billboard.model.TwoScreenModel;
import cn.com.billboard.net.BillboardApi;
import cn.com.billboard.net.UserInfoKey;
import cn.com.billboard.service.UpdateService;
import cn.com.billboard.ui.FragmentBigScreenActivity;
import cn.com.billboard.util.APKVersionCodeUtils;
import cn.com.billboard.util.AppSharePreferenceMgr;
import cn.com.billboard.util.DownloadBigScreenFileUtil;
import cn.com.library.log.XLog;
import cn.com.library.mvp.XPresent;
import cn.com.library.net.ApiSubscriber;
import cn.com.library.net.NetError;
import cn.com.library.net.XApi;



public class FragmentBigScreenActivityPresent extends XPresent<FragmentBigScreenActivity> {

    /**
     * 回调页面展示数据、启动及时服务、上报状态
     */
    BigScreenCallBack callBack = new BigScreenCallBack() {

        @Override
        public void onScreenChangeUI() {
            getV().toFragmentVideo();
            updateState(AppSharePreferenceMgr.get(getV(), UserInfoKey.MAC, "").toString());
        }

        @Override
        public void onErrorChangeUI(String error) {
            getV().showError(error);
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
                            if(isRefresh){
                                getV().toFragmentVideo();
                            }
                            callBack.onErrorChangeUI(error.getMessage());
                            UpdateService.getInstance().startTimer();
                        }

                        @Override
                        public void onNext(BaseBean<TwoScreenModel> model) {
                            if (model.isSuccess()) {
                                getV().toFragmentUpdate();
                                dealData(model.getMessageBody());
                            } else {
                                if(isRefresh){
                                    getV().toFragmentVideo();
                                }
                                callBack.onErrorChangeUI(model.getDescribe());
                            }
                            UpdateService.getInstance().startTimer();
                        }
                    });
    }

    /**
     * 处理数据
     * @param model
     */
    private void dealData(TwoScreenModel model){
           String s_version= model.getBuild();
           if(s_version != null){
               int v_no = APKVersionCodeUtils.getVersionCode(getV());
               int a = Integer.parseInt(s_version);
               if(a > v_no){
                   //更新app
                   getV().toUpdateVer(model.getApkurl(),s_version);
               }else {
                   downloadAndSaveData(model);
               }
           }


    }

    /**
     * 下载并保存数据
     */
    private void downloadAndSaveData(TwoScreenModel model) {
        //下屏小图片
        List<String> lists_pic = new ArrayList<>();
        List<TwoScreenModel.HalfdowndisplayBean> halfdowndisplayBeanList =  model.getHalfdowndisplay();
          if(halfdowndisplayBeanList!=null){
              if(halfdowndisplayBeanList.size()>0){
                  for(int i=0;i<halfdowndisplayBeanList.size();i++){
                      lists_pic.add(halfdowndisplayBeanList.get(i).getUrl());
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
              }
          }

        DownloadBigScreenFileUtil.getInstance().down( lists_pic,lists_video, callBack);//下载
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
//                        getV().showError(error);
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

}
