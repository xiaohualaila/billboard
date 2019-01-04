package cn.com.billboard.net;

import java.util.List;
import java.util.Map;

import cn.com.billboard.model.BaseBean;
import cn.com.billboard.model.MessageBodyBean;
import cn.com.billboard.model.TwoScreenModel;
import io.reactivex.Flowable;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface BillBoardService {



    /**
     * 获取数据
     *
     * @param mac
     * @return
     */
    @GET("system/multimedia/getDeviceContents")
    Flowable<BaseBean<TwoScreenModel>> getData(
            @Query("devicemac") String mac,
            @Query("deviceip") String deviceip);

    /**
     * 获取数据
     *
     * @param mac
     * @return
     */
    @GET("system/multimedia/getDeviceContents")
    Flowable<BaseBean<MessageBodyBean>> getBigScreenData(
            @Query("devicemac") String mac,
            @Query("deviceip") String deviceip);
    /**
     * 上报状态
     *
     * @param screenIP
     * @return
     */
    @POST("system/multimedia/updateDeviceMessageStatus")
    Flowable<BaseBean> upState(@Query("devicemac") String screenIP);

    /**
     * 心跳发送服务器状态
     *
     * @return
     */
    @POST("pc/multimedia/screen/selectMultimediaMessage")
    Flowable<BaseBean> sendState(@Query("mac") String mac
    );


    /**
     * 上传报警信息
     * @param
     */
    @POST("system/alarm/startAlarmOfDevice")
    Flowable<BaseBean> uploadAlarm(
            @Query("devicemac") String macAddress,
            @Query("telkey") int phone
    );

    /**
     * 上传报警信息
     * telkey 1 消防 2监督
     * @param
     */
    @POST("system/alarm/saveAlarmDeviceFileRecord")
    @Multipart
    Flowable<BaseBean> uploadAlarmInfo(
            @Query("devicemac") String macAddress,
            @Query("recordid") String id,
            @Part List<MultipartBody.Part> file
    );

    @FormUrlEncoded
    @POST("http://117.36.77.242:8099/doormaster/server/employees")
    Flowable<ResponseBody> getRequest(
            @FieldMap Map<String, String> map);
}
