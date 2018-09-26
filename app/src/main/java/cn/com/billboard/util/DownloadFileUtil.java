package cn.com.billboard.util;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cn.com.billboard.download.DownLoadObserver;
import cn.com.billboard.download.DownloadInfo;
import cn.com.billboard.download.DownloadManager;
import cn.com.billboard.model.ProgressModel;
import cn.com.billboard.net.UserInfoKey;
import cn.com.billboard.present.OneScreenPresent;
import cn.com.billboard.present.TwoScreenPresent;
import cn.com.library.event.BusProvider;
import cn.com.library.log.XLog;

public class DownloadFileUtil {

    private static DownloadFileUtil downloadFileUtil;
    int index =0;

    public static DownloadFileUtil getInstance() {
        if (downloadFileUtil == null) {
            synchronized (ReaderJsonUtil.class) {
                if (downloadFileUtil == null) {
                    downloadFileUtil = new DownloadFileUtil();
                }
            }
        }
        return downloadFileUtil;
    }

    /**
     * 下载主屏图片
     *
     * @param context
     * @param images_url   图片下载的url
     * @param videos_url   视频下载的url
     * @param callBack 回调
     */
    public void downMainLoadPicture(Context context, List<String> images_url, List<String> videos_url, TwoScreenPresent.CallBack callBack) {
        List<String> images  = FileUtil.getCommonFileNames(images_url, UserInfoKey.FILE_MAIN_PICTURE);
        List<String>  videos  = FileUtil.getCommonFileNames(videos_url, UserInfoKey.FILE_MAIN_VIDEO);
        index =0;
        if (images.size() > 0) {
             downMainFilePic( images, callBack, videos);
        } else {
            if (videos.size() > 0) {
                downMainFileVideo(videos, callBack);
            } else {
                callBack.onMainChangeUI();
            }
        }
    }

