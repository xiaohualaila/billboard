package cn.com.billboard.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.Window;
import android.widget.TextView;

import com.wang.avi.AVLoadingIndicatorView;

import cn.com.billboard.R;

public class LoadingDialog extends Dialog {

    private TextView loadingTip;

    private String tipText;

    public LoadingDialog(@NonNull Context context) {
        super(context, R.style.MyDialog_pro);
    }

    public LoadingDialog(@NonNull Context context, String tipText) {
        super(context, R.style.MyDialog_pro);
        this.tipText = tipText;
    }

    public LoadingDialog showDialog(Context context, String tipText){
        LoadingDialog dialog = new LoadingDialog(context, tipText);
        return dialog;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.view_loading);
        initView();
    }

    private void initView() {
        this.setCanceledOnTouchOutside(false);
        loadingTip = (TextView) findViewById(R.id.loading_tip);
        loadingTip.setText(TextUtils.isEmpty(tipText) ? "加载中···" : tipText);
    }

    @Override
    public void dismiss() {
        if (this != null && this.isShowing()) {
            super.dismiss();
        }
    }

}
