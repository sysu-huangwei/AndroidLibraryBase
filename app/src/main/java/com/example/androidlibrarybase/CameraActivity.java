package com.example.androidlibrarybase;

import static android.view.View.INVISIBLE;

import android.content.res.AssetManager;
import android.graphics.BitmapFactory;
import android.graphics.RectF;
import android.opengl.GLES20;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Button;

import com.example.librarybase.number.NumberItem;
import com.example.librarybase.number.NumberRollFilter;
import com.example.librarybase.opengl.Base2DTexturePainter;
import com.example.librarybase.opengl.BaseCamera;
import com.example.librarybase.opengl.BaseGLUtils;
import com.example.librarybase.opengl.BasePointPainter;
import com.example.librarybase.opengl.BaseRectPainter;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class CameraActivity extends AppCompatActivity implements View.OnClickListener {

    private Button mSwitchCameraFacingButton;
    private Button mChangeCameraRatioButton;
    private Button mTakePictureButton;

    private GLSurfaceView mGLSurfaceView;
    private int surfaceWidth = 0;
    private int surfaceHeight = 0;

    private BaseCamera mBaseCamera = new BaseCamera();

    private boolean mIsTakingPicture = false;

    Base2DTexturePainter mBase2DTexturePainter = new Base2DTexturePainter();

    BasePointPainter mBasePointPainter = new BasePointPainter();
    BaseRectPainter mBaseRectPainter = new BaseRectPainter();

    Bitmap bitmap = null;
    int bitmapTextureID = 0;

    Bitmap numberBitmap = null;
    int numberTextureID = 0;

    NumberRollFilter numberRollFilter = new NumberRollFilter();

    NumberItem numberItem0 = new NumberItem();
    NumberItem numberItem1 = new NumberItem();
    NumberItem numberItem2 = new NumberItem();
    ArrayList<NumberItem> numberItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        mSwitchCameraFacingButton = findViewById(R.id.switch_camera_facing);
        mSwitchCameraFacingButton.setOnClickListener(this);
        mChangeCameraRatioButton = findViewById(R.id.change_camera_ratio);
        mChangeCameraRatioButton.setOnClickListener(this);
        mTakePictureButton = findViewById(R.id.take_picture);
        mTakePictureButton.setOnClickListener(this);
        mSwitchCameraFacingButton.setVisibility(INVISIBLE);
        mChangeCameraRatioButton.setVisibility(INVISIBLE);
        mTakePictureButton.setVisibility(INVISIBLE);

        numberItem0.left = 0.2f;
        numberItem0.top = 0.2f;
        numberItem0.right = 0.4f;
        numberItem0.bottom = 0.5f;
        numberItem0.maxSpeed = 1.0f;
        numberItem0.speedUpTime = 1.0f;
        numberItem0.continueTime = 1.0f;
        numberItem0.stopTime = 3.0f;
        numberItem0.targetNumber = 3;

        numberItem1.left = 0.4f;
        numberItem1.top = 0.2f;
        numberItem1.right = 0.6f;
        numberItem1.bottom = 0.5f;
        numberItem1.maxSpeed = 1.0f;
        numberItem1.speedUpTime = 1.0f;
        numberItem1.continueTime = 2.0f;
        numberItem1.stopTime = 3.0f;
        numberItem1.targetNumber = 1;

        numberItem2.left = 0.6f;
        numberItem2.top = 0.2f;
        numberItem2.right = 0.8f;
        numberItem2.bottom = 0.5f;
        numberItem2.maxSpeed = 1.0f;
        numberItem2.speedUpTime = 1.0f;
        numberItem2.continueTime = 3.0f;
        numberItem2.stopTime = 3.0f;
        numberItem2.targetNumber = 7;

        numberItems.add(numberItem0);
        numberItems.add(numberItem1);
        numberItems.add(numberItem2);

        mGLSurfaceView = findViewById(R.id.gl_surface_view);
        mGLSurfaceView.setEGLContextClientVersion(2);

        AssetManager assetManager = this.getAssets();
        InputStream inputStream = null;
        try {
            inputStream = assetManager.open("人物.jpeg");
        } catch (IOException e) {
            e.printStackTrace();
        }
        bitmap = getBitmapFromAssets("人物.jpeg");
        numberBitmap = getBitmapFromAssets("number.png");


        mGLSurfaceView.setRenderer(new GLSurfaceView.Renderer() {
            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config) {
                mBaseCamera.initGL();
                mBase2DTexturePainter.init();
                mBasePointPainter.init();
                mBaseRectPainter.init();
                bitmapTextureID = BaseGLUtils.createTextures2DWithBitmap(bitmap, GLES20.GL_RGBA);
                numberTextureID = BaseGLUtils.createTextures2DWithBitmap(numberBitmap, GLES20.GL_RGBA);
                numberRollFilter.init(bitmap.getWidth(), bitmap.getHeight());
            }

            @Override
            public void onSurfaceChanged(GL10 gl, int width, int height) {
                surfaceWidth = width;
                surfaceHeight = height;
            }

            @Override
            public void onDrawFrame(GL10 gl) {
                int numberRollTextureID = numberRollFilter.render(bitmapTextureID, numberTextureID, numberItems);
                mBase2DTexturePainter.render(numberRollTextureID, bitmap.getWidth(), bitmap.getHeight(), surfaceWidth, surfaceHeight);
            }
        });

        mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    @Override
    public void onClick(View v) {
        if (v.equals(mSwitchCameraFacingButton)) {
            mBaseCamera.switchCameraFacing();
        } else if (v.equals(mChangeCameraRatioButton)) {
            @BaseCamera.CameraAspectRatioEnum int currentAspectRatio = mBaseCamera.getAspectRatio();
            if (currentAspectRatio == BaseCamera.BASE_CAMERA_ASPECT_RATIO_16_9) {
                currentAspectRatio = BaseCamera.BASE_CAMERA_ASPECT_RATIO_4_3;
            } else if (currentAspectRatio == BaseCamera.BASE_CAMERA_ASPECT_RATIO_4_3 ) {
                currentAspectRatio = BaseCamera.BASE_CAMERA_ASPECT_RATIO_1_1;
            } else /*if (currentAspectRatio == BaseCamera.BASE_CAMERA_ASPECT_RATIO_1_1 ) */{
                currentAspectRatio = BaseCamera.BASE_CAMERA_ASPECT_RATIO_16_9;
            }
            mBaseCamera.setAspectRatio(currentAspectRatio);
        } else if (v.equals(mTakePictureButton)) {
            if (!mIsTakingPicture) {
                mTakePictureButton.setText("返回预览");
                mIsTakingPicture = true;
                mBaseCamera.takePicture();
                mBaseCamera.stopPreview();
            } else {
                mTakePictureButton.setText("拍照");
                mIsTakingPicture = false;
                mBaseCamera.startPreview();
            }
        }
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

}
