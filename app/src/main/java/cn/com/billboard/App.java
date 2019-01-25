package cn.com.billboard;

import android.app.Application;
import android.content.Context;

import com.doormaster.vphone.inter.DMVPhoneModel;
import com.thinmoo.utils.ChangeServerUtil;
import com.thinmoo.utils.ServerContainer;


/**
 * Created by wanglei on 2016/12/31.
 */

public class App extends Application {

    private static Context context;
    public static final ServerContainer YUANYANG_APP_SERVER = new ServerContainer("117.36.77.242", "8099", "远洋服务器");
    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        //初始化西墨可视对讲sdk
        DMVPhoneModel.initDMVPhoneSDK(this,"DDemo",false,false);
        DMVPhoneModel.enableCallPreview(true,this);//打开预览消息界面显示
        ChangeServerUtil.getInstance().initConfig(this);
        ChangeServerUtil.getInstance().setAppServer(YUANYANG_APP_SERVER);

    }

    public static Context getContext() {
        return context;
    }

}
