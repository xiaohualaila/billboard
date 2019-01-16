package cn.com.billboard.ui.main;

import android.content.Context;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import cn.com.billboard.model.BaseBean;
import cn.com.billboard.model.BigScreenCallBack;
import cn.com.billboard.model.MessageBodyBean;
import cn.com.billboard.retrofitdemo.BillboardApi;
import cn.com.billboard.retrofitdemo.Request_Interface;
import cn.com.billboard.retrofitdemo.RetrofitManager;
import cn.com.billboard.ui.base.BasePresenter;
import cn.com.billboard.util.APKVersionCodeUtils;
import cn.com.billboard.util.DownloadBigScreenFileUtil;
import cn.com.billboard.util.FileUtil;
import cn.com.billboard.util.SharedPreferencesUtil;
import cn.com.billboard.util.UserInfoKey;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;


/**
 * Created by Administrator on 2017/6/3.
 */

public class MainPresenter extends BasePresenter implements MainContract.Presenter {
    private MainContract.View view;
    private boolean isFirst = true;

    public MainPresenter(MainContract.View view) {
        this.view = view;
        this.view.setPresenter(this);
    }
    /**
     * 回调页面展示数据、启动及时服务、上报状态
     */
    BigScreenCallBack callBack = new BigScreenCallBack() {

        @Override
        public void onScreenChangeUI() {
            List<String> images = FileUtil.getFilePath(UserInfoKey.PIC_BIG_IMAGE_DOWN);
            if(images.size()>0){
                view.toFragmentImg();
            }else {
                view.toFragmentVideo();
            }

            updateState(SharedPreferencesUtil.getString(MainActivity.instance(), UserInfoKey.MAC, ""));
        }

        @Override
        public void onErrorChangeUI(String error) {
            view.showError(error);
        }
    };


    /**
     * 获取数据
     */
    public void getScreenData(Context context,boolean isRefresh, String mac, String ipAddress) {
        isFirst = isRefresh;
        Request_Interface request = RetrofitManager.getInstance().create(Request_Interface.class);
        request.getBigScreenData(mac, ipAddress)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<BaseBean<MessageBodyBean>>() {
                    @Override
                    public void onComplete() {

                    }
                    @Override
                    public void onError(Throwable e) {
                        if (isRefresh) {
                            callBack.onScreenChangeUI();
                        }
                        callBack.onErrorChangeUI(e.getMessage());
                    }
                    @Override
                    public void onNext(BaseBean<MessageBodyBean> model) {
                        if (model.isSuccess()) {
                            dealData(model.getMessageBody(),context,isRefresh);
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
    private void dealData(MessageBodyBean model,Context context,boolean isRefresh) {

        String s_version = model.getBuild();
        if (s_version != null) {
            int v_no = APKVersionCodeUtils.getVersionCode(context);
            int a = Integer.parseInt(s_version);
            if (a > v_no) {
                //更新app
                view.toFragmentUpdate();
                view.toUpdateVer(model.getApkurl(), s_version);
            } else {
                downloadAndSaveData(model,context,isRefresh);
            }
        }
    }

    /**
     * 下载并保存数据
     */
    private void downloadAndSaveData(MessageBodyBean model,Context context,boolean isRefresh) {
        String  tel1 = model.getTel1();
        String  tel2 = model.getTel2();
        String  tel3 = model.getTel3();
        String  tel4 = model.getTel4();
//        Log.i("sss","tel1  "+tel1);
//        Log.i("sss","tel2  "+tel2);
//        Log.i("sss","tel3  "+tel3);
//        Log.i("sss","tel4  "+tel4);
        SharedPreferencesUtil.putString(context,"tel1",tel1);
        SharedPreferencesUtil.putString(context,"tel2",tel2);
        SharedPreferencesUtil.putString(context,"tel3",tel3);
        SharedPreferencesUtil.putString(context,"tel4",tel4);
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
        List<String>  images  = FileUtil.getCommonFileNames(lists_pic,UserInfoKey.PIC_BIG_IMAGE_DOWN);
        List<String>  videos  = FileUtil.getCommonFileNames(lists_video, UserInfoKey.BIG_VIDEO);

        if(images.size()==0 && videos.size()==0){
            if(isRefresh){
                callBack.onScreenChangeUI();
            }
            return;
        }
        view.toFragmentUpdate();
        DownloadBigScreenFileUtil.getInstance().down(images, videos, callBack);//下载
    }


    /**
     * 上报状态
     */
    private void updateState(String mac) {
        Request_Interface request = RetrofitManager.getInstance().create(Request_Interface.class);
        request.upState(mac)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<BaseBean>() {
                    @Override
                    public void onComplete() {

                    }
                    @Override
                    public void onError(Throwable e) {
                        view.showError("网络异常！");
                    }
                    @Override
                    public void onNext(BaseBean model) {
                        if (model.isSuccess()) {
                            Log.i("sss","状态上报成功");
                        } else {
                            Log.i("sss","状态上报失败");
                        }
                    }
                });
    }

    /**
     * 上传报警
     */
    public void uploadAlarm(String macAddress,int telkey) {
        Request_Interface request = RetrofitManager.getInstance().create(Request_Interface.class);
        request.uploadAlarm(macAddress,telkey)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<BaseBean>() {
                    @Override
                    public void onComplete() {

                    }
                    @Override
                    public void onError(Throwable e) {
                        view.showError("网络异常！");
                    }
                    @Override
                    public void onNext(BaseBean model) {
                        if (model.isSuccess()) {
                            Log.i("sss","状态上报成功");
                        } else {
                            Log.i("sss","状态上报失败");
                        }
                    }
                });
    }


    @Override
    public void start() {

    }
}
