package cn.com.billboard.ui.main;

import android.text.TextUtils;
import android.util.Log;
import java.io.File;
import java.util.List;
import cn.com.billboard.retrofitdemo.GetRequest_Interface;
import cn.com.billboard.retrofitdemo.PostRequest_Interface;
import cn.com.billboard.ui.base.BasePresenter;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * Created by Administrator on 2017/6/3.
 */

public class MainPresenter extends BasePresenter implements MainContract.Presenter {
    private MainContract.View view;

    public MainPresenter(MainContract.View view) {
        this.view = view;
        this.view.setPresenter(this);
    }

    @Override
    public void start() {

    }


    /**
     * 上传报警
     */
    public void uploadAlarm(String macAddress,int telkey) {
//        Api.getBaseApiWithOutFormat(ConnectUrl.URL)
//                .uploadAlarm(macAddress, telkey)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Action1<JSONObject>() {
//                               @Override
//                               public void call(JSONObject jsonObject) {
//
//                               }
//                           }, new Action1<Throwable>() {
//                               @Override
//                               public void call(Throwable throwable) {
//
//                               }
//                           }
//                );

    }

    public void request() {

        //步骤4:创建Retrofit对象
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://fy.iciba.com/") // 设置 网络请求 Url
                .addConverterFactory(GsonConverterFactory.create()) //设置使用Gson解析(记得加入依赖)
                .build();

        // 步骤5:创建 网络请求接口 的实例
        GetRequest_Interface request = retrofit.create(GetRequest_Interface.class);

        //对 发送请求 进行封装
        Call<ResponseBody> call = request.getBigScreenData("","");

        //步骤6:发送网络请求(异步)
        call.enqueue(new Callback<ResponseBody>() {
            //请求成功时候的回调
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                //请求处理,输出结果

            }

            //请求失败时候的回调
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable throwable) {
                System.out.println("连接失败");
            }
        });
    }

    public void requestPost() {

        //步骤4:创建Retrofit对象
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://fanyi.youdao.com/") // 设置 网络请求 Url
                .addConverterFactory(GsonConverterFactory.create()) //设置使用Gson解析(记得加入依赖)
                .build();

        // 步骤5:创建 网络请求接口 的实例
        PostRequest_Interface request = retrofit.create(PostRequest_Interface.class);

        //对 发送请求 进行封装(设置需要翻译的内容)
//        Call<RequestBody> call = request.getCall("I love you");

        //步骤6:发送网络请求(异步)
//        call.enqueue(new Callback<RequestBody>() {
//
//            //请求成功时回调
//            @Override
//            public void onResponse(Call<RequestBody> call, Response<RequestBody> response) {
//                // 请求处理,输出结果
//                // 输出翻译的内容
//
//            }
//
//            //请求失败时回调
//            @Override
//            public void onFailure(Call<RequestBody> call, Throwable throwable) {
//                System.out.println("请求失败");
//                System.out.println(throwable.getMessage());
//            }
//        });
    }

    /**
     * 获取数据
     */
    public void getScreenData(boolean isRefresh,String mac,String ipAddress) {
//        Api.getBaseApiWithOutFormat(ConnectUrl.URL)
//                .getData(mac, ipAddress)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Action1<JSONObject>() {
//                               @Override
//                               public void call(JSONObject jsonObject) {
//
//                               }
//                           }, new Action1<Throwable>() {
//                               @Override
//                               public void call(Throwable throwable) {
//
//                               }
//                           }
//                );
    }

    /**
     * 上传打电话人员的视频
     */
    public void uploadAlarmInfo(String macAddress,String recordId,String video_path,String pic_path) {
        Log.i("sss","准备上传");

        if(TextUtils.isEmpty(video_path) && TextUtils.isEmpty(pic_path)){
            return;
        }
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        RequestBody requestBody = null;
        if(!TextUtils.isEmpty(video_path)){
            File v_file =new File(video_path);
            if(v_file.exists()){
                requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), v_file);
                builder.addFormDataPart("video", v_file.getName(), requestBody);
            }
        }
        if(!TextUtils.isEmpty(pic_path)){
            File p_file =new File(pic_path);
            if (p_file.exists()){
                //   requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), pic_path);
                //    builder.addFormDataPart("pic", p_file.getName(), requestBody);
                builder.addPart( Headers.of("Content-Disposition", "form-data; name=\"pic\";filename=\"file.jpeg\""),
                        RequestBody.create(MediaType.parse("image/png"),p_file)).build();

            }
        }
        List<MultipartBody.Part> list = null;
        try {
            list = builder.build().parts();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i("sss","开始上传");
//        Api.getBaseApiWithOutFormat(ConnectUrl.URL)
//                .uploadAlarmInfo(macAddress,recordId,list)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Action1<JSONObject>() {
//                               @Override
//                               public void call(JSONObject jsonObject) {
//
//                               }
//                           }, new Action1<Throwable>() {
//                               @Override
//                               public void call(Throwable throwable) {
//
//                               }
//                           }
//                );
    }



}
