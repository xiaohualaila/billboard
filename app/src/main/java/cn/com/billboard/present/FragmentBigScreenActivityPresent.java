package cn.com.billboard.present;


import java.util.ArrayList;
import java.util.List;
import cn.com.billboard.model.BaseBean;
import cn.com.billboard.model.BigScreenCallBack;
import cn.com.billboard.model.MessageBodyBean;
import cn.com.billboard.net.BillboardApi;
import cn.com.billboard.net.UserInfoKey;
import cn.com.billboard.ui.FragmentBigScreenActivity;
import cn.com.billboard.util.APKVersionCodeUtils;

import cn.com.billboard.util.DownloadBigScreenFileUtil;
import cn.com.billboard.util.FileUtil;
import cn.com.billboard.util.GsonProvider;
import cn.com.billboard.util.SharedPreferencesUtil;
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
           List<String> images = FileUtil.getFilePath(UserInfoKey.PIC_BIG_IMAGE_DOWN);
           if(images.size()>0){
               getV().toFragmentImg();
           }else {
               getV().toFragmentVideo();
           }

            updateState(SharedPreferencesUtil.getString(getV(), UserInfoKey.MAC, ""));
        }

        @Override
        public void onErrorChangeUI(String error) {
            getV().showError(error);
        }
    };


    /**
     * 获取数据
     */
    public void getScreenData(boolean isRefresh, String mac, String ipAddress) {
        BillboardApi.getDataService().getBigScreenData(mac, ipAddress)
                .compose(XApi.<BaseBean<MessageBodyBean>>getApiTransformer())
                .compose(XApi.<BaseBean<MessageBodyBean>>getScheduler())
                .compose(getV().<BaseBean<MessageBodyBean>>bindToLifecycle())
                .subscribe(new ApiSubscriber<BaseBean<MessageBodyBean>>() {
                    @Override
                    protected void onFail(NetError error) {
                        if (isRefresh) {
                            callBack.onScreenChangeUI();
                        }
                        callBack.onErrorChangeUI(error.getMessage());
                    }

                    @Override
                    public void onNext(BaseBean<MessageBodyBean> model) {
                        if (model.isSuccess()) {
                            getV().toFragmentUpdate();
                            dealData(model.getMessageBody());
                        } else {
                            if (isRefresh) {
                                callBack.onScreenChangeUI();
                            }
                            callBack.onErrorChangeUI(model.getDescribe());
                        }
                    }
                });
    }

    /**
     * 处理数据
     *
     * @param model
     */
    private void dealData(MessageBodyBean model) {

        String s_version = model.getBuild();
        if (s_version != null) {
            int v_no = APKVersionCodeUtils.getVersionCode(getV());
            int a = Integer.parseInt(s_version);
            if (a > v_no) {
                //更新app
                getV().toUpdateVer(model.getApkurl(), s_version);
            } else {
                downloadAndSaveData(model);
            }
        }
    }

    /**
     * 下载并保存数据
     */
    private void downloadAndSaveData(MessageBodyBean model) {
        String  tel1 = model.getTel1();
        String  tel2 = model.getTel2();
        String  tel3 = model.getTel3();
        String  tel4 = model.getTel4();
        SharedPreferencesUtil.putString(getV(),"tel1",tel1);
        SharedPreferencesUtil.putString(getV(),"tel2",tel2);
        SharedPreferencesUtil.putString(getV(),"tel3",tel3);
        SharedPreferencesUtil.putString(getV(),"tel4",tel4);
        if( model.getFullPics()== null && model.getFullVideos() == null  ){
            callBack.onScreenChangeUI();
            return;
        }
        //图片
        List<String> lists_pic = new ArrayList<>();

        List<MessageBodyBean.FullPicsBean> halfdowndisplayBeanList = model.getFullPics();
        if (halfdowndisplayBeanList != null) {
            if (halfdowndisplayBeanList.size() > 0) {
                for (int i = 0; i < halfdowndisplayBeanList.size(); i++) {
                    lists_pic.add(halfdowndisplayBeanList.get(i).getUrl());
                }
            }
        }

        //视频
        List<String> lists_video = new ArrayList<>();
        List<MessageBodyBean.FullVideosBean> halfupdisplayBean = model.getFullVideos();
        if (halfupdisplayBean != null) {
            if (halfupdisplayBean.size() > 0) {
                for (int i = 0; i < halfupdisplayBean.size(); i++) {
                    lists_video.add(halfupdisplayBean.get(i).getUrl());
                }
            }
        }
        //标题
//        List<MessageBodyBean.StripedisplayBean> stripedisplayBeans = model.getStripedisplay();
//        List<String> lists_str = new ArrayList<>();
//        if (stripedisplayBeans != null) {
//            if (stripedisplayBeans.size()>0) {
//                for (int i = 0; i < stripedisplayBeans.size(); i++) {
//                    lists_str.add(stripedisplayBeans.get(i).getTitle());
//                }
//            }
//        }
//        SharedPreferencesUtil.putString(getV(),"titles", GsonProvider.getInstance().getGson().toJson(lists_str));

        DownloadBigScreenFileUtil.getInstance().down(lists_pic, lists_video, callBack);//下载
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
