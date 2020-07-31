package com.example.androidlibrarybase;

import android.content.res.AssetManager;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.os.Build.VERSION_CODES;
import android.util.Pair;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Button;

import com.example.librarybase.opengl.Base2DTexturePainter;
import com.example.librarybase.opengl.Base3DPainter;
import com.example.librarybase.opengl.BaseCamera;
import com.example.librarybase.opengl.BaseGLUtils;
import com.example.librarybase.opengl.BaseDilateFilter;
import com.example.librarybase.opengl.BaseGaussianBlurFilter;
import com.example.librarybase.opengl.BasePerspectiveFilter;
import com.example.librarybase.opengl.BasePointPainter;
import com.example.librarybase.opengl.BaseRectPainter;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class CameraActivity extends AppCompatActivity implements View.OnClickListener, OnSeekBarChangeListener {

    private Button mSwitchCameraFacingButton;
    private Button showDepthButton;
    private Button mChangeCameraRatioButton;
    private Button mTakePictureButton;

    private SeekBar eyeX;
    private SeekBar eyeY;
    private SeekBar threeDSeekBar;
    private SeekBar perspectiveSeekBar;
    private SeekBar speedSeekBar;
    static final int RATIO = 500;

    private TextView eyeXTextView;
    private TextView eyeYTextView;
    private TextView threeDTextView;
    private TextView perspectiveTextView;
    private TextView speedTextView;


    private GLSurfaceView mGLSurfaceView;
    private int surfaceWidth = 0;
    private int surfaceHeight = 0;

    private BaseCamera mBaseCamera = new BaseCamera();

    private boolean mIsTakingPicture = false;

    Base2DTexturePainter mBase2DTexturePainter = new Base2DTexturePainter();

    BasePointPainter mBasePointPainter = new BasePointPainter();
    BaseRectPainter mBaseRectPainter = new BaseRectPainter();

    String[] imageFileList;
    String[] depthFileList;
    int currentFileIndex = 7;
    volatile boolean isNeedReloadImage = false;

    Bitmap bitmap;
    int bitmapTextureID;

    Bitmap depth;
    int depthTextureID;

    Bitmap material1;
    int materialTextureID1;
    float materialDepth1 = 0.07f;

    Bitmap material2;
    int materialTextureID2;
    float materialDepth2 = 0.2f;

    Bitmap material3;
    int materialTextureID3;
    float materialDepth3 = 0.7f;

    Bitmap material4;
    int materialTextureID4;
    float materialDepth4 = 1.5f;

    Bitmap material5;
    int materialTextureID5;
    float materialDepth5 = 2.0f;


    Base3DPainter base3DPainter = new Base3DPainter();
    ArrayList<Float> xShift;
    ArrayList<Float> yShift;
    float threshold = 0.5f;
    int step = 20;
    int currentShiftIndex = 0;
    int currentDirection = 1;
    final Object shiftLock = new Object();

    BaseDilateFilter baseDilateFilter = new BaseDilateFilter();
    volatile boolean showOrigin = false;
    volatile boolean showDepth = false;

    BaseGaussianBlurFilter baseGaussianBlurFilter = new BaseGaussianBlurFilter();

    BasePerspectiveFilter basePerspectiveFilter = new BasePerspectiveFilter();

    @RequiresApi(api = VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        eyeXTextView = findViewById(R.id.textView_eyeX);
        eyeYTextView = findViewById(R.id.textView_eyeY);
        threeDTextView = findViewById(R.id.textView_3D);
        perspectiveTextView = findViewById(R.id.textView_perspective);
        speedTextView = findViewById(R.id.textView_speed);

        eyeX = findViewById(R.id.seekBar_eyeX);
        eyeY = findViewById(R.id.seekBar_eyeY);
        threeDSeekBar = findViewById(R.id.seekBar_3D);
        perspectiveSeekBar = findViewById(R.id.seekBar_perspective);
        speedSeekBar = findViewById(R.id.seekBar_speed);

        eyeX.setMin((int)(-0.1 * RATIO));
        eyeX.setMax((int)(0.1 * RATIO));
        eyeY.setMin((int)(-0.1 * RATIO));
        eyeY.setMax((int)(0.1 * RATIO));
        threeDSeekBar.setMin(0);
        threeDSeekBar.setMax((int)(0.1 * RATIO));
        perspectiveSeekBar.setMin(0);
        perspectiveSeekBar.setMax((int)(0.25 * RATIO));
        speedSeekBar.setMin(0);
        speedSeekBar.setMax(1 * RATIO);

        eyeX.setProgress(0 * RATIO);
        eyeY.setProgress(0 * RATIO);
        threeDSeekBar.setProgress((int)(0.05f * RATIO));
        perspectiveSeekBar.setProgress((int)(0.03 * RATIO));
        speedSeekBar.setProgress((int)(0.5 * RATIO));

        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        eyeXTextView.setText(decimalFormat.format(eyeX.getProgress() / (float)RATIO));
        eyeYTextView.setText(decimalFormat.format(eyeY.getProgress() / (float)RATIO));
        threeDTextView.setText(decimalFormat.format(threeDSeekBar.getProgress() / (float)RATIO));
        perspectiveTextView.setText(decimalFormat.format(perspectiveSeekBar.getProgress() / (float)RATIO));
        speedTextView.setText(decimalFormat.format(speedSeekBar.getProgress() / (float)RATIO));

        eyeX.setOnSeekBarChangeListener(this);
        eyeY.setOnSeekBarChangeListener(this);
        threeDSeekBar.setOnSeekBarChangeListener(this);
        perspectiveSeekBar.setOnSeekBarChangeListener(this);
        speedSeekBar.setOnSeekBarChangeListener(this);

        mSwitchCameraFacingButton = findViewById(R.id.switch_camera_facing);
        mSwitchCameraFacingButton.setOnClickListener(this);
        showDepthButton = findViewById(R.id.show_depth);
        showDepthButton.setOnClickListener(this);
        mChangeCameraRatioButton = findViewById(R.id.change_camera_ratio);
        mChangeCameraRatioButton.setOnClickListener(this);
        mTakePictureButton = findViewById(R.id.take_picture);
        mTakePictureButton.setOnClickListener(this);

        try {
            imageFileList = this.getAssets().list("自拍");
            depthFileList = this.getAssets().list("selfie_depth");
        } catch (IOException e) {
            e.printStackTrace();
        }
        bitmap = getBitmapFromAssets("自拍/" + imageFileList[currentFileIndex]);
        depth = getBitmapFromAssets("selfie_depth/" + depthFileList[currentFileIndex]);
        material1 = getBitmapFromAssets("蝴蝶 (1).png");
        material2 = getBitmapFromAssets("蝴蝶 (2).png");
        material3 = getBitmapFromAssets("蝴蝶 (3).png");
        material4 = getBitmapFromAssets("蝴蝶 (4).png");
        material5 = getBitmapFromAssets("蝴蝶 (5).png");
        Pair<ArrayList<Float>, ArrayList<Float>> shift = getShift(currentDirection, threshold, step);
        xShift = shift.first;
        yShift = shift.second;

        ArrayList<Pair<Integer, Float>> materialTextureAndDepth = new ArrayList<>();


        mGLSurfaceView = findViewById(R.id.gl_surface_view);
        mGLSurfaceView.setEGLContextClientVersion(2);

        mGLSurfaceView.setRenderer(new GLSurfaceView.Renderer() {
            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config) {
                bitmapTextureID = BaseGLUtils.createTextures2DWithBitmap(bitmap, GLES20.GL_RGBA);
                depthTextureID = BaseGLUtils.createTextures2DWithBitmap(depth, GLES20.GL_RGBA);
                materialTextureID1 = BaseGLUtils.createTextures2DWithBitmap(material1, GLES20.GL_RGBA);
                materialTextureID2 = BaseGLUtils.createTextures2DWithBitmap(material2, GLES20.GL_RGBA);
                materialTextureID3 = BaseGLUtils.createTextures2DWithBitmap(material3, GLES20.GL_RGBA);
                materialTextureID4 = BaseGLUtils.createTextures2DWithBitmap(material4, GLES20.GL_RGBA);
                materialTextureID5 = BaseGLUtils.createTextures2DWithBitmap(material5, GLES20.GL_RGBA);
                materialTextureAndDepth.add(Pair.create(materialTextureID1, materialDepth1));
                materialTextureAndDepth.add(Pair.create(materialTextureID2, materialDepth2));
                materialTextureAndDepth.add(Pair.create(materialTextureID3, materialDepth3));
                materialTextureAndDepth.add(Pair.create(materialTextureID4, materialDepth4));
                materialTextureAndDepth.add(Pair.create(materialTextureID5, materialDepth5));
                base3DPainter.init(bitmap.getWidth(), bitmap.getHeight());
                baseDilateFilter.init(bitmap.getWidth(), bitmap.getHeight());
                baseGaussianBlurFilter.init(bitmap.getWidth(), bitmap.getHeight());
                basePerspectiveFilter.init(bitmap.getWidth(), bitmap.getHeight());
//                mBaseCamera.initGL();
                mBase2DTexturePainter.init();
                mBasePointPainter.init();
                mBaseRectPainter.init();
            }

            @Override
            public void onSurfaceChanged(GL10 gl, int width, int height) {
                surfaceWidth = width;
                surfaceHeight = height;
            }

            @Override
            public void onDrawFrame(GL10 gl) {
                synchronized (shiftLock) {
                    if (isNeedReloadImage) {
                        bitmap = getBitmapFromAssets("自拍/" + imageFileList[currentFileIndex]);
                        depth = getBitmapFromAssets("selfie_depth/" + depthFileList[currentFileIndex]);
                        bitmapTextureID = BaseGLUtils.createTextures2DWithBitmap(bitmap, GLES20.GL_RGBA);
                        depthTextureID = BaseGLUtils.createTextures2DWithBitmap(depth, GLES20.GL_RGBA);
                        isNeedReloadImage = false;
                    }
                    if (showOrigin) {
                        mBase2DTexturePainter
                                .render(bitmapTextureID, bitmap.getWidth(), bitmap.getHeight(),
                                        surfaceWidth, surfaceHeight);
                    } else if (showDepth) {
                        mBase2DTexturePainter
                                .render(depthTextureID, bitmap.getWidth(), bitmap.getHeight(),
                                        surfaceWidth, surfaceHeight);
                    } else {
                        int dilateDepthTextureID = baseDilateFilter.render(depthTextureID);
                        int dilateAndBlurDepthTextureID = baseGaussianBlurFilter.render(dilateDepthTextureID);
                        int threeDTextureID = base3DPainter
                                .render(bitmapTextureID, dilateAndBlurDepthTextureID, depthTextureID, materialTextureAndDepth,
                                        xShift.get(currentShiftIndex), yShift.get(currentShiftIndex), threeDSeekBar.getProgress() / (float)RATIO);
                        int perspectiveTextureID = basePerspectiveFilter.render(threeDTextureID,
                                eyeX.getProgress() / (float)RATIO, eyeY.getProgress() / (float)RATIO,
                                xShift.get(currentShiftIndex), yShift.get(currentShiftIndex), threshold, perspectiveSeekBar.getProgress() / (float)RATIO);
                        mBase2DTexturePainter
                                .render(perspectiveTextureID, bitmap.getWidth(), bitmap.getHeight(),
                                        surfaceWidth, surfaceHeight);
                    }
                        currentShiftIndex++;
                        if (currentShiftIndex >= xShift.size()) {
                            currentShiftIndex = 0;
                        }
////                int cameraOutputTexture = mBaseCamera.render();
////                mBase2DTexturePainter.render(cameraOutputTexture, mBaseCamera.getOutputTextureWidth(), mBaseCamera.getOutputTextureHeight(), surfaceWidth, surfaceHeight);
////                mBaseRectPainter.setRectPoints(new float[] {0.25f, 0.25f, 0.75f, 0.75f, 0.3f, 0.3f, 0.6f, 0.6f});
//                        int dilateDepthTextureID = baseDilateFilter.render(depthTextureID);
//                        int dilateAndBlurDepthTextureID = baseGaussianBlurFilter.render(dilateDepthTextureID);
//                    int texture = base3DPainter
//                            .render(bitmapTextureID, dilateAndBlurDepthTextureID, depthTextureID, materialTextureAndDepth,
//                                    xShift.get(currentShiftIndex), yShift.get(currentShiftIndex));
////                int pointOutTexture = mBasePointPainter.renderToInnerFBO(bitmapTextureID, bitmap.getWidth(), bitmap.getHeight());
////                int rectOutTexture = mBaseRectPainter.renderToInnerFBO(pointOutTexture, mBasePointPainter.getOutputTextureWidth(), mBasePointPainter.getOutputTextureHeight());
//                        mBase2DTexturePainter
//                                .render(texture, bitmap.getWidth(), bitmap.getHeight(),
//                                        surfaceWidth,
//                                        surfaceHeight);
//                    } else {
//                        mBase2DTexturePainter
//                                .render(bitmapTextureID, bitmap.getWidth(), bitmap.getHeight(),
//                                        surfaceWidth, surfaceHeight);
//                    }

                }
            }
        });

        mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        mGLSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
//                mBaseCamera.initCamera();
//                mBaseCamera.setupCamera();
//                mBaseCamera.startPreview();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
//                mBaseCamera.stopPreview();
//                mBaseCamera.releaseCamera();
            }
        });

