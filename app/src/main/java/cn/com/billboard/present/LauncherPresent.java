package cn.com.billboard.present;

import cn.com.billboard.ui.FragmentActivity;
import cn.com.billboard.ui.FragmentBigScreenActivity;
import cn.com.billboard.ui.LauncherActivity;
import cn.com.library.mvp.XPresent;


public class LauncherPresent extends XPresent<LauncherActivity> {

    public void toActivity(){
//        TwoScreenActivity.launch(getV());
      FragmentActivity.launch(getV());//室内屏切换
      // FragmentBigScreenActivity.launch(getV());//室外屏切换
        getV().finish();
    }



}
