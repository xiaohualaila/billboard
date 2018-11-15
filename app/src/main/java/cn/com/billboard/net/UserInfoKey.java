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
    public static String MAC = "mac";
    public static String IPADDRESS = "ip_address";
    public static String SUB_PICTURE_FILE = "sub_picture_file";//副屏图片(本地存储地址)


    //lists_pic_small_dowm,lists_pic_big_dowm,lists_pic_up,lists_video,
    public static String FILE_APK = SDCardUtil.getStoragePath(App.getContext()) + "/billboard/file/apk";
    public static String FILE_MAIN_VIDEO = SDCardUtil.getStoragePath(App.getContext()) + "/billboard/file/main/video";

    public static String PIC_SMALL_DOWN = SDCardUtil.getStoragePath(App.getContext()) + "/billboard/two/picture/small";
    public static String PIC_BIG_DOWM = SDCardUtil.getStoragePath(App.getContext()) + "/billboard/two/picture/big";
    public static String PIC_UP = SDCardUtil.getStoragePath(App.getContext()) + "/billboard/two/picture/up";
    public static String VIDEO = SDCardUtil.getStoragePath(App.getContext()) + "/billboard/two/video";
    public static String VIDEO_UP = SDCardUtil.getStoragePath(App.getContext()) + "/billboard/two/videoup";



    public static String FILE_SUB_VIDEO = SDCardUtil.getStoragePath(App.getContext()) + "/billboard/file/sub/video";
    public static String FILE_SUB_PICTURE = SDCardUtil.getStoragePath(App.getContext()) + "/billboard/file/sub/picture";
    public static String FILE_BIG_VIDEO = SDCardUtil.getStoragePath(App.getContext()) + "/billboard/file/big/video";
    public static String FILE_BIG_PICTURE = SDCardUtil.getStoragePath(App.getContext()) + "/billboard/file/big/picture";
    public static String FILE_MAIN_VIDEO_LOCAL = SDCardUtil.getStoragePath(App.getContext()) + "/billboard/video";

    /**拍照保存路径*/
    public static String BILLBOARD_PICTURE_PATH = SDCardUtil.getStoragePath(App.getContext()) + "/billboard/file/camera"; // 拍照保存路径
    public static String RECORD_VIDEO_PATH = SDCardUtil.getStoragePath(App.getContext()) + "/billboard/recordvideo"; // 视频保存路径
    public static String BILLBOARD_PICTURE_FACE_PATH = SDCardUtil.getStoragePath(App.getContext()) + "/billboard/file/face"; // OPENCV抓拍人脸照片

    public static String MAIN_SHOW_VIDEO_URL = "main_show_video_url";//主屏视频(本地存储URL)
    public static String MAIN_SHOW_PICTURE_URL = "main_show_picture_url";//主屏图片(本地存储URL)
    public static String SUB_SHOW_VIDEO_URL = "sub_show_video_url"; //副屏视频(本地存储URL)
    public static String SUB_SHOW_PICTURE_URL = "sub_show_picture_url"; //副屏图片(本地存储URL)
    public static String BIG_SHOW_VIDEO_URL = "big_show_video_url"; //大屏视频(本地存储URL)
    public static String BIG_SHOW_PICTURE_URL = "big_show_picture_url"; //大屏图片(本地存储URL)
}
