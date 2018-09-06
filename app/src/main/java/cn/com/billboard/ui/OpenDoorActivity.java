package cn.com.billboard.ui;

import android.app.Activity;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.blankj.utilcode.util.AppUtils;

import butterknife.BindView;
import butterknife.OnClick;
import cn.com.billboard.R;
import cn.com.billboard.net.UserInfoKey;
import cn.com.billboard.present.OpenDoorPresent;
import cn.com.billboard.util.AppSharePreferenceMgr;
import cn.com.library.kit.ToastManager;
import cn.com.library.log.XLog;
import cn.com.library.mvp.XActivity;
import cn.com.library.router.Router;

public class OpenDoorActivity extends XActivity<OpenDoorPresent> {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.village_id)
    EditText villageId;
    @BindView(R.id.unit_id)
    EditText unitId;
    @BindView(R.id.room_id)
    EditText roomId;
    @BindView(R.id.direction_door)
    TextView directionDoor;
    @BindView(R.id.direction_door_down)
    ImageView directionDown;
    @BindView(R.id.open_door)
    TextView openDoor;
    @BindView(R.id.open_door_down)
    ImageView openDown;
    @BindView(R.id.tv_content)
    TextView tipContent;

    private PopupWindow popupWindow;

    private String[] direction = {"东门", "西门", "南门", "北门"};

    private String[] open = {"进", "出"};

    @Override
    public void initData(Bundle savedInstanceState) {
        initToolbar();
        setAppendContent("终端启动");
        setAppendContent("请设置参数\n参数设置说明:\n小区编号:长度为9，不足前补0，如小区编号为：123456789(正常模式，直接写入即可)，又如编号为：1234,不足9位，前补0，即输入000001234" + "" +
                "\n\n单元号:长度为6(可为空)，不足前补0，参考小区编号设置，如:123456 --> 123456 又如:452 --> 000452" + "" +
                "\n\n房间号:长度为6(可为空)，不足前补0，参考单元号设置，如:654321 --> 654321 又如:1002 --> 001002\n");
        initViewData();
        getP().startMusic();
        getP().startOpenSerialPort();
    }

    /**
     * 设置title
     */
    private void initToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("门禁设置");
    }

    /**
     * 初始化数据
     */
    private void initViewData() {
        if (!TextUtils.isEmpty(AppSharePreferenceMgr.get(context, UserInfoKey.OPEN_DOOR_VILLAGE_ID, "").toString())) {
            villageId.setText(AppSharePreferenceMgr.get(context, UserInfoKey.OPEN_DOOR_VILLAGE_ID, "").toString());
        }
        if (!TextUtils.isEmpty(AppSharePreferenceMgr.get(context, UserInfoKey.OPEN_DOOR_DIRECTION_ID, "").toString())) {
            directionDoor.setText(AppSharePreferenceMgr.get(context, UserInfoKey.OPEN_DOOR_DIRECTION_ID, "").toString());
        } else {
            directionDoor.setText(direction[0]);
        }
        if (!TextUtils.isEmpty(AppSharePreferenceMgr.get(context, UserInfoKey.OPEN_DOOR_ENTER_EXIT_ID, "").toString())) {
            openDoor.setText(AppSharePreferenceMgr.get(context, UserInfoKey.OPEN_DOOR_ENTER_EXIT_ID, "").toString());
        } else {
            openDoor.setText(open[0]);
        }
        if (!TextUtils.isEmpty(AppSharePreferenceMgr.get(context, UserInfoKey.OPEN_DOOR_UNIT_ID, "").toString())) {
            unitId.setText(AppSharePreferenceMgr.get(context, UserInfoKey.OPEN_DOOR_UNIT_ID, "").toString());
        }
        if (!TextUtils.isEmpty(AppSharePreferenceMgr.get(context, UserInfoKey.OPEN_DOOR_ROOM_ID, "").toString())) {
            roomId.setText(AppSharePreferenceMgr.get(context, UserInfoKey.OPEN_DOOR_ROOM_ID, "").toString());
        }
    }

    @OnClick({R.id.direction_select, R.id.open_select, R.id.bt_set})
    public void clickEvent(View view) {
        switch (view.getId()) {
            case R.id.direction_select:
                if (popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                } else {
                    showSelectWindow(1, direction, findViewById(R.id.direction_select));
                    directionDown.setImageResource(R.drawable.ic_arrow_drop_up_black);
                }
                break;
            case R.id.open_select:
                if (popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                } else {
                    showSelectWindow(2, open, findViewById(R.id.open_select));
                    openDown.setImageResource(R.drawable.ic_arrow_drop_up_black);
                }
                break;
            case R.id.bt_set:
                if (checkIsEmpty()) {
                    AppUtils.relaunchApp();
                }
                break;
        }
    }

    /**
     * 校验输入是否为空、添加提示
     */
    private boolean checkIsEmpty() {
        if (TextUtils.isEmpty(villageId.getText().toString())) {
            ToastManager.showShort(context, "请设置小区编号");
            return false;
        } else {
            if (!TextUtils.isEmpty(roomId.getText().toString())) {
                if (TextUtils.isEmpty(unitId.getText().toString())) {
                    ToastManager.showShort(context, "请设置单元号");
                    return false;
                }
            }
            AppSharePreferenceMgr.put(context, UserInfoKey.OPEN_DOOR_VILLAGE_ID, villageId.getText().toString());
            AppSharePreferenceMgr.put(context, UserInfoKey.OPEN_DOOR_DIRECTION_ID, directionDoor.getText().toString());
            AppSharePreferenceMgr.put(context, UserInfoKey.OPEN_DOOR_ENTER_EXIT_ID, openDoor.getText().toString());
            AppSharePreferenceMgr.put(context, UserInfoKey.OPEN_DOOR_UNIT_ID, TextUtils.isEmpty(unitId.getText().toString()) ? "000000" : unitId.getText().toString());
            AppSharePreferenceMgr.put(context, UserInfoKey.OPEN_DOOR_ROOM_ID, TextUtils.isEmpty(roomId.getText().toString()) ? "000000" : roomId.getText().toString());
        }
        return true;
    }

    /**
     * 添加提示
     */
    public void setAppendContent(String content) {
        if (!TextUtils.isEmpty(content)) {
            tipContent.append("\n" + content);
        } else {
            tipContent.append(content);
        }
    }

    /**
     * 选择大门朝向以及进出方式窗口
     */
    private void showSelectWindow(final int type, final String[] list, View view) {
        View window = LayoutInflater.from(context).inflate(R.layout.layout_drop_down, null);
        popupWindow = new PopupWindow(window, view.getWidth(), (view.getHeight() - 4) * list.length);
        ListView listView = (ListView) window.findViewById(R.id.down_list_view);
        listView.setAdapter(new ArrayAdapter<String>(this, R.layout.item_text, list));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                XLog.e(list[i]);
                if (type == 1) directionDoor.setText(list[i]);
                else openDoor.setText(list[i]);
                popupWindow.dismiss();
            }
        });
        popupWindow.setFocusable(true);// 使其聚集
        popupWindow.setTouchable(true); // 设置允许在外点击消失/
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        //点击其他地方消失
        popupWindow.setOutsideTouchable(true);
        popupWindow.showAsDropDown(view);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                directionDown.setImageResource(R.drawable.ic_arrow_drop_down_black);
                openDown.setImageResource(R.drawable.ic_arrow_drop_down_black);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getP().onDestroy();
    }

    public static void launch(Activity activity) {
        Router.newIntent(activity)
                .to(OpenDoorActivity.class)
                .launch();
    }

    //记录用户首次点击返回键的时间
    private long firstTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                long secondTime = System.currentTimeMillis();
                if (secondTime - firstTime > 2000) {
                    ToastManager.showShort(context, "再按一次退出");
                    firstTime = secondTime;
                    return true;
                } else {
                    System.exit(0);
                }
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }



    @Override
    public int getLayoutId() {
        return R.layout.activity_open_door;
    }

    @Override
    public OpenDoorPresent newP() {
        return new OpenDoorPresent();
    }
}
