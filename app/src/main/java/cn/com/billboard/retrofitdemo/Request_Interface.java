package cn.com.billboard.retrofitdemo;

import java.util.List;
import cn.com.billboard.model.BaseBean;
import cn.com.billboard.model.TwoScreenModel;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import okhttp3.MultipartBody;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

/**
 * Created by Carson_Ho on 17/3/21.
 */
public interface Request_Interface {


    /**
     * 上报状态
     *
     * @param screenIP
     * @return
     */
    @POST("system/multimedia/updateDeviceMessageStatus")
    Observable<BaseBean> upState(@Query("devicemac") String screenIP);

    /**
     * 上传报警信息
     * @param
     */
    @POST("system/alarm/startAlarmOfDevice")
    Observable<BaseBean> uploadAlarm(
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
    Observable<BaseBean> uploadAlarmInfo(
            @Query("devicemac") String macAddress,
            @Query("recordid") String id,
            @Part List<MultipartBody.Part> file
    );

    /**
     * 获取数据
     *
     * @param mac
     * @return
     */
    @GET("system/multimedia/getDeviceContents")
    Observable<BaseBean<TwoScreenModel>> getData(
            @Query("devicemac") String mac,
            @Query("deviceip") String deviceip);


}

