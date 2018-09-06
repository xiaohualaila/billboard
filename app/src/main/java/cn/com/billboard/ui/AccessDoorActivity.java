package cn.com.billboard.ui;

import android.app.Activity;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.blankj.utilcode.util.AppUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import cn.com.billboard.R;
import cn.com.billboard.model.AccessModel;
import cn.com.billboard.net.UserInfoKey;
import cn.com.billboard.present.AccessPresent;
import cn.com.billboard.ui.adapter.AccessDoorAdapter;
import cn.com.billboard.util.AppSharePreferenceMgr;
import cn.com.billboard.util.GsonProvider;
import cn.com.library.base.SimpleRecAdapter;
import cn.com.library.kit.ToastManager;
import cn.com.library.log.XLog;
import cn.com.library.mvp.XActivity;
import cn.com.library.router.Router;
import cn.droidlover.xrecyclerview.XRecyclerView;

/**
 * 门禁出入
 */
public class AccessDoorActivity extends XActivity<AccessPresent> {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.open_door_param)
    XRecyclerView param;
    @BindView(R.id.village_id)
    EditText villageId;
    @BindView(R.id.direction_door)
    TextView directionDoor;
    @BindView(R.id.direction_door_down)
    ImageView directionDown;
    @BindView(R.id.building)
    EditText building;
    @BindView(R.id.tv_content)
    TextView tipContent;

    AccessDoorAdapter adapter;

    private String[] direction = {"东门", "西门", "南门", "北门", "楼栋"};

    private PopupWindow popupWindow;

    private List<AccessModel> list;

    @Override
    public void initData(Bundle savedInstanceState) {
        initToolbar();
        initAdapter();
        setAppendContent("门禁终端启动");
        setAppendContent("请设置参数\n参数设置说明:\n小区编号:长度为9，不足前补0，如小区编号为：123456789(正常模式，直接写入即可)，又如编号为：1234,不足9位，前补0，即输入000001234" + "" +
                "\n\n楼栋号:长度为6(可为空)，不足前补0，参考小区编号设置，如:123456 --> 123456 又如:452 --> 000452" + "");
        initViewData();
        getP().initMusic();
        getP().startOpenSerialPort();
    }

    private void initViewData() {
        if (!TextUtils.isEmpty(AppSharePreferenceMgr.get(context, UserInfoKey.OPEN_DOOR_VILLAGE_ID, "").toString()))
            villageId.setText(AppSharePreferenceMgr.get(context, UserInfoKey.OPEN_DOOR_VILLAGE_ID, "").toString());
        else
            villageId.setText("");
        if (!TextUtils.isEmpty(AppSharePreferenceMgr.get(context, UserInfoKey.OPEN_DOOR_DIRECTION_ID, "").toString()))
            directionDoor.setText(AppSharePreferenceMgr.get(context, UserInfoKey.OPEN_DOOR_DIRECTION_ID, "").toString());
        else
            directionDoor.setText("请选择");
        if (!TextUtils.isEmpty(AppSharePreferenceMgr.get(context, UserInfoKey.OPEN_DOOR_BUILDING, "").toString())) {
            building.setVisibility(View.VISIBLE);
            building.setText(AppSharePreferenceMgr.get(context, UserInfoKey.OPEN_DOOR_BUILDING, "").toString());
        } else {
            building.setVisibility(View.INVISIBLE);
            building.setText("");
        }
        list = GsonProvider.stringToList(AppSharePreferenceMgr.get(context, UserInfoKey.OPEN_DOOR_PARAMS, "[]").toString(), AccessModel.class);
        if (list.size() == 0) {
            adapter.setIsSelect(true);
            AccessModel model = new AccessModel();
            model.setErCode(1);
            model.setRelay(0);
            model.setDoorNum("无");
            model.setAccessible("请选择");
            list.add(model);
            findViewById(R.id.add_er_code).setVisibility(View.VISIBLE);
            findViewById(R.id.bt_set).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.add_er_code).setVisibility(View.GONE);
            findViewById(R.id.bt_set).setVisibility(View.GONE);
        }
        adapter.setData(list);
    }

    /**
     * 设置title
     */
    private void initToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("门禁系统");
    }

    private void initAdapter() {
        setLayoutManager(param);
        param.setAdapter(getAdapter());
    }

    private void setLayoutManager(XRecyclerView recyclerView) {
        recyclerView.verticalLayoutManager(context);
    }

    private SimpleRecAdapter getAdapter() {
        if (adapter == null) {
            adapter = new AccessDoorAdapter(context, true);
        }
        return adapter;
    }

    @OnClick({R.id.direction_select, R.id.add_er_code, R.id.bt_set})
    public void clickEvent(View view) {
        switch (view.getId()) {
            case R.id.direction_select:
                if (popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                } else {
                    showSelectWindow(direction, findViewById(R.id.direction_select));
                    directionDown.setImageResource(R.drawable.ic_arrow_drop_up_black);
                }
                break;
            case R.id.add_er_code:
                AccessModel model = new AccessModel();
                model.setErCode(list.size() + 1);
                model.setRelay(0);
                model.setDoorNum("无");
                model.setAccessible("请选择");
                list.add(model);
                adapter.setData(list);
//                if (list.size() == 8) {
//                    findViewById(R.id.add_er_code).setVisibility(View.GONE);
//                }
                break;
            case R.id.bt_set:
                if (checkIsEmpty()) {
//                    AppUtils.relaunchApp();
                    adapter.setIsSelect(false);
                    initViewData();
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
            if (villageId.getText().toString().length() == 9) {
                if ( (!directionDoor.getText().toString().equals("请选择")) && (directionDoor.getText().toString().equals("楼栋"))) {
                    if (TextUtils.isEmpty(building.getText().toString())) {
                        ToastManager.showShort(context, "请设置楼栋号");
                        return false;
                    }
                } else if (GsonProvider.getInstance().getGson().toJson(adapter.getDataSource()).contains("请选择")) {
                    ToastManager.showShort(context, "请设置参数");
                    return false;
                }
                AppSharePreferenceMgr.put(context, UserInfoKey.OPEN_DOOR_VILLAGE_ID, villageId.getText().toString());
                AppSharePreferenceMgr.put(context, UserInfoKey.OPEN_DOOR_DIRECTION_ID, directionDoor.getText().toString());
                AppSharePreferenceMgr.put(context, UserInfoKey.OPEN_DOOR_BUILDING, building.getText().toString());
                AppSharePreferenceMgr.put(context, UserInfoKey.OPEN_DOOR_PARAMS, GsonProvider.getInstance().getGson().toJson(adapter.getDataSource()));
                tipContent.setText("");
                setAppendContent("参数设置成功！");
            } else {
                ToastManager.showShort(context, "请输入正确的小区编号");
                return false;
            }
        }
        return true;
    }

    /**
     * 选择大门朝向或者楼栋
     */
    private void showSelectWindow(final String[] list, View view) {
        View window = LayoutInflater.from(context).inflate(R.layout.layout_drop_down, null);
        popupWindow = new PopupWindow(window, view.getWidth(), (view.getHeight() - 4) * list.length);
        ListView listView = (ListView) window.findViewById(R.id.down_list_view);
        listView.setAdapter(new ArrayAdapter<String>(this, R.layout.item_text, list));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                XLog.e(list[i]);
                directionDoor.setText(list[i]);
                if (list[i].equals("楼栋"))
                    building.setVisibility(View.VISIBLE);
                else
                    building.setVisibility(View.INVISIBLE);
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
            }
        });
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_door, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_empty:
                AppSharePreferenceMgr.put(context, UserInfoKey.OPEN_DOOR_VILLAGE_ID, "");
                AppSharePreferenceMgr.put(context, UserInfoKey.OPEN_DOOR_DIRECTION_ID, "");
                AppSharePreferenceMgr.put(context, UserInfoKey.OPEN_DOOR_BUILDING, "");
                AppSharePreferenceMgr.put(context, UserInfoKey.OPEN_DOOR_PARAMS, "[]");
                tipContent.setText("");
                initViewData();
                ToastManager.showShort(context, "清空参数,请重新设置扫码盒");
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getP().onDestroy();
    }

    public static void launch(Activity activity) {
        Router.newIntent(activity)
                .to(AccessDoorActivity.class)
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
        return R.layout.activity_access_door;
    }

    @Override
    public AccessPresent newP() {
        return new AccessPresent();
    }
}
