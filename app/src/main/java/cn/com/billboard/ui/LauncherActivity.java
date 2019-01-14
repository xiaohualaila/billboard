package cn.com.billboard.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import cn.com.billboard.ui.main.MainActivity;
import cn.com.billboard.util.APKVersionCodeUtils;



public class LauncherActivity extends AppCompatActivity {
    @BindView(R.id.ver_name)
    TextView ver_name;

    Handler handler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        ButterKnife.bind(this);
        String v_name = APKVersionCodeUtils.getVerName(this);
        ver_name.setText("当前版本 "+v_name);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                toActivity();
            }
        },3000);

    }

    private void toActivity() {
        startActivity(new Intent(this,MainActivity.class));
    }


    public int getLayoutId() {
        return R.layout.activity_launcher;
    }



}
