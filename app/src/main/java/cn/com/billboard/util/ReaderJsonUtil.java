package cn.com.billboard.util;

import android.content.Context;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import cn.com.billboard.model.ScreenDetailModel;
import cn.com.billboard.model.ScreenShowModel;
import cn.com.billboard.net.BillboardApi;
import cn.com.billboard.net.UserInfoKey;
import cn.com.library.kit.ToastManager;
import cn.com.library.log.XLog;

/**
 * @fileName: ReaderJsonUtil
 * @author: shangpandeng
 * @date: 2018/6/1 15:45
 * @description: TODO(这里用一句话描述这个类的作用)
 */

public class ReaderJsonUtil {

    private static ReaderJsonUtil readerJsonUtil;

    public static ReaderJsonUtil getInstance() {
        if (readerJsonUtil == null) {
            synchronized (ReaderJsonUtil.class) {
                if (readerJsonUtil == null) {
                    readerJsonUtil = new ReaderJsonUtil();
                }
            }
        }
        return readerJsonUtil;
    }

    public String getJSONData(Context context, String fileName){
        String result = "";
        try {
            InputStream is = context.getAssets().open(fileName);
            int size = is.available();
            byte[] buff = new byte[size];
            is.read(buff);
            is.close();
            result = new String(buff, "utf-8");
            XLog.e(result);
            return result;
        } catch (Exception e){
            e.printStackTrace();
            ToastManager.showShort(context, "没有找到指定的文件");
        }
        return "";
    }

    public String getJSONFile(Context context, String fileName){
        String result = "";
        try {
            File file = new File(UserInfoKey.JSON_DATA + "/" + fileName + ".json");
            int length = (int) file.length();
            byte[] buff = new byte[length];
            FileInputStream fin = new FileInputStream(file);
            fin.read(buff);
            fin.close();
            result = new String(buff, "utf-8");
            XLog.e(result);
            return result;
        } catch (Exception e){
            e.printStackTrace();
            ToastManager.showShort(context, "没有找到指定的文件");
        }
        return "";
    }

    public List[] splitsData(ScreenShowModel model){
        return new List[]{getImages(model), getVideos(model)};
    }

    private List<String> getImages(ScreenShowModel screenShowModel){
        List<String> images = new ArrayList<>();
        for (ScreenDetailModel model : screenShowModel.getPolices()){
            if (model != null &&model.getType() == 2){
                images.add(BillboardApi.API_BASE_URL + model.getDetails().substring(1, model.getDetails().length()));
            }
        }
        for (ScreenDetailModel model : screenShowModel.getPropertys()){
            if (model != null &&model.getType() == 2){
                images.add(BillboardApi.API_BASE_URL + model.getDetails().substring(1, model.getDetails().length()));
            }
        }
        for (ScreenDetailModel model : screenShowModel.getYuanyangs()){
            if (model != null &&model.getType() == 2){
                images.add(BillboardApi.API_BASE_URL + model.getDetails().substring(1, model.getDetails().length()));
            }
        }
        return images;
    }

    private List<String> getImagesName(ScreenShowModel screenShowModel){
        List<String> images = new ArrayList<>();
        for (ScreenDetailModel model : screenShowModel.getPolices()){
            if (model != null &&model.getType() == 2){
                images.add(getUrlFileName(model.getDetails()));
            }
        }
        for (ScreenDetailModel model : screenShowModel.getPropertys()){
            if (model != null &&model.getType() == 2){
                images.add(getUrlFileName(model.getDetails()));
            }
        }
        for (ScreenDetailModel model : screenShowModel.getYuanyangs()){
            if (model != null &&model.getType() == 2){
                images.add(getUrlFileName(model.getDetails()));
            }
        }
        return images;
    }

    private List<String> getVideos(ScreenShowModel screenShowModel){
        List<String> videos = new ArrayList<>();
        for (ScreenDetailModel model : screenShowModel.getPolices()){
            if (model != null &&model.getType() == 3){
                videos.add(BillboardApi.API_BASE_URL + model.getDetails().substring(1, model.getDetails().length()));
            }
        }
        for (ScreenDetailModel model : screenShowModel.getPropertys()){
            if (model != null &&model.getType() == 3){
                videos.add(BillboardApi.API_BASE_URL + model.getDetails().substring(1, model.getDetails().length()));
            }
        }
        for (ScreenDetailModel model : screenShowModel.getYuanyangs()){
            if (model != null &&model.getType() == 3){
                videos.add(BillboardApi.API_BASE_URL + model.getDetails().substring(1, model.getDetails().length()));
            }
        }
        return videos;
    }

    public String getUrlFileName(String resurlt) {
        if (!TextUtils.isEmpty(resurlt)) {
            int nameIndex = resurlt.lastIndexOf("/");
            String loacalname = "";
            if (nameIndex != -1) {
                loacalname = resurlt.substring(nameIndex + 1);
            }

            int index = loacalname.indexOf("?");
            if (index != -1) {
                loacalname = loacalname.substring(0, index);
            }
            return loacalname;
        } else {
            return resurlt;
        }
    }

}
