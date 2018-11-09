package cn.com.billboard.model;

public interface CallBack {
    void onMainChangeUI();//主屏回调

    void onMainUpdateUI();//主屏更新

    void onSubChangeUI();//副屏回调

    void onErrorChangeUI(String error);//下载失败无法下载
}
