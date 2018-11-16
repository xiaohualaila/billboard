package cn.com.billboard.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.concurrent.TimeUnit;

import cn.com.billboard.model.EventModel;
import cn.com.library.event.BusProvider;
import cn.com.library.event.IBus;
import cn.com.library.log.XLog;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class UpdateService extends Service {

    private static UpdateService updateService;

    public static UpdateService getInstance() {
        if (updateService == null) {
            synchronized (UpdateService.class) {
                if (updateService == null) {
                    updateService = new UpdateService();
                }
            }
        }
        return updateService;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new Binder();
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    public void startTimer(){
        //TODO 启动计时服务
        Observable.timer(5, TimeUnit.MINUTES, AndroidSchedulers.mainThread()).subscribe(new Observer<Long>() {

            @Override
            public void onSubscribe(Disposable d) {
                XLog.e("更新数据倒计时开始");
            }

            @Override
            public void onNext(Long value) {
                BusProvider.getBus().post(new EventModel("refreshData", "refreshData"));
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {
                XLog.e("倒计时结束，开始获取数据");
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
