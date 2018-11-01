package cn.com.billboard.present;

import java.io.File;
import java.util.Date;

import cn.com.billboard.ui.OpenCVCameraActivity;

import cn.com.library.mvp.XPresent;

public class OpenCVPresent extends XPresent<OpenCVCameraActivity> {
    /**
     * 上传打电话人员的视频
     */
    public void uploadVideo(String macAddress, Date date, int phone, String screenType, File file) {
        getV().uploadFinish();
//        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
//        RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
//        builder.addFormDataPart("video", file.getName(), requestBody);
//        BillboardApi.getDataService().uploadVideo(macAddress,beginDate,endData,phone,screenType,builder.build().parts())
//                .compose(XApi.<BaseBean>getApiTransformer())
//                .compose(XApi.<BaseBean>getScheduler())
//                .compose(getV().<BaseBean>bindToLifecycle())
//                .subscribe(new ApiSubscriber<BaseBean>() {
//                    @Override
//                    protected void onFail(NetError error) {
//                         getV().uploadFinish();
//                    }
//
//                    @Override
//                    public void onNext(BaseBean model) {
//                        if (model.isSuccess()) {
//                            XLog.e("状态上报成功");
//                        } else {
//                            XLog.e("状态上报失败");
//                        }
//                        getV().uploadFinish();
//                    }
//                });
    }
}
