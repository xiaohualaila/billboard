package cn.com.billboard.ui.main;


import java.io.File;

import cn.com.billboard.ui.base.IBasePresenter;
import cn.com.billboard.ui.base.IBaseView;


/**
 * Created by Administrator on 2017/6/3.
 */

public interface MainContract {
    interface View extends IBaseView<Presenter> {


    }

    interface Presenter extends IBasePresenter {
        void uploadAlarm(String macAddress,int telkey);

        void getScreenData(boolean isRefresh,String mac,String ipAddress);

        void uploadAlarmInfo(String macAddress,String recordId,String video_path,String pic_path);
    }
}
