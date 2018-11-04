package cn.com.billboard.ui;

import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import butterknife.BindView;
import cn.com.billboard.R;
import cn.com.billboard.present.LauncherPresent;
import cn.com.billboard.util.APKVersionCodeUtils;
import cn.com.library.mvp.XActivity;


public class LauncherActivity extends XActivity<LauncherPresent>  {
    @BindView(R.id.ver_name)
    TextView ver_name;

    Handler handler = new Handler();
    @Override
    public void initData(Bundle savedInstanceState) {
        String v_name = APKVersionCodeUtils.getVerName(this);
        ver_name.setText("当前版本 "+v_name);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
               getP().checkPermissions();
            }
        },3000);

    }


    @Override
    public int getLayoutId() {
        return R.layout.activity_launcher;
    }

    @Override
    public LauncherPresent newP() {
        return new LauncherPresent();
    }

}