    /**
     * 下载大屏图片
     * @param images
     * @param callBack
     * @param videos
     */
    public void downMainFilePic( List<String> images,TwoScreenPresent.CallBack  callBack, List<String>  videos){
        DownloadManager.getInstance().download(images.get(index), UserInfoKey.FILE_MAIN_PICTURE, new DownLoadObserver() {
            @Override
            public void onNext(DownloadInfo value) {
                super.onNext(value);
                BusProvider.getBus().post(new ProgressModel(value.getProgress(), value.getTotal(), index+1, images.size(), value.getFileName(), "图片"));
             //   Log.i("sss", " finalI  " + index+1 + " videos size " + images.size() + " FileName " + value.getFileName());
            }

            @Override
            public void onComplete() {
                if (downloadInfo != null) {
                     index ++;
                    if (index == images.size()) {
                        XLog.e("主屏图片下载完成！");
                        if (videos.size() > 0) {
                            index = 0;
                            downMainFileVideo( videos, callBack);
                        } else {
                            callBack.onMainChangeUI();
                        }
                    }else {
                        downMainFilePic(images,callBack, videos);
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
     * 下载大屏视频
     * @param voides
     * @param callBack
     */
    public void downMainFileVideo(List<String> voides,TwoScreenPresent.CallBack  callBack){
        DownloadManager.getInstance().download(voides.get(index), UserInfoKey.FILE_MAIN_VIDEO, new DownLoadObserver() {
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
                    if (index == voides.size()) {//判断视频是否下载完成
           //             XLog.e("主屏视频下载完成！");
                        callBack.onMainChangeUI();
                    }else {
                        downMainFileVideo(voides,callBack);
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
     * 下载主屏视频
     *
     * @param context
     * @param videos   视频下载url
     * @param callBack 回调
     */
//    public void downMainLoadVideo(Context context, List<String> videos, TwoScreenPresent.CallBack callBack) {
//        List<String> files = new ArrayList<>();
//        for (int i=0;i<videos.size();i++) {
//            final int finalI = i;
//            DownloadManager.getInstance().download(videos.get(i), UserInfoKey.FILE_MAIN_VIDEO, new DownLoadObserver() {
//                @Override
//                public void onNext(DownloadInfo value) {
//                    super.onNext(value);
//                    XLog.e("url==" + videos.get(finalI) + "\nprogress===" + value.getProgress() + "/" + value.getTotal());
//                    BusProvider.getBus().post(new ProgressModel( value.getProgress(), value.getTotal(),finalI+1,videos.size(),value.getFileName(),"视频"));
//                    Log.i("sss"," finalI   " +(finalI+1) + "   videos size   " + videos.size() + "  FileName " +value.getFileName());
//                }
//
//                @Override
//                public void onComplete() {
//                    if (downloadInfo != null) {
//                        files.add(downloadInfo.getFilePath() + "/" + downloadInfo.getFileName());
//                        if (files.size() == videos.size()) {//判断视频是否下载完成
//                            XLog.e("主屏视频下载完成！");
//                            callBack.onMainChangeUI();
//                        }
//                    }
//                }
//
//                @Override
//                public void onError(Throwable e) {
//                    super.onError(e);
//                    callBack.onErrorChangeUI(e.getMessage());
//                }
//            });
//        }
//    }

    /**
     * 下载副屏图片
     *
     * @param context
     * @param callBack 回调
     */
    public void downSubLoadPicture(Context context, List<String> images_url, List<String> videos_url, TwoScreenPresent.CallBack callBack) {

        List<String> images  = FileUtil.getCommonFileNames(images_url, UserInfoKey.FILE_SUB_PICTURE);
        List<String>  videos  = FileUtil.getCommonFileNames(videos_url, UserInfoKey.FILE_SUB_VIDEO);

        if (images.size() > 0) {
            List<String> files = new ArrayList<>();
            for (String url : images) {
                DownloadManager.getInstance().download(url, UserInfoKey.FILE_SUB_PICTURE, new DownLoadObserver() {
                    @Override
                    public void onNext(DownloadInfo value) {
                        super.onNext(value);
                        XLog.e("url==" + url + "\nprogress===" + value.getProgress() + "/" + value.getTotal());
                    }

                    @Override
                    public void onComplete() {
                        if (downloadInfo != null) {
                            files.add(downloadInfo.getFilePath() + "/" + downloadInfo.getFileName());
                            if (files.size() == images.size()) {//判断图片是否下载完成
                                XLog.e("副屏图片下载完成！");
                                files.clear();
                                if (videos.size() > 0) {
                                    downSubLoadVideo(context, videos, callBack);
                                } else {
                                    callBack.onSubChangeUI();
                                }
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
        } else {
            if (videos.size() > 0) {
                AppSharePreferenceMgr.put(context, UserInfoKey.SUB_PICTURE_FILE, "[]");//保存图片路径
                downSubLoadVideo(context, videos, callBack);
            } else {
                callBack.onSubChangeUI();
            }
        }

    }


    /**
     * 下载副屏视频
     *
     * @param context
     * @param videos   视频下载url
     * @param callBack 回调
     */
    public void downSubLoadVideo(Context context, List<String> videos, TwoScreenPresent.CallBack callBack) {
        List<String> files = new ArrayList<>();
        for (String url : videos) {
            DownloadManager.getInstance().download(url, UserInfoKey.FILE_SUB_VIDEO, new DownLoadObserver() {
                @Override
                public void onNext(DownloadInfo value) {
                    super.onNext(value);
                    XLog.e("url==" + url + "\nprogress===" + value.getProgress() + "/" + value.getTotal());
                }

                @Override
                public void onComplete() {
                    if (downloadInfo != null) {
                        files.add(downloadInfo.getFilePath() + "/" + downloadInfo.getFileName());
                        if (files.size() == videos.size()) {//判断视频是否下载完成
                            XLog.e("副屏视频下载完成！");
                            callBack.onSubChangeUI();
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
    }

    /**
     * 下载大屏图片
     *
     * @param context
     * @param callBack 回调
     */
    public void downBigLoadPicture(Context context, List<String> images_url, List<String> videos_url, OneScreenPresent.CallBack callBack) {
//        Kits.File.deleteFile(UserInfoKey.FILE_BIG_PICTURE);
//        Kits.File.deleteFile(UserInfoKey.FILE_BIG_VIDEO);

        List<String> images  = FileUtil.getCommonFileNames(images_url, UserInfoKey.FILE_BIG_PICTURE);
        List<String>  videos  = FileUtil.getCommonFileNames(videos_url, UserInfoKey.FILE_BIG_VIDEO);
        index =0;
        if (images.size() > 0) {
            downBigFilePic(images,callBack,videos);
        } else {
            if (videos.size() > 0) {
                downBigFileVideo(videos,callBack);
            } else {
                callBack.onChangeUI();
            }
        }

    }


    /**
     * 下载室外大屏图片
     * @param images
     * @param callBack
     * @param videos
     */
    public void downBigFilePic( List<String> images,OneScreenPresent.CallBack  callBack, List<String>  videos){
        DownloadManager.getInstance().download(images.get(index), UserInfoKey.FILE_BIG_PICTURE, new DownLoadObserver() {
            @Override
            public void onNext(DownloadInfo value) {
                super.onNext(value);
                BusProvider.getBus().post(new ProgressModel(value.getProgress(), value.getTotal(),
                        index+1, images.size(), value.getFileName(), "图片"));
            }

            @Override
            public void onComplete() {
                if (downloadInfo != null) {
                    index ++;
                    if (index == images.size()) {
                        XLog.e("主屏图片下载完成！");
                        if (videos.size() > 0) {
                            index = 0;
                            downBigFileVideo(videos,callBack);
                        } else {
                            callBack.onChangeUI();
                        }
                    }else {
                        downBigFilePic(images,callBack, videos);
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
     * 下载大屏视频
     *
     * @param videos   视频下载url
     * @param callBack 回调
     */
    public void downBigFileVideo(List<String> videos, OneScreenPresent.CallBack callBack) {

            DownloadManager.getInstance().download(videos.get(index), UserInfoKey.FILE_BIG_VIDEO, new DownLoadObserver() {
                @Override
                public void onNext(DownloadInfo value) {
                    super.onNext(value);
                    BusProvider.getBus().post(new ProgressModel(value.getProgress(), value.getTotal(),
                            index+1, videos.size(), value.getFileName(), "视频"));
                }

                @Override
                public void onComplete() {
                    if (downloadInfo != null) {
                        index ++;
                        if (index == videos.size()) {//判断视频是否下载完成
                            callBack.onChangeUI();
                        }else {
                            downBigFileVideo(videos,callBack);
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

}
