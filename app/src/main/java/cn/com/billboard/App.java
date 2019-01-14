package cn.com.billboard;

import android.app.Application;
import android.content.Context;


/**
 * Created by wanglei on 2016/12/31.
 */

public class App extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;


    }

    public static Context getContext() {
        return context;
    }

}
