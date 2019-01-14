package cn.com.billboard.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import cn.com.billboard.ui.LauncherActivity;

public class SelfOpeningReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED") ) {
            Intent intent2 = new Intent(context, LauncherActivity.class);
            intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent2);
        }
        Toast.makeText(context,"开机启动成功",Toast.LENGTH_LONG).show();
    }
}
