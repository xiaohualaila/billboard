package cn.com.billboard.retrofit.api;

import org.json.JSONObject;

import java.util.List;

import cn.com.billboard.model.BaseBean;
import cn.com.billboard.model.TwoScreenModel;
import okhttp3.MultipartBody;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by xyuxiao on 2016/9/23.
 */
public interface BaseApi {

    /**
     * 上传报警信息
     * @param
     */
    @POST("system/alarm/startAlarmOfDevice")
    Observable<JSONObject> uploadAlarm(
            @Query("devicemac") String macAddress,
            @Query("telkey") int phone
    );

    /**
     * 获取数据
     * @param mac
     * @return
     */
    @GET("system/multimedia/getDeviceContents")
    Observable<JSONObject> getData(
            @Query("devicemac") String mac,
            @Query("deviceip") String deviceip);

    /**
     * 上传报警信息
     * telkey 1 消防 2监督
     * @param
     */
    @POST("system/alarm/saveAlarmDeviceFileRecord")
    @Multipart
    Observable<JSONObject> uploadAlarmInfo(
            @Query("devicemac") String macAddress,
            @Query("recordid") String id,
            @Part List<MultipartBody.Part> file
    );
}

