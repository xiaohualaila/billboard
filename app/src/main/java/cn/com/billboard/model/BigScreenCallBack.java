package cn.com.billboard.model;

public interface BigScreenCallBack {
    void onScreenChangeUI();//主屏回调

    void onErrorChangeUI(String error);//下载失败无法下载
}
