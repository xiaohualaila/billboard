package cn.com.billboard.ui.main;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.com.billboard.model.BaseBean;
import cn.com.billboard.model.CallBack;
import cn.com.billboard.model.TwoScreenModel;
import cn.com.billboard.retrofitdemo.Request_Interface;
import cn.com.billboard.retrofitdemo.RetrofitManager;
import cn.com.billboard.ui.base.BasePresenter;
import cn.com.billboard.util.APKVersionCodeUtils;
import cn.com.billboard.util.DownloadFileUtil;
import cn.com.billboard.util.FileUtil;
import cn.com.billboard.util.Kits;
import cn.com.billboard.util.SharedPreferencesUtil;
import cn.com.billboard.util.UserInfoKey;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * Created by Administrator on 2017/6/3.
 */

public class MainPresenter extends BasePresenter implements MainContract.Presenter {
    private MainContract.View view;

    public MainPresenter(MainContract.View view) {
        this.view = view;
        this.view.setPresenter(this);
    }

    @Override
    public void start() {

    }

    /**
     * 回调页面展示数据、启动及时服务、上报状态
     */
    CallBack callBack = new CallBack() {
        @Override
        public void onMainChangeUI() {
            selectFragment();
        }

        @Override
        public void onMainUpdateUI() {
            selectFragment();
            updateState(SharedPreferencesUtil.getString(MainActivity.instance(), UserInfoKey.MAC, ""));
        }

        @SuppressLint("NewApi")
        @Override
        public void onSubChangeUI() {
            view.showSubData();
        }

        @Override
        public void onErrorChangeUI(String error) {
            view.showError(error);
        }

    };

    private void selectFragment() {
        List<String> images_big = FileUtil.getFilePath(UserInfoKey.PIC_BIG_DOWM);
        if (images_big.size() > 0) {
            view.toFragemntBigPic();
        } else {
            view.toFragemntMain();
        }
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

                    }

