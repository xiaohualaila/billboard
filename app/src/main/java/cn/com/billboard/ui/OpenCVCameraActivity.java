package cn.com.billboard.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Calendar;

import cn.com.billboard.R;
import cn.com.billboard.model.EventRecordVideoModel;
import cn.com.billboard.net.UserInfoKey;
import cn.com.billboard.present.OpenCVPresent;
import cn.com.library.event.BusProvider;
import cn.com.library.mvp.XActivity;
import cn.com.library.router.Router;
import io.reactivex.android.schedulers.AndroidSchedulers;


public class OpenCVCameraActivity extends XActivity<OpenCVPresent> implements CameraBridgeViewBase.CvCameraViewListener,JavaCameraView.PhotoSuccessCallback {


    JavaCameraView openCvCameraView;
    private CascadeClassifier cascadeClassifier;
    private Mat grayscaleImage;
    private int absoluteFaceSize;
    private String fileName = "";
    int faceSerialCount = 0;
    private void initializeOpenCVDependencies() {
        try {
            // Copy the resource into a temp file so OpenCV can load it
            InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
            File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
            File mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
            FileOutputStream os = new FileOutputStream(mCascadeFile);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();
            // Load the cascade classifier
            cascadeClassifier = new CascadeClassifier(mCascadeFile.getAbsolutePath());
        } catch (Exception e) {
            Log.e("OpenCVActivity", "Error loading cascade", e);
        }
        // And we are ready to go
        openCvCameraView.enableView();
        BusProvider.getBus().toFlowable(EventRecordVideoModel.class).observeOn(AndroidSchedulers.mainThread()).subscribe(
                model -> {
                    if(!model.isCalling){
                     finish();
                    }
                }
        );
    }

    @Override
    public void initData(Bundle savedInstanceState) {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN;
        getWindow().setAttributes(params);
        openCvCameraView = (JavaCameraView) findViewById(R.id.jcv);
        openCvCameraView.setCameraIndex(1);
        openCvCameraView.setCvCameraViewListener(this);
        openCvCameraView.setPhotoSuccessCallback(this);
    }


    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.e("log_wons", "OpenCV init error");
        }
        initializeOpenCVDependencies();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        grayscaleImage = new Mat(height, width, CvType.CV_8UC4);
        // The faces will be a 20% of the height of the screen
        absoluteFaceSize = (int) (height * 0.2);
    }

    @Override
    public void onCameraViewStopped() {
    }

    @Override
    public Mat onCameraFrame(Mat aInputFrame) {

        Imgproc.cvtColor(aInputFrame, grayscaleImage, Imgproc.COLOR_RGBA2RGB);
        MatOfRect faces = new MatOfRect();
        if (cascadeClassifier != null) {
            cascadeClassifier.detectMultiScale(grayscaleImage, faces, 1.1, 2, 2,
                    new Size(absoluteFaceSize, absoluteFaceSize), new Size());
        }
        Rect[] facesArray = faces.toArray();
        int faceCount = facesArray.length;
        if (faceCount > 0) {
            faceSerialCount++;
        } else {
            faceSerialCount = 0;
        }
        if (faceSerialCount > 6) {
            fileName =UserInfoKey.BILLBOARD_PICTURE_FACE_PATH + File.separator + getTime() + ".jpeg";
            openCvCameraView.takePhoto(fileName);
            faceSerialCount = -5000;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(OpenCVCameraActivity.this,"拍摄照片啦",Toast.LENGTH_LONG).show();
                }
            });

        }

        for (int i = 0; i < facesArray.length; i++) {
            Imgproc.rectangle(aInputFrame, facesArray[i].tl(), facesArray[i].br(), new Scalar(0, 255, 0, 255), 3);
        }
        return aInputFrame;
    }
    public long getTime() {
        return Calendar.getInstance().getTimeInMillis();
    }

    @Override
    public void doThing() {
        Toast.makeText(this,"拍好了！！！！！！！！！！！！",Toast.LENGTH_LONG).show();
        if (faceSerialCount > 6) {
            finish();
        }
    }

    public static void launch(Activity activity, String mac, int phoneType) {
        Router.newIntent(activity)
                .to(OpenCVCameraActivity.class)
                .launch();
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_open_cv;
    }

    @Override
    public OpenCVPresent newP() {
        return  new OpenCVPresent();
    }

}