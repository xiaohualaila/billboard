package cn.com.billboard.ui.main;



import android.content.Context;

import cn.com.billboard.ui.base.IBasePresenter;
import cn.com.billboard.ui.base.IBaseView;


/**
 * Created by Administrator on 2017/6/3.
 */

public interface MainContract {
    interface View extends IBaseView<Presenter> {
        void showError(String msg);

        void toUpdateVer(String apkurl, String version);

        void toFragemntUpdate();

        void toFragemntMain();

        void toFragemntBigPic();

        void showSubData();

    }

    interface Presenter extends IBasePresenter {

        void getScreenData(boolean isRefresh, String mac, String ipAddress, Context context);

    }
}
