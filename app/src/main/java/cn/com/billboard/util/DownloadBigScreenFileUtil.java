package cn.com.billboard.util;

import java.util.List;
import cn.com.billboard.download.DownLoadObserver;
import cn.com.billboard.download.DownloadInfo;
import cn.com.billboard.download.DownloadManager;
import cn.com.billboard.event.BusProvider;
import cn.com.billboard.model.BigScreenCallBack;
import cn.com.billboard.model.ProgressModel;


public class DownloadBigScreenFileUtil {

    private static DownloadBigScreenFileUtil downloadFileUtil;
    int index =0;

    public static DownloadBigScreenFileUtil getInstance() {
        if (downloadFileUtil == null) {
            synchronized (ReaderJsonUtil.class) {
                if (downloadFileUtil == null) {
                    downloadFileUtil = new DownloadBigScreenFileUtil();
                }
            }
        }
        return downloadFileUtil;
    }

    /**
     * 下载主屏图片
     * @param images  图片
     * @param videos   下屏视频
     * @param callBack 回调
     */
    public void down( List<String> images, List<String> videos,BigScreenCallBack callBack) {
        if (images.size() > 0) {
            index =0;
            downBigScreenFilePic( images,videos, callBack,UserInfoKey.PIC_BIG_IMAGE_DOWN,"图片");
             return;
        }

        if (videos.size() > 0) {
            index =0;
            downBigScreenFileVideo(videos, callBack,UserInfoKey.BIG_VIDEO);
            return;
        }else {
            callBack.onScreenChangeUI();
        }
    }

    /**
     * 下载图片
     * @param images
     * @param callBack
     * @param
     */
    public void downBigScreenFilePic(List<String> images,List<String> videos, BigScreenCallBack callBack, String url_type, String screen_name){

        DownloadManager.getInstance().download(images.get(index),url_type, new DownLoadObserver() {
            @Override
            public void onNext(DownloadInfo value) {
                super.onNext(value);
                BusProvider.getBus().post(new ProgressModel(value.getProgress(), value.getTotal(), index+1, images.size(), value.getFileName(), screen_name));
            //    Log.i("sss", " finalI  " + index+1 + " videos size " + images.size() + " FileName " + value.getFileName());
            }

            @Override
            public void onComplete() {
                if (downloadInfo != null) {
                     index ++;
                    if (index == images.size()) {
                        index=0;
                        downBigScreenFileVideo(videos, callBack,UserInfoKey.BIG_VIDEO);
                    }else {
                        downBigScreenFilePic(images,videos,callBack,url_type,screen_name);
                    }
                }
            }

            @Override
            public void onError(Throwable e) {
                super.onError(e);
                callBack.onErrorChangeUI(e.getMessage());
            }
        });
    }

    /**
     * 下载室内屏视频
     * @param voides
     * @param callBack
     */
    public void downBigScreenFileVideo(List<String> voides, BigScreenCallBack callBack, String url_type){
        if(voides.size()>0){
            DownloadManager.getInstance().download(voides.get(index), url_type, new DownLoadObserver() {
                @Override
                public void onNext(DownloadInfo value) {
                    super.onNext(value);
                    BusProvider.getBus().post(new ProgressModel(value.getProgress(), value.getTotal(), index+1, voides.size(), value.getFileName(), "视频"));
               //     Log.i("sss", " finalI  " + index+1 + " videos size " + voides.size() + " FileName " + value.getFileName());
                }

                @Override
                public void onComplete() {
                    if (downloadInfo != null) {
                        index ++;
                        if (index == voides.size()) {
                            callBack.onScreenChangeUI();
                        }else {
                            downBigScreenFileVideo(voides,callBack,url_type);
                        }
                    }
                }

                @Override
                public void onError(Throwable e) {
                    super.onError(e);
                    callBack.onErrorChangeUI(e.getMessage());
                }
            });
        }else {
            callBack.onScreenChangeUI();
        }
    }
}
