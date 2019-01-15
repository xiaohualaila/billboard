package cn.com.billboard.ui.main;



import android.content.Context;

import cn.com.billboard.ui.base.IBasePresenter;
import cn.com.billboard.ui.base.IBaseView;


/**
 * Created by Administrator on 2017/6/3.
 */

public interface MainContract {
    interface View extends IBaseView<Presenter> {
        void toFragmentImg();
        void toFragmentVideo();
        void toFragmentUpdate();
        void showError(String msg);
        void toUpdateVer(String apkurl, String version);
    }

    interface Presenter extends IBasePresenter {
        void uploadAlarm(String macAddress,int telkey);

        void getScreenData(Context context,boolean isRefresh, String mac, String ipAddress);
    }
}
