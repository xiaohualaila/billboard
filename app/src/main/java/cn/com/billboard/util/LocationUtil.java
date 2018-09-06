package cn.com.billboard.util;

import android.content.Context;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

import cn.com.billboard.App;
import cn.com.library.kit.ToastManager;
import cn.com.library.log.XLog;

public class LocationUtil {

    private static LocationUtil locationUtil;

    private AMapLocationClient locationClient;

    public static LocationUtil getInstance() {
        if (locationUtil == null) {
            synchronized (LocationUtil.class) {
                if (locationUtil == null) {
                    locationUtil = new LocationUtil();
                }
            }
        }
        return locationUtil;
    }

    public void startLocation(Context context){
        locationClient = new AMapLocationClient(context);
        locationClient.setLocationOption(getOption());
        locationClient.setLocationListener(listener);
        locationClient.startLocation();
    }

    private AMapLocationClientOption getOption(){
        AMapLocationClientOption option = new AMapLocationClientOption();
        option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);//可选，设置定位模式，可选的模式有高精度、仅设备、仅网络。默认为高精度模式
        option.setGpsFirst(false);//可选，设置是否gps优先，只在高精度模式下有效。默认关闭
        option.setHttpTimeOut(30000);//可选，设置网络请求超时时间。默认为30秒。在仅设备模式下无效
        option.setInterval(2000);//可选，设置定位间隔。默认为2秒
        option.setNeedAddress(true);//可选，设置是否返回逆地理地址信息。默认是true
        option.setOnceLocation(false);//可选，设置是否单次定位。默认是false
//        option.setOnceLocationLatest(false);//可选，设置是否等待wifi刷新，默认为false.如果设置为true,会自动变为单次定位，持续定位时不要使用
        AMapLocationClientOption.setLocationProtocol(AMapLocationClientOption.AMapLocationProtocol.HTTP);//可选， 设置网络请求的协议。可选HTTP或者HTTPS。默认为HTTP
        option.setSensorEnable(false);//可选，设置是否使用传感器。默认是false
        option.setWifiScan(true); //可选，设置是否开启wifi扫描。默认为true，如果设置为false会同时停止主动刷新，停止以后完全依赖于系统刷新，定位位置可能存在误差
        option.setLocationCacheEnable(true); //可选，设置是否使用缓存定位，默认为true
        return option;
    }

    AMapLocationListener listener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            if (null != aMapLocation) {
                //解析定位结果
                XLog.e("aMapLocation=====" + GsonProvider.getInstance().getGson().toJson(aMapLocation));
                stopLocation();
            } else {
                ToastManager.showShort(App.getContext(), "定位失败，loc is null");
            }
        }
    };

    private void stopLocation(){
        locationClient.stopLocation();
    }

}
