package cn.com.billboard.retrofitdemo;

import cn.com.billboard.model.BaseBean;
import cn.com.billboard.model.MessageBodyBean;
import cn.com.billboard.model.TwoScreenModel;
import io.reactivex.Flowable;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Carson_Ho on 17/3/20.
 */

public interface GetRequest_Interface {


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
    Call<ResponseBody> getBigScreenData(
            @Query("devicemac") String mac,
            @Query("deviceip") String deviceip);
}
