package cn.com.billboard.net;

import cn.com.billboard.model.BaseBean;
import cn.com.billboard.model.ScreenDataModel;
import cn.com.billboard.model.VersionModel;
import io.reactivex.Flowable;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface BillBoardService {

    /**
     * 版本检测
     *
     * @param code 传0时代表检测双屏代码版本   传1时代表检测大屏代码版本  2 代表智慧社区  3 门禁
     * @return
     */
    @POST("app/version/selectNewVersion")
    Flowable<BaseBean<VersionModel>> checkVersion(@Query("genre") int code);

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

    @GET("/yykjZhCommunity/app/opendoor/addOpendoor")
    Flowable<BaseBean> uploadLog(@Query("memberMobile") String memberMobile,
                                 @Query("VistorMobile") String VistorMobile,
                                 @Query("ComID") String ComID,
                                 @Query("UnitID") String UnitID,
                                 @Query("ComdoorID") String ComdoorID,
                                 @Query("orientation") String orientation,
                                 @Query("category") String category,
                                 @Query("addr") String addr,
                                 @Query("lat") String lat,
                                 @Query("lng") String lng);

    /**
     * 心跳发送服务器状态
     *
     * @return
     */
    @POST("pc/multimedia/screen/selectMultimediaMessage")
    Flowable<BaseBean> sendState(@Query("mac") String mac,
                                 @Query("ip_address") String ip_address);

}