                    @Override
                    public void onNext(BaseBean model) {
                        if (model.isSuccess()) {
                            Log.i("sss", "状态上报成功");
                        } else {
                            Log.i("sss", "状态上报失败");
                        }

                    }
                });
    }


    /**
     * 获取数据
     */
    public void getScreenData(boolean isRefresh, String mac, String ipAddress, Context context) {
        Request_Interface request = RetrofitManager.getInstance().create(Request_Interface.class);
        request.getData(mac, ipAddress)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<BaseBean<TwoScreenModel>>() {
                    @Override
                    public void onComplete() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.i("sss", "++++" + e.toString());
                        if (isRefresh) {
                            callBack.onMainChangeUI();
                            callBack.onSubChangeUI();
                        }
                        view.showError("网络异常！");
                    }

                    @Override
                    public void onNext(BaseBean<TwoScreenModel> model) {
                        if (model.isSuccess()) {
                            dealData(model.getMessageBody(), context, isRefresh);
                        } else {
                            if (isRefresh) {
                                callBack.onMainChangeUI();
                                callBack.onSubChangeUI();
                            }
                            view.showError(model.getDescribe());
                        }

                    }
                });
    }

    /**
     * 处理数据
     *
     * @param model
     */
    private void dealData(TwoScreenModel model, Context context, boolean isRefresh) {
        String s_version = model.getBuild();
        if (s_version != null) {
            int v_no = APKVersionCodeUtils.getVersionCode(context);
            int a = Integer.parseInt(s_version);
            if (a > v_no) {
                //更新app
                view.toUpdateVer(model.getApkurl(), s_version);
            } else {
                if (model.getHalfdowndisplay() == null && model.getDowndisplay() == null && model.getDowndisplay() == null && model.getUpdisplay() == null) {
                    if (isRefresh) {
                        callBack.onMainChangeUI();
                        callBack.onSubChangeUI();
                    }
                    return;
                }
                downloadAndSaveData(model, context);
            }
        }
    }

    /**
     * 下载并保存数据
     */
    private void downloadAndSaveData(TwoScreenModel model, Context context) {
        String tell = model.getTel1();
        String tel2 = model.getTel2();
        int time = Integer.parseInt(model.getHeartinterval());
        SharedPreferencesUtil.putString(context, "tell", tell);
        SharedPreferencesUtil.putString(context, "tel2", tel2);
        SharedPreferencesUtil.putInt(context, "time", time);

        view.toFragemntUpdate();

        //下屏小图片
        List<String> lists_pic_small_dowm = new ArrayList<>();
        List<TwoScreenModel.HalfdowndisplayBean> halfdowndisplayBeanList = model.getHalfdowndisplay();
        if (halfdowndisplayBeanList != null) {
            if (halfdowndisplayBeanList.size() > 0) {
                for (int i = 0; i < halfdowndisplayBeanList.size(); i++) {
                    lists_pic_small_dowm.add(halfdowndisplayBeanList.get(i).getUrl());
                }
            }
        }

        //下屏大图片
        List<String> lists_pic_big_dowm = new ArrayList<>();
        List<TwoScreenModel.DowndisplayBean> downdisplayBean = model.getDowndisplay();
        if (downdisplayBean != null) {
            if (downdisplayBean.size() > 0) {
                for (int i = 0; i < downdisplayBean.size(); i++) {
                    lists_pic_big_dowm.add(downdisplayBean.get(i).getUrl());
                }
            }
        }

        //上屏图片
        List<String> lists_pic_up = new ArrayList<>();
        List<TwoScreenModel.UpdisplayBean> updisplayBean = model.getUpdisplay();
        if (updisplayBean != null) {
            if (updisplayBean.size() > 0) {
                for (int i = 0; i < updisplayBean.size(); i++) {
                    lists_pic_up.add(updisplayBean.get(i).getUrl());
                }
            }
        }

        //下屏视频
        List<String> lists_video = new ArrayList<>();
        List<TwoScreenModel.HalfupdisplayBean> halfupdisplayBean = model.getHalfupdisplay();
        if (halfupdisplayBean != null) {
            if (halfupdisplayBean.size() > 0) {
                for (int i = 0; i < halfupdisplayBean.size(); i++) {
                    lists_video.add(halfupdisplayBean.get(i).getUrl());
                }
            }
        }


        DownloadFileUtil.getInstance().downMainLoadPicture(context, lists_pic_small_dowm, lists_pic_big_dowm, lists_pic_up, lists_video, callBack);//下载
    }

    /**
     * 上传打电话人员的视频
     */
    public void uploadAlarmInfo(String macAddress, String recordId, String video_path, String pic_path, Context context) {
        Log.i("sss", "准备上传");
        if (TextUtils.isEmpty(video_path) && TextUtils.isEmpty(pic_path)) {
            return;
        }
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        RequestBody requestBody = null;
        if (!TextUtils.isEmpty(video_path)) {
            File v_file = new File(video_path);
            if (v_file.exists()) {
                requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), v_file);
                builder.addFormDataPart("video", v_file.getName(), requestBody);
            }
        }
        if (!TextUtils.isEmpty(pic_path)) {
            File p_file = new File(pic_path);
            if (p_file.exists()) {
                builder.addPart(Headers.of("Content-Disposition", "form-data; name=\"pic\";filename=\"file.jpeg\""),
                        RequestBody.create(MediaType.parse("image/png"), p_file)).build();

            }
        }
        List<MultipartBody.Part> list = null;
        try {
            list = builder.build().parts();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i("sss", "开始上传");
        Request_Interface request = RetrofitManager.getInstance().create(Request_Interface.class);
        request.uploadAlarmInfo(macAddress, recordId, list)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<BaseBean>() {
                    @Override
                    public void onComplete() {
                        Kits.File.deleteFile(UserInfoKey.RECORD_VIDEO_PATH);
                        Kits.File.deleteFile(UserInfoKey.BILLBOARD_PICTURE_FACE_PATH);
                        SharedPreferencesUtil.putString(context, "alarmId", "");
                        Log.i("sss", "onComplete");
                    }

                    @Override
                    public void onError(Throwable e) {
                        view.showError("网络异常！");
                        Log.i("sss", "上传失败");
                        Log.i("sss", e.getMessage());
                        Kits.File.deleteFile(UserInfoKey.RECORD_VIDEO_PATH);
                        Kits.File.deleteFile(UserInfoKey.BILLBOARD_PICTURE_FACE_PATH);
                        SharedPreferencesUtil.putString(context, "alarmId", "");
                    }

                    @Override
                    public void onNext(BaseBean model) {
                        if (model.isSuccess()) {
                            view.showError("上报成功！");
                            Log.i("sss", "上报成功");
                        } else {
                            view.showError("上报失败！");
                            Log.i("sss", "上传失败");
                        }
                    }
                });
    }

}
