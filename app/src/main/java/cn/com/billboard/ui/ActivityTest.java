package cn.com.billboard.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import cn.com.billboard.R;

import cn.com.library.log.XLog;

public class ActivityTest extends AppCompatActivity{


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        XLog.e("sss",">>>>>>>>>>>>>>>>>>");

    }



    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}
