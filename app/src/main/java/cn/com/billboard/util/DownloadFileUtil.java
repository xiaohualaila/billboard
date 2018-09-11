package cn.com.billboard.util;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;
import cn.com.billboard.download.DownLoadObserver;
import cn.com.billboard.download.DownloadInfo;
import cn.com.billboard.download.DownloadManager;
import cn.com.billboard.model.EventModel;
import cn.com.billboard.model.ProgressModel;
import cn.com.billboard.net.UserInfoKey;
import cn.com.billboard.present.OneScreenPresent;
import cn.com.billboard.present.TwoScreenPresent;
import cn.com.library.event.BusProvider;
import cn.com.library.kit.Kits;
import cn.com.library.log.XLog;

public class DownloadFileUtil {

    private static DownloadFileUtil downloadFileUtil;


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
        List<String> images  = FileUtil.getCommonFileNames(images_url,UserInfoKey.FILE_MAIN_PICTURE);
        List<String>  videos  = FileUtil.getCommonFileNames(videos_url,UserInfoKey.FILE_MAIN_VIDEO);

        if (images.size() > 0) {
            List<String> files = new ArrayList<>();
//            for (String url : images) {
            for (int i=0;i<images.size();i++) {

                final int finalI = i;
                DownloadManager.getInstance().download(images.get(i), UserInfoKey.FILE_MAIN_PICTURE, new DownLoadObserver() {
                    @Override
                    public void onNext(DownloadInfo value) {
                        super.onNext(value);
                        XLog.e("url==" + images.get(finalI) + "\nprogress===" + value.getProgress() + "/" + value.getTotal());
                   //     Log.i("xxx"," 进度>>>>>>>>" + value.getProgress() +" 总进度>>>>>>>>" +value.getTotal() );
                        BusProvider.getBus().post(new ProgressModel( value.getProgress(), value.getTotal(),finalI,images.size(),value.getFileName(),"图片"));
                    }

                    @Override
                    public void onComplete() {
                        if (downloadInfo != null) {
                            files.add(downloadInfo.getFilePath() + "/" + downloadInfo.getFileName());
                            if (files.size() == images.size()) {
                                XLog.e("主屏图片下载完成！");
                                files.clear();
                                if (videos.size() > 0) {
                                    downMainLoadVideo(context, videos, callBack);
                                } else {
                                    callBack.onMainChangeUI();
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                    }
                });
            }
        } else {
            if (videos.size() > 0) {
                downMainLoadVideo(context, videos, callBack);
            } else {
                callBack.onMainChangeUI();
            }
        }
    }




    /**
     * 下载主屏视频
     *
     * @param context
     * @param videos   视频下载url
     * @param callBack 回调
     */
    public void downMainLoadVideo(Context context, List<String> videos, TwoScreenPresent.CallBack callBack) {
        List<String> files = new ArrayList<>();
//        for (String url : videos) {
        for (int i=0;i<videos.size();i++) {
            final int finalI = i;
            DownloadManager.getInstance().download(videos.get(i), UserInfoKey.FILE_MAIN_VIDEO, new DownLoadObserver() {
                @Override
                public void onNext(DownloadInfo value) {
                    super.onNext(value);
                    XLog.e("url==" + videos.get(finalI) + "\nprogress===" + value.getProgress() + "/" + value.getTotal());

                    BusProvider.getBus().post(new ProgressModel( value.getProgress(), value.getTotal(),finalI,videos.size(),value.getFileName(),"视频"));
                }

                @Override
                public void onComplete() {
                    if (downloadInfo != null) {
                        files.add(downloadInfo.getFilePath() + "/" + downloadInfo.getFileName());
                        if (files.size() == videos.size()) {//判断视频是否下载完成
                            XLog.e("主屏视频下载完成！");
                            callBack.onMainChangeUI();
                        }
                    }
                }

                @Override
                public void onError(Throwable e) {
                    super.onError(e);
                }
            });
        }
    }

    /**
     * 下载副屏图片
     *
     * @param context
     * @param callBack 回调
     */
    public void downSubLoadPicture(Context context, List<String> images_url, List<String> videos_url, TwoScreenPresent.CallBack callBack) {

        List<String> images  = FileUtil.getCommonFileNames(images_url,UserInfoKey.FILE_SUB_PICTURE);
        List<String>  videos  = FileUtil.getCommonFileNames(videos_url,UserInfoKey.FILE_SUB_VIDEO);

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
                }
            });
        }
    }

    /**
     * 下载大屏图片
     *
     * @param context
     * @param images   图片下载的url
     * @param videos   视频下载的url
     * @param callBack 回调
     */
    public void downBigLoadPicture(Context context, List<String> images, List<String> videos, OneScreenPresent.CallBack callBack) {
        Kits.File.deleteFile(UserInfoKey.FILE_BIG_PICTURE);
        Kits.File.deleteFile(UserInfoKey.FILE_BIG_VIDEO);
        if (images.size() > 0) {
            List<String> files = new ArrayList<>();
            for (String url : images) {
                DownloadManager.getInstance().download(url, UserInfoKey.FILE_BIG_PICTURE, new DownLoadObserver() {
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
                                XLog.e("大屏图片下载完成！");
                                files.clear();
                                for (String path : images){
                                    files.add(UserInfoKey.FILE_BIG_PICTURE + "/" + ReaderJsonUtil.getInstance().getUrlFileName(path));
                                }
                                AppSharePreferenceMgr.put(context, UserInfoKey.BIG_PICTURE_FILE, new Gson().toJson(files));//保存图片路径
                                if (videos.size() > 0) {
                                    downBigLoadVideo(context, videos, callBack);
                                } else {
                                    AppSharePreferenceMgr.put(context, UserInfoKey.BIG_VIDEO_FILE, "[]");//保存视频路径
                                    callBack.onChangeUI();
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                    }
                });
            }
        } else {
            if (videos.size() > 0) {
                AppSharePreferenceMgr.put(context, UserInfoKey.BIG_PICTURE_FILE, "[]");//保存图片路径
                downBigLoadVideo(context, videos, callBack);
            } else {
                callBack.onChangeUI();
            }
        }

    }

    /**
     * 下载大屏视频
     *
     * @param context
     * @param videos   视频下载url
     * @param callBack 回调
     */
    public void downBigLoadVideo(Context context, List<String> videos, OneScreenPresent.CallBack callBack) {
        List<String> files = new ArrayList<>();
        for (String url : videos) {
            DownloadManager.getInstance().download(url, UserInfoKey.FILE_BIG_VIDEO, new DownLoadObserver() {
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
                            XLog.e("大屏视频下载完成！");
                            AppSharePreferenceMgr.put(context, UserInfoKey.BIG_VIDEO_FILE, new Gson().toJson(files));//保存视频路径
                            callBack.onChangeUI();
                        }
                    }
                }

                @Override
                public void onError(Throwable e) {
                    super.onError(e);
                }

            });
        }
    }

}
