package cn.com.billboard.ui;

import android.os.Bundle;
import android.view.View;
import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;

import cn.com.billboard.net.UserInfoKey;
import cn.com.billboard.util.AppSharePreferenceMgr;
import cn.com.billboard.R;
import cn.com.billboard.present.LauncherPresent;
import cn.com.billboard.widget.LoadingDialog;
import cn.com.library.kit.ToastManager;
import cn.com.library.mvp.XActivity;
import cn.com.library.net.NetError;

public class LauncherActivity extends XActivity<LauncherPresent> {

    public LoadingDialog dialog;

    @Override
    public void initData(Bundle savedInstanceState) {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        dialog = new LoadingDialog(context, "请稍后···");
//        XLog.e("getAllDevices====" + new Gson().toJson(new SerialPortFinder().getAllDevices()));
//        XLog.e("getAllDevicesPath====" + new Gson().toJson(new SerialPortFinder().getAllDevicesPath()));
//        LocationUtil.getInstance().startLocation(context);
        getP().checkPermissions();
    }

    /**
     * 判断是否选择过屏
     */
    public void nextAction() {
        if (((int) AppSharePreferenceMgr.get(context, UserInfoKey.SCREEN_NUM, -1)) == -1)
            selectScreenNum();
        else {
            dialog.show();
            getP().loadData((int) AppSharePreferenceMgr.get(context, UserInfoKey.SCREEN_NUM, -1));
        }
    }

    /**
     * 选择屏幕
     */
    private void selectScreenNum() {
        new AlertView("选择屏幕", null, null, null, new String[]{"室内双屏", "室外大屏",  "拍照", "视频"}, this, AlertView.Style.ActionSheet,
                (o, position) -> {
                    dialog.show();
                    ToastManager.showShort(context, position == 0 ? "室内双屏" : position == 1 ? "室外大屏" : position == 2 ?  "拍照" : "视频");
                    AppSharePreferenceMgr.put(context, UserInfoKey.SCREEN_NUM, position);
                    getP().loadData(position);
                }).show();
    }

    /**
     * 请求返回错误
     */
    public void showError(NetError error) {
        dialog.dismiss();
        if (error != null) {
            switch (error.getType()) {
                case NetError.ParseError:
                    ToastManager.showShort(context, "数据解析异常");
                    break;

                case NetError.AuthError:
                    ToastManager.showShort(context, "身份验证异常");
                    break;

                case NetError.BusinessError:
                    ToastManager.showShort(context, "业务异常");
                    break;

                case NetError.NoConnectError:
                    ToastManager.showShort(context, "网络无连接");
                    break;

                case NetError.NoDataError:
                    ToastManager.showShort(context, "数据为空");
                    break;

                case NetError.OtherError:
                    ToastManager.showShort(context, "其他异常");
                    break;
            }

                CreateParamsActivity.launch(context);
                finish();
        }
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
