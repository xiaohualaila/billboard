package cn.com.billboard.retrofitdemo;

import cn.com.billboard.model.BaseBean;
import cn.com.billboard.model.MessageBodyBean;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by Carson_Ho on 17/3/21.
 */
public interface Request_Interface {

    /**
     * 获取数据
     *
     * @param mac
     * @return
     */
    @GET("system/multimedia/getDeviceContents")
    Observable<BaseBean<MessageBodyBean>> getBigScreenData(
            @Query("devicemac") String mac,
            @Query("deviceip") String deviceip);

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
}

