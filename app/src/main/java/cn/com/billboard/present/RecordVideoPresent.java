package cn.com.billboard.present;

import java.io.File;
import java.util.Date;
import cn.com.billboard.model.BaseBean;
import cn.com.billboard.net.BillboardApi;
import cn.com.billboard.ui.RecordvideoActivity;
import cn.com.library.log.XLog;
import cn.com.library.mvp.XPresent;
import cn.com.library.net.ApiSubscriber;
import cn.com.library.net.NetError;
import cn.com.library.net.XApi;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class RecordVideoPresent extends XPresent<RecordvideoActivity> {

    /**
     * 上传打电话人员的视频
     */
    public void uploadVideo(String macAddress,int phone, File file) {
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        builder.addFormDataPart("file", file.getName(), requestBody);
        BillboardApi.getDataService().uploadAlarmInfo(macAddress,phone,3,builder.build().parts())
                .compose(XApi.<BaseBean>getApiTransformer())
                .compose(XApi.<BaseBean>getScheduler())
                .compose(getV().<BaseBean>bindToLifecycle())
                .subscribe(new ApiSubscriber<BaseBean>() {
                    @Override
                    protected void onFail(NetError error) {
                         getV().uploadFinish();
                    }

                    @Override
                    public void onNext(BaseBean model) {
                        if (model.isSuccess()) {
                            XLog.e("状态上报成功");
                        } else {
                            XLog.e("状态上报失败");
                        }
                        getV().uploadFinish();
                    }
                });
    }




}
