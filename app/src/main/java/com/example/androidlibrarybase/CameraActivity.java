package com.example.androidlibrarybase;

import android.content.res.AssetManager;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.os.Build.VERSION_CODES;
import android.util.Log;
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
    private Button mChangeCameraRatioButton;
    private Button mTakePictureButton;

    private SeekBar eyeX;
    private SeekBar eyeY;
    private SeekBar eyeZ;
    private SeekBar centerX;
    private SeekBar centerY;
    private SeekBar centerZ;
    private SeekBar upX;
    private SeekBar upY;
    private SeekBar upZ;
    static final int RATIO = 100;

    private TextView eyeXTextView;
    private TextView eyeYTextView;
    private TextView eyeZTextView;
    private TextView centerXTextView;
    private TextView centerYTextView;
    private TextView centerZTextView;
    private TextView upXTextView;
    private TextView upYTextView;
    private TextView upZTextView;


    private GLSurfaceView mGLSurfaceView;
    private int surfaceWidth = 0;
    private int surfaceHeight = 0;

    private BaseCamera mBaseCamera = new BaseCamera();

    private boolean mIsTakingPicture = false;

    Base2DTexturePainter mBase2DTexturePainter = new Base2DTexturePainter();

    BasePointPainter mBasePointPainter = new BasePointPainter();
    BaseRectPainter mBaseRectPainter = new BaseRectPainter();

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
    float materialDepth4 = 1.2f;

    Bitmap material5;
    int materialTextureID5;
    float materialDepth5 = 1.5f;


    Base3DPainter base3DPainter = new Base3DPainter();
    ArrayList<Float> xShift;
    ArrayList<Float> yShift;
    int currentShiftIndex = 0;
    int currentDirection = 1;
    final Object shiftLock = new Object();

    BaseDilateFilter baseDilateFilter = new BaseDilateFilter();
    volatile boolean showOrigin = false;

    BaseGaussianBlurFilter baseGaussianBlurFilter = new BaseGaussianBlurFilter();

    BasePerspectiveFilter basePerspectiveFilter = new BasePerspectiveFilter();

    @RequiresApi(api = VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        eyeXTextView = findViewById(R.id.textView_eyeX);
        eyeYTextView = findViewById(R.id.textView_eyeY);
        eyeZTextView = findViewById(R.id.textView_eyeZ);
        centerXTextView = findViewById(R.id.textView_centerX);
        centerYTextView = findViewById(R.id.textView_centerY);
        centerZTextView = findViewById(R.id.textView_centerZ);
        upXTextView = findViewById(R.id.textView_upX);
        upYTextView = findViewById(R.id.textView_upY);
        upZTextView = findViewById(R.id.textView_upZ);

        eyeX = findViewById(R.id.seekBar_eyeX);
        eyeY = findViewById(R.id.seekBar_eyeY);
        eyeZ = findViewById(R.id.seekBar_eyeZ);
        centerX = findViewById(R.id.seekBar_centerX);
        centerY = findViewById(R.id.seekBar_centerY);
        centerZ = findViewById(R.id.seekBar_centerZ);
        upX = findViewById(R.id.seekBar_upX);
        upY = findViewById(R.id.seekBar_upY);
        upZ = findViewById(R.id.seekBar_upZ);

        eyeX.setOnSeekBarChangeListener(this);
        eyeY.setOnSeekBarChangeListener(this);
        eyeZ.setOnSeekBarChangeListener(this);
        centerX.setOnSeekBarChangeListener(this);
        centerY.setOnSeekBarChangeListener(this);
        centerZ.setOnSeekBarChangeListener(this);
        upX.setOnSeekBarChangeListener(this);
        upY.setOnSeekBarChangeListener(this);
        upZ.setOnSeekBarChangeListener(this);


        eyeX.setMin((int)(-0.1 * RATIO));
        eyeX.setMax((int)(0.1 * RATIO));
        eyeY.setMin((int)(-0.1 * RATIO));
        eyeY.setMax((int)(0.1 * RATIO));
        eyeZ.setMin(-3 * RATIO);
        eyeZ.setMax(3 * RATIO);

        eyeX.setProgress(0 * RATIO);
        eyeY.setProgress(0 * RATIO);
        eyeZ.setProgress(-2 * RATIO);

        centerX.setMin(-1 * RATIO);
        centerX.setMax(1 * RATIO);
        centerY.setMin(-1 * RATIO);
        centerY.setMax(1 * RATIO);
        centerZ.setMin(-1 * RATIO);
        centerZ.setMax(1 * RATIO);

        centerX.setProgress(0 * RATIO);
        centerY.setProgress(0 * RATIO);
        centerZ.setProgress(0 * RATIO);

        upX.setMin(-1 * RATIO);
        upX.setMax(1 * RATIO);
        upY.setMin(-1 * RATIO);
        upY.setMax(1 * RATIO);
        upZ.setMin(-1 * RATIO);
        upZ.setMax(1 * RATIO);

        upX.setProgress(0 * RATIO);
        upY.setProgress(1 * RATIO);
        upZ.setProgress(0 * RATIO);


        eyeX = findViewById(R.id.seekBar_eyeX);
        eyeY = findViewById(R.id.seekBar_eyeY);
        eyeZ = findViewById(R.id.seekBar_eyeZ);
        centerX = findViewById(R.id.seekBar_centerX);
        centerY = findViewById(R.id.seekBar_centerY);
        centerZ = findViewById(R.id.seekBar_centerZ);
        upX = findViewById(R.id.seekBar_upX);
        upY = findViewById(R.id.seekBar_upY);
        upZ = findViewById(R.id.seekBar_upZ);


        mSwitchCameraFacingButton = findViewById(R.id.switch_camera_facing);
        mSwitchCameraFacingButton.setOnClickListener(this);
        mChangeCameraRatioButton = findViewById(R.id.change_camera_ratio);
        mChangeCameraRatioButton.setOnClickListener(this);
        mTakePictureButton = findViewById(R.id.take_picture);
        mTakePictureButton.setOnClickListener(this);

        bitmap = getBitmapFromAssets("input.jpg");
        depth = getBitmapFromAssets("depth.png");
        material1 = getBitmapFromAssets("蝴蝶 (1).png");
        material2 = getBitmapFromAssets("蝴蝶 (2).png");
        material3 = getBitmapFromAssets("蝴蝶 (3).png");
        material4 = getBitmapFromAssets("蝴蝶 (4).png");
        material5 = getBitmapFromAssets("蝴蝶 (5).png");
        Pair<ArrayList<Float>, ArrayList<Float>> shift = getShift(currentDirection, 0.5f, 20);
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
                    if (showOrigin) {
                        int texture = basePerspectiveFilter.render(bitmapTextureID,
                                eyeX.getProgress() / (float)RATIO, eyeY.getProgress() / (float)RATIO, eyeZ.getProgress() / (float)RATIO,
                                centerX.getProgress() / (float)RATIO, centerY.getProgress() / (float)RATIO, centerZ.getProgress() / (float)RATIO,
                                upX.getProgress() / (float)RATIO, upY.getProgress() / (float)RATIO, upZ.getProgress() / (float)RATIO);
                        mBase2DTexturePainter
                                .render(texture, bitmap.getWidth(), bitmap.getHeight(),
                                        surfaceWidth, surfaceHeight);
                    } else {
                        mBase2DTexturePainter
                                .render(bitmapTextureID, bitmap.getWidth(), bitmap.getHeight(),
                                        surfaceWidth, surfaceHeight);
                    }
//                        currentShiftIndex++;
//                        if (currentShiftIndex >= xShift.size()) {
//                            currentShiftIndex = 0;
//                        }
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
                Pair<ArrayList<Float>, ArrayList<Float>> shift = getShift(currentDirection, 0.5f, 20);
                xShift = shift.first;
                yShift = shift.second;
            }
        }
        if (v.equals(mSwitchCameraFacingButton)) {
            showOrigin = !showOrigin;
        }
        if (v.equals(mChangeCameraRatioButton)) {
            currentShiftIndex++;
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

    private static Pair<ArrayList<Float>, ArrayList<Float>> getShift(int direction, float threshold, int step) {
        ArrayList<Float> xShift = new ArrayList<>();
        ArrayList<Float> yShift = new ArrayList<>();
        if (direction == 1) {
            ArrayList<Float> left = new ArrayList<>();
            ArrayList<Float> right = new ArrayList<>();
            for (float f = 0.0f; f < threshold; f += (threshold / step)) {
                left.add(f);
                right.add(-f);
            }
            xShift.addAll(left);
            Collections.reverse(left);
            xShift.addAll(left);
            xShift.addAll(right);
            Collections.reverse(right);
            xShift.addAll(right);
            yShift.addAll(xShift);
            Collections.fill(yShift, 0.0f);
        } else if (direction == 2) {
            for (float f = -threshold; f < threshold; f += (2.0f * threshold / step)) {
                xShift.add(0.0f);
                yShift.add(-f);
            }
        } else {
            float fullDeg = (float) Math.PI * 2.0f;
            for (float f = 0.0f; f < fullDeg; f+= fullDeg / step) {
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
        DecimalFormat decimalFormat = new DecimalFormat("0.0");
        eyeXTextView.setText(decimalFormat.format(eyeX.getProgress() / (float)RATIO));
        eyeYTextView.setText(decimalFormat.format(eyeY.getProgress() / (float)RATIO));
        eyeZTextView.setText(decimalFormat.format(eyeZ.getProgress() / (float)RATIO));
        centerXTextView.setText(decimalFormat.format(centerX.getProgress() / (float)RATIO));
        centerYTextView.setText(decimalFormat.format(centerY.getProgress() / (float)RATIO));
        centerZTextView.setText(decimalFormat.format(centerZ.getProgress() / (float)RATIO));
        upXTextView.setText(decimalFormat.format(upX.getProgress() / (float)RATIO));
        upYTextView.setText(decimalFormat.format(upY.getProgress() / (float)RATIO));
        upZTextView.setText(decimalFormat.format(upZ.getProgress() / (float)RATIO));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
