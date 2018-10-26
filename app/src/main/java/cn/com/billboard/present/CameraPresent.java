package cn.com.billboard.present;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.view.TextureView;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import cn.com.billboard.net.UserInfoKey;
import cn.com.billboard.ui.CameraActivity;
import cn.com.billboard.util.AppDateMgr;
import cn.com.library.kit.ToastManager;
import cn.com.library.log.XLog;
import cn.com.library.mvp.XPresent;

public class CameraPresent extends XPresent<CameraActivity> {

    private Camera camera;

    private Bitmap bitmap;

    public void takeCamera(){
        if (camera != null) {
            //调用抓拍摄像头抓拍
            camera.takePicture(null, null, pictureCallback);
        } else {
            XLog.e("请检查摄像头！");
        }
    }

    public void takeVideo(){
        if (camera != null) {
            //调用摄像头拍摄视频
            camera.setPreviewCallback(previewCallback);
        } else {
            XLog.e("请检查摄像头！");
        }
    }

    private Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] bytes, Camera camera) {
            if (bytes == null) {
                return;
            }
            String pictureName = AppDateMgr.todayYyyyMmDdHhMmSsTrim();
            ToastManager.showShort(getV(), "图像正在保存……");
            bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            File folder = new File(UserInfoKey.BILLBOARD_PICTURE_PATH);
            if (!folder.exists()){
                folder.mkdirs();
            }
            File file = new File(UserInfoKey.BILLBOARD_PICTURE_PATH + "/" +  pictureName + ".jpg");
            try {
                file.createNewFile();
                BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(file));
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
                os.flush();
                os.close();
                ToastManager.showShort(getV(), "图像保存成功");
                if (camera != null){
                    camera.startPreview();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    private Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] bytes, Camera camera) {
            if (bytes == null) {
                return;
            }
            String pictureName = AppDateMgr.todayYyyyMmDdHhMmSsTrim();
            ToastManager.showShort(getV(), "视频正在保存……");
            File folder = new File(UserInfoKey.RECORD_VIDEO_PATH);
            if (!folder.exists()){
                folder.mkdirs();
            }
            File file = new File(UserInfoKey.RECORD_VIDEO_PATH + "/" +  pictureName + ".mp4");
            try {
//                file.createNewFile();
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(bytes);
                fos.close();
                ToastManager.showShort(getV(), "视频保存成功");
                if (camera != null){
                    camera.startPreview();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    public TextureView.SurfaceTextureListener listener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
            camera = Camera.open(1);
            if (camera != null) {
                try {
                    camera.setPreviewTexture(surfaceTexture);
                    camera.startPreview();
                } catch (IOException e) {
                    XLog.d(e.getMessage());
                }
            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            if (camera != null) {
                camera.stopPreview();
                camera.release();
                camera = null;
            }
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

        }
    };
}
