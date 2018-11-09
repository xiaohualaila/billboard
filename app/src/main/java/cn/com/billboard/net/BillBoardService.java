package cn.com.billboard.net;

import java.util.List;
import cn.com.billboard.model.BaseBean;
import cn.com.billboard.model.ScreenDataModel;
import cn.com.billboard.model.TwoScreenModel;
import cn.com.billboard.model.VersionModel;
import io.reactivex.Flowable;
import okhttp3.MultipartBody;
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
     *type 1图片 2文字 3视频，telkey 1 消防 2监督
     * @param
     */
    @POST("system/alarm/saveAlarmDeviceRecord")
    @Multipart
    Flowable<BaseBean> uploadAlarmInfo(
            @Query("devicemac") String macAddress,
            @Query("telkey") int phone,
            @Query("type") int type,
            @Part List<MultipartBody.Part> file
    );

}
