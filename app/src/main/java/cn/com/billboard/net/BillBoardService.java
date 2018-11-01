package cn.com.billboard.net;

import java.util.Date;
import java.util.List;

import cn.com.billboard.model.BaseBean;
import cn.com.billboard.model.ScreenDataModel;
import cn.com.billboard.model.VersionModel;
import io.reactivex.Flowable;
import okhttp3.MultipartBody;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface BillBoardService {

    /**
     * 版本检测
     *
     * @param code 传0时代表检测双屏代码版本   传1时代表检测大屏代码版本
     * @return
     */
    @POST("app/version/selectNewVersion")
    Flowable<BaseBean<VersionModel>> checkVersion(
            @Query("mac") String mac,
            @Query("genre") int code);

    /**
     * 获取数据
     *
     * @param screenIP
     * @return
     */
    @POST("pc/multimedia/screen/selectMultimediaMessage")
    Flowable<BaseBean<ScreenDataModel>> getData(@Query("screenip") String screenIP);

    /**
     * 上报状态
     *
     * @param screenIP
     * @return
     */
    @POST("pc/multimedia/screen/completeMultimediaMessage")
    Flowable<BaseBean> upState(@Query("id") String screenIP);

    /**
     * 心跳发送服务器状态
     *
     * @return
     */
    @POST("pc/multimedia/screen/selectMultimediaMessage")
    Flowable<BaseBean> sendState(@Query("mac") String mac
    );

    /**
     * 上传视频
     *
     * @param
     * @return    public void uploadVideo(String macAddress, Date beginDate, Date endData, int phone, String screenType, File file) {
     */
    @POST("")
    Flowable<BaseBean> uploadVideo(
            @Query("mac") String macAddress,
            @Query("beginDate") Date beginDate,
            @Query("endData") Date endData,
            @Query("phone") int phone,
            @Query("screenType") String screenType,
            @Part List<MultipartBody.Part> file
    );

    /**
     * 上传视频
     *
     * @param
     * @return    public void uploadVideo(String macAddress, Date beginDate, Date endData, int phone, String screenType, File file) {
     */
    @POST("")
    Flowable<BaseBean> uploadFacePic(
            @Query("mac") String macAddress,
            @Query("phone") int phone,
            @Part List<MultipartBody.Part> file
    );

}
