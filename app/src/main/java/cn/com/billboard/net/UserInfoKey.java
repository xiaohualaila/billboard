package cn.com.billboard.net;

import cn.com.billboard.App;
import cn.com.billboard.util.SDCardUtil;

public class UserInfoKey {

    public static String JSON_DATA = SDCardUtil.getStoragePath(App.getContext());

    /** 多媒体*/
    public static String SCREEN_NUM = "screen_num";
    public static String MAIN_SCREEN_IP = "main_screen_ip"; //主屏ip
    public static String SUB_SCREEN_IP = "sub_screen_ip"; // 副屏ip
    public static String BIG_SCREEN_IP = "big_screen_ip"; // 大屏ip

    public static String MAIN_SCREEN_ID = "main_screen_id";//主屏id
    public static String SUB_SCREEN_ID = "sub_screen_id"; // 副屏id
    public static String BIG_SCREEN_ID = "sub_screen_id"; // 大屏id


    public static String SUB_PICTURE_FILE = "sub_picture_file";//副屏图片(本地存储地址)



    public static String FILE_APK = SDCardUtil.getStoragePath(App.getContext()) + "/billboard/file/apk";
    public static String FILE_MAIN_VIDEO = SDCardUtil.getStoragePath(App.getContext()) + "/billboard/file/main/video";
    public static String FILE_MAIN_PICTURE = SDCardUtil.getStoragePath(App.getContext()) + "/billboard/file/main/picture";
    public static String FILE_SUB_VIDEO = SDCardUtil.getStoragePath(App.getContext()) + "/billboard/file/sub/video";
    public static String FILE_SUB_PICTURE = SDCardUtil.getStoragePath(App.getContext()) + "/billboard/file/sub/picture";
    public static String FILE_BIG_VIDEO = SDCardUtil.getStoragePath(App.getContext()) + "/billboard/file/big/video";
    public static String FILE_BIG_PICTURE = SDCardUtil.getStoragePath(App.getContext()) + "/billboard/file/big/picture";


    /**拍照保存路径*/
    public static String BILLBOARD_PICTURE_PATH = SDCardUtil.getStoragePath(App.getContext()) + "/billboard/file/camera"; // 拍照保存路径
    public static String RECORD_VIDEO_PATH = SDCardUtil.getStoragePath(App.getContext()) + "/billboard/recordvideo"; // 视频保存路径
    public static String BILLBOARD_PICTURE_FACE_PATH = SDCardUtil.getStoragePath(App.getContext()) + "/billboard/file/face"; // OPENCV抓拍人脸照片
}
