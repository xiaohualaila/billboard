package cn.com.billboard.ui;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.media.FaceDetector;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.BindView;
import butterknife.OnClick;
import cn.com.billboard.R;
import cn.com.billboard.net.UserInfoKey;
import cn.com.billboard.util.AppDateMgr;
import cn.com.billboard.widget.FindFaceView;
import cn.com.library.kit.ToastManager;
import cn.com.library.mvp.XActivity;
import cn.com.library.router.Router;

public class FaceActivity extends XActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.preview)
    SurfaceView preview;
    @BindView(R.id.my_preview)
    FindFaceView mFindFaceView;
    @BindView(R.id.iv_photo)
    ImageView iv_photo;
    @BindView(R.id.bt_camera)
    Button bt_camera;
    @BindView(R.id.show_count)
    TextView mTV;
    private Camera camera;
    private Camera.Parameters parameters;
    private int orientionOfCamera;// 前置摄像头的安装角度
    private int faceNumber;// 识别的人脸数
    private FaceDetector.Face[] faces;

    @Override
    public void initData(Bundle savedInstanceState) {
        initToolbar();
        // 设置缓冲类型
        preview.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        // 设置surface的分辨率
        preview.getHolder().setFixedSize(176, 144);
        // 设置屏幕常亮
        preview.getHolder().setKeepScreenOn(true);
        preview.getHolder().addCallback(new SurfaceCallback());
    }

    private final class MyPictureCallback implements Camera.PictureCallback {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            try {
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0,
                        data.length);
                Matrix matrix = new Matrix();
                matrix.setRotate(-90);
                Bitmap bmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap
                        .getWidth(), bitmap.getHeight(), matrix, true);
                bitmap.recycle();
                iv_photo.setImageBitmap(bmp);
                camera.startPreview();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private final class SurfaceCallback implements SurfaceHolder.Callback {

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                   int height) {
            if (camera != null) {
                parameters = camera.getParameters();
                parameters.setPictureFormat(PixelFormat.JPEG);
                // 设置预览区域的大小
                parameters.setPreviewSize(width, height);
                // 设置每秒钟预览帧数
                parameters.setPreviewFrameRate(20);
                // 设置预览图片的大小
                parameters.setPictureSize(width, height);
                parameters.setJpegQuality(80);
            }
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            int cameraCount = 0;
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            cameraCount = Camera.getNumberOfCameras();
            for (int i = 0; i < cameraCount; i++) {
                Camera.getCameraInfo(i, cameraInfo);
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    try {
                        camera = Camera.open(i);
                        camera.setPreviewDisplay(holder);
                        setCameraDisplayOrientation(i, camera);
                        camera.setPreviewCallback(new MyPreviewCallback());
                        camera.startPreview();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            if (camera != null) {
                camera.setPreviewCallback(null);
                camera.stopPreview();
                camera.release();
                camera = null;
            }
        }
    }

    private class MyPreviewCallback implements Camera.PreviewCallback {

        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            Camera.Size size = camera.getParameters().getPreviewSize();
            YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21,
                    size.width, size.height, null);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            yuvImage.compressToJpeg(new Rect(0, 0, size.width, size.height),
                    80, baos);
            byte[] byteArray = baos.toByteArray();
            detectionFaces(byteArray);
        }
    }

    /**
     * 检测人脸
     *
     * @param data 预览的图像数据
     */
    private void detectionFaces(byte[] data) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap bitmap1 = BitmapFactory.decodeByteArray(data, 0, data.length,
                options);
        int width = bitmap1.getWidth();
        int height = bitmap1.getHeight();
        Matrix matrix = new Matrix();
        Bitmap bitmap2 = null;
        FaceDetector detector = null;

        switch (orientionOfCamera) {
            case 0:
                detector = new FaceDetector(width, height, 10);
                matrix.postRotate(0.0f, width / 2, height / 2);
                // 以指定的宽度和高度创建一张可变的bitmap（图片格式必须是RGB_565，不然检测不到人脸）
                bitmap2 = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
                break;
            case 90:
                detector = new FaceDetector(height, width, 1);
                matrix.postRotate(-270.0f, height / 2, width / 2);
                bitmap2 = Bitmap.createBitmap(height, width, Bitmap.Config.RGB_565);
                break;
            case 180:
                detector = new FaceDetector(width, height, 1);
                matrix.postRotate(-180.0f, width / 2, height / 2);
                bitmap2 = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
                break;
            case 270:
                detector = new FaceDetector(height, width, 1);
                matrix.postRotate(-90.0f, height / 2, width / 2);
                bitmap2 = Bitmap.createBitmap(height, width, Bitmap.Config.RGB_565);
                break;
        }

        faces = new FaceDetector.Face[10];
        Paint paint = new Paint();
        paint.setDither(true);
        Canvas canvas = new Canvas();
        canvas.setBitmap(bitmap2);
        canvas.setMatrix(matrix);
        // 将bitmap1画到bitmap2上（这里的偏移参数根据实际情况可能要修改）
        canvas.drawBitmap(bitmap1, 0, 0, paint);
        faceNumber = detector.findFaces(bitmap2, faces);
        mTV.setText("人脸数：----" + faceNumber);
        mTV.setTextColor(Color.RED);
        if (faceNumber != 0) {
            mFindFaceView.setVisibility(View.VISIBLE);
            mFindFaceView.drawRect(faces, faceNumber);
            File folder = new File(UserInfoKey.BILLBOARD_PICTURE_PATH);
            if (!folder.exists()) {
                folder.mkdirs();
            }
            File file = new File(UserInfoKey.BILLBOARD_PICTURE_PATH + "/" + AppDateMgr.todayYyyyMmDdHhMmSsTrim() + ".jpg");
            try {
                file.createNewFile();
                BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(file));
                bitmap2.compress(Bitmap.CompressFormat.JPEG, 100, os);
                os.flush();
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            mFindFaceView.setVisibility(View.GONE);
        }
        bitmap2.recycle();
        bitmap1.recycle();
    }

    /**
     * 设置相机的显示方向（这里必须这么设置，不然检测不到人脸）
     *
     * @param cameraId 相机ID(0是后置摄像头，1是前置摄像头）
     * @param camera   相机对象
     */
    private void setCameraDisplayOrientation(int cameraId, Camera camera) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        int degree = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degree = 0;
                break;
            case Surface.ROTATION_90:
                degree = 90;
                break;
            case Surface.ROTATION_180:
                degree = 180;
                break;
            case Surface.ROTATION_270:
                degree = 270;
                break;
        }

        orientionOfCamera = info.orientation;
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degree) % 360;
            result = (360 - result) % 360;
        } else {
            result = (info.orientation - degree + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    /**
     * 设置title
     */
    private void initToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("调用摄像头人脸抓拍");
    }

    @OnClick({R.id.bt_camera})
    public void clickEvent(View view) {
        switch (view.getId()) {
            case R.id.bt_camera:
                if (camera != null) {
                    try {
                        camera.takePicture(null, null, new MyPictureCallback());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    public static void launch(Activity activity) {
        Router.newIntent(activity)
                .to(FaceActivity.class)
                .launch();
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_face;
    }

    @Override
    public Object newP() {
        return null;
    }
}
