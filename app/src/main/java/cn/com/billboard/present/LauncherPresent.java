package cn.com.billboard.present;

import cn.com.billboard.ui.FragmentActivity;
import cn.com.billboard.ui.FragmentBigScreenActivity;
import cn.com.billboard.ui.LauncherActivity;
import cn.com.library.mvp.XPresent;


public class LauncherPresent extends XPresent<LauncherActivity> {

    public void toActivity(){
//        TwoScreenActivity.launch(getV());
     // FragmentActivity.launch(getV());
       FragmentBigScreenActivity.launch(getV());
        getV().finish();
    }



}
