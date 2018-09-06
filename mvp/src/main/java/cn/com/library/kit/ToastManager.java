package cn.com.library.kit;

import android.content.Context;
import android.support.annotation.StringRes;
import android.widget.Toast;

public class ToastManager {

    private Toast mToast;
    private static ToastManager manager;

    private ToastManager(Context context) {
        if (mToast == null)
            mToast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
    }

    public static ToastManager getInstance(Context context) {
        if (manager == null)
            manager = new ToastManager(context);
        return manager;

    }

    public ToastManager setText(CharSequence msg) {
        mToast.setText(msg);
        return this;
    }

    public ToastManager setText(@StringRes int resId) {
        mToast.setText(resId);
        return this;
    }

    public ToastManager durationShort() {
        mToast.setDuration(Toast.LENGTH_SHORT);
        return this;
    }

    public ToastManager durationLong() {
        mToast.setDuration(Toast.LENGTH_LONG);
        return this;
    }

    public void show() {
        mToast.show();
    }


    public static void showShort(Context context, String msg) {
        getInstance(context)
                .setText(msg)
                .durationShort()
                .show();
    }

    public static void showLong(Context context, String msg) {
        getInstance(context)
                .setText(msg)
                .durationLong()
                .show();
    }

    public static void showShort(Context context, @StringRes int resId) {
        getInstance(context)
                .setText(resId)
                .durationShort()
                .show();
    }

    public static void showLong(Context context, @StringRes int resId) {
        getInstance(context)
                .setText(resId)
                .durationLong()
                .show();
    }


}
