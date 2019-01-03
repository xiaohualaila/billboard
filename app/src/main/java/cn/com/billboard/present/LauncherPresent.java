package cn.com.billboard.present;

import cn.com.billboard.ui.FragmentActivity;
import cn.com.billboard.ui.LauncherActivity;
import cn.com.library.mvp.XPresent;


public class LauncherPresent extends XPresent<LauncherActivity> {

    public void toActivity(){
        FragmentActivity.launch(getV());
        getV().finish();
    }



}