//        mBaseCamera.setBaseCameraCallback(new BaseCamera.BaseCameraCallback() {
//            @Override
//            public void onFrameAvailable() {
//                mGLSurfaceView.requestRender();
//            }
//
//            @Override
//            public void onInitGLComplete() {
//                mBaseCamera.initCamera();
//                mBaseCamera.setupCamera();
//                mBaseCamera.startPreview();
//                mBaseCamera.setFaceDetectEnable(true);
//            }
//
//            @Override
//            public void onTakePictureEnd(Bitmap bitmap) {
//                mGLSurfaceView.requestRender();
//            }
//
//            @Override
//            public void onFaceDetected(Camera.Face[] faces, int width, int height) {
//                if (faces != null && faces.length > 0) {
//                    float[] points = new float[6 * faces.length];
//                    float[] rectPoints = new float[4 * faces.length];
//                    for (int i = 0; i < faces.length; i++) {
//                        Camera.Face face = faces[i];
//                        points[6 * i] = (float)face.leftEye.x / (float)width;
//                        points[6 * i + 1] = (float)face.leftEye.y / (float)height;
//                        points[6 * i + 2] = (float)face.rightEye.x / (float)width;
//                        points[6 * i + 3] = (float)face.rightEye.y / (float)height;
//                        points[6 * i + 4] = (float)face.mouth.x / (float)width;
//                        points[6 * i + 5] = (float)face.mouth.y / (float)height;
//                        rectPoints[4 * i] = (float)face.rect.left / (float)width;
//                        rectPoints[4 * i + 1] = (float)face.rect.top / (float)height;
//                        rectPoints[4 * i + 2] = (float)face.rect.right / (float)width;
//                        rectPoints[4 * i + 3] = (float)face.rect.bottom / (float)height;
//                    }
//                    for (int i = 0; i < points.length / 2; i++) {
//                        float originX = points[2 * i];
//                        float originY = points[2 * i + 1];
//                        points[2 * i] = -originY;
//                        points[2 * i + 1] = originX;
//                    }
//                    for (int i = 0; i < rectPoints.length / 2; i++) {
//                        float originX = rectPoints[2 * i];
//                        float originY = rectPoints[2 * i + 1];
//                        rectPoints[2 * i] = -originY;
//                        rectPoints[2 * i + 1] = originX;
//                    }
//                    mBasePointPainter.setPoints(points);
//                    mBaseRectPainter.setRectPoints(rectPoints);
//                }
//            }
//        });
    }

    @Override
    public void onClick(View v) {
        if (v.equals(mTakePictureButton)) {
            synchronized (shiftLock) {
                currentDirection++;
                if (currentDirection > 3) {
                    currentDirection = 1;
                }
                Pair<ArrayList<Float>, ArrayList<Float>> shift = getShift(currentDirection, threshold, step);
                xShift = shift.first;
                yShift = shift.second;
            }
        }
        if (v.equals(mSwitchCameraFacingButton)) {
            showOrigin = !showOrigin;
            if (showOrigin) {
                showDepth = false;
            }
        }
        if (v.equals(showDepthButton)) {
            showDepth = !showDepth;
            if (showDepth) {
                showOrigin = false;
            }
        }
        if (v.equals(mChangeCameraRatioButton)) {
            currentFileIndex++;
            if (currentFileIndex >= imageFileList.length) {
                currentFileIndex = 0;
            }
            isNeedReloadImage = true;
        }
//        if (v.equals(mSwitchCameraFacingButton)) {
//            mBaseCamera.switchCameraFacing();
//        } else if (v.equals(mChangeCameraRatioButton)) {
//            @BaseCamera.CameraAspectRatioEnum int currentAspectRatio = mBaseCamera.getAspectRatio();
//            if (currentAspectRatio == BaseCamera.BASE_CAMERA_ASPECT_RATIO_16_9) {
//                currentAspectRatio = BaseCamera.BASE_CAMERA_ASPECT_RATIO_4_3;
//            } else if (currentAspectRatio == BaseCamera.BASE_CAMERA_ASPECT_RATIO_4_3 ) {
//                currentAspectRatio = BaseCamera.BASE_CAMERA_ASPECT_RATIO_1_1;
//            } else /*if (currentAspectRatio == BaseCamera.BASE_CAMERA_ASPECT_RATIO_1_1 ) */{
//                currentAspectRatio = BaseCamera.BASE_CAMERA_ASPECT_RATIO_16_9;
//            }
//            mBaseCamera.setAspectRatio(currentAspectRatio);
//        } else if (v.equals(mTakePictureButton)) {
//            if (!mIsTakingPicture) {
//                mTakePictureButton.setText("返回预览");
//                mIsTakingPicture = true;
//                mBaseCamera.takePicture();
//                mBaseCamera.stopPreview();
//            } else {
//                mTakePictureButton.setText("拍照");
//                mIsTakingPicture = false;
//                mBaseCamera.startPreview();
//            }
//        }
    }

    /**
     * 获取前景后景坐标偏移量
     * @param direction 旋转方式：1 左右 2 上下 3 转圈
     * @param threshold 最大偏移量
     * @param step 1/4周期的步长
     * @return first：x方向偏移量  second：y方向偏移量
     */
    private static Pair<ArrayList<Float>, ArrayList<Float>> getShift(int direction, float threshold, int step) {
        ArrayList<Float> xShift = new ArrayList<>();
        ArrayList<Float> yShift = new ArrayList<>();
        if (direction == 1 || direction == 2) {
            ArrayList<Float> positive = new ArrayList<>();
            ArrayList<Float> negative = new ArrayList<>();
            for (float f = 0.0f; f < threshold; f += (threshold / step)) {
                positive.add(f);
                negative.add(-f);
            }
            ArrayList<Float> animalFunction = new ArrayList<>();
            animalFunction.addAll(positive);
            Collections.reverse(positive);
            animalFunction.addAll(positive);
            animalFunction.addAll(negative);
            Collections.reverse(negative);
            animalFunction.addAll(negative);

            xShift.addAll(animalFunction);
            yShift.addAll(animalFunction);

            if (direction == 1) {
                Collections.fill(yShift, 0.0f);
            } else {
                Collections.fill(xShift, 0.0f);
            }

        } else if (direction == 3) {
            float fullDeg = (float) Math.PI * 2.0f;
            for (float f = 0.0f; f < fullDeg; f += fullDeg / step / 4.0f) {
                xShift.add(threshold * (float) Math.cos(f));
                yShift.add(threshold * (float) Math.sin(f));
            }
        }
        return Pair.create(xShift, yShift);
    }

    private Bitmap getBitmapFromAssets(String path) {
        AssetManager assetManager = this.getAssets();
        InputStream inputStream = null;
        try {
            inputStream = assetManager.open(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return BitmapFactory.decodeStream(inputStream);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        eyeXTextView.setText(decimalFormat.format(eyeX.getProgress() / (float)RATIO));
        eyeYTextView.setText(decimalFormat.format(eyeY.getProgress() / (float)RATIO));
        threeDTextView.setText(decimalFormat.format(threeDSeekBar.getProgress() / (float)RATIO));
        perspectiveTextView.setText(decimalFormat.format(perspectiveSeekBar.getProgress() / (float)RATIO));
        speedTextView.setText(decimalFormat.format(speedSeekBar.getProgress() / (float)RATIO));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (seekBar.equals(speedSeekBar)) {
            synchronized (shiftLock) {
                step = (int) (speedSeekBar.getProgress() / (float) RATIO * (-40.0f) + 40);
                if (step == 0) {
                    step = 1;
                }
                Pair<ArrayList<Float>, ArrayList<Float>> shift = getShift(currentDirection, threshold, step);
                xShift = shift.first;
                yShift = shift.second;
                currentShiftIndex = 0;
            }
        }
    }
}
