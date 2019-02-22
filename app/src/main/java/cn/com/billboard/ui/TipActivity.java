package cn.com.billboard.ui;

import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import butterknife.ButterKnife;
import cn.com.billboard.R;
import cn.com.billboard.event.BusProvider;
import cn.com.billboard.model.AlarmRecordModel;
import cn.com.billboard.model.BaseBean;
import cn.com.billboard.model.TipModel;
import cn.com.billboard.retrofitdemo.Request_Interface;
import cn.com.billboard.retrofitdemo.RetrofitManager;
import cn.com.billboard.util.SharedPreferencesUtil;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class TipActivity  extends AppCompatActivity {

    public static final String MAC = "mac";
    private String mac="";
    private int phoneType;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId() );
        ButterKnife.bind(this);
        Intent intent = getIntent();

        mac = intent.getStringExtra(MAC);
        BusProvider.getBus().toFlowable(AlarmRecordModel.class).observeOn(AndroidSchedulers.mainThread()).subscribe(
                (AlarmRecordModel recordModel) -> {
                    if (recordModel.isCalling) {
                        phoneType = recordModel.phoneType;
                        uploadAlarm(mac, phoneType);
                    }
                }
        );
        BusProvider.getBus().toFlowable(TipModel.class).observeOn(AndroidSchedulers.mainThread()).subscribe(
                (TipModel model) -> {
                    if (!model.isHandup) {
                        finish();
                    }
                }
        );
    }

    /**
     * 上传报警
     */
    public void uploadAlarm(String macAddress,int telkey) {
        Request_Interface request = RetrofitManager.getInstance().create(Request_Interface.class);
        request.uploadAlarm(macAddress, telkey)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<BaseBean>() {
                    @Override
                    public void onComplete() {

                    }
                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(TipActivity.this,"网络异常！",Toast.LENGTH_LONG).show();
                    }
                    @Override
                    public void onNext(BaseBean model) {
                        if (model.isSuccess()) {
                            String str = (String) model.getMessageBody();
                            getAlarmId(str);//返回报警ID
                        } else {
                            Toast.makeText(TipActivity.this,"未获取到报警ID！",Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }


    public void getAlarmId(String s) {
        if (!TextUtils.isEmpty(s)) {
            SharedPreferencesUtil.putString(this,"alarmId",s);
            Intent intent = new Intent(this,RecordvideoActivity.class);
            intent.putExtra("mac",mac);
            intent.putExtra("phoneType",phoneType);
            startActivity(intent);
        }
    }

    public int getLayoutId() {
        return R.layout.activity_tip;
    }
}
