package cn.com.billboard.util;

import cn.com.billboard.App;
import cn.com.billboard.util.SDCardUtil;

public class UserInfoKey {

    public static String JSON_DATA = SDCardUtil.getStoragePath(App.getContext());

    public static String MAC = "mac";

    public static String PIC_SMALL_DOWN = SDCardUtil.getStoragePath(App.getContext()) + "/billboard/two/picture/small";
    public static String PIC_BIG_DOWM = SDCardUtil.getStoragePath(App.getContext()) + "/billboard/two/picture/big";
    public static String PIC_UP = SDCardUtil.getStoragePath(App.getContext()) + "/billboard/two/picture/up";
    public static String VIDEO = SDCardUtil.getStoragePath(App.getContext()) + "/billboard/two/video";
    public static String VIDEO_UP = SDCardUtil.getStoragePath(App.getContext()) + "/billboard/two/videoup";


    /**拍照保存路径*/
    public static String RECORD_VIDEO_PATH = SDCardUtil.getStoragePath(App.getContext()) + "/billboard/recordvideo"; // 视频保存路径
    public static String BILLBOARD_PICTURE_FACE_PATH = SDCardUtil.getStoragePath(App.getContext()) + "/billboard/file/face"; // OPENCV抓拍人脸照片

}
