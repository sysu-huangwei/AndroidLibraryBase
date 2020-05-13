package com.example.androidlibrarybase;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Button;

import com.example.librarybase.opengl.Base2DTexturePainter;
import com.example.librarybase.opengl.BaseCamera;

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


        mGLSurfaceView = findViewById(R.id.gl_surface_view);
        mGLSurfaceView.setEGLContextClientVersion(2);

        mGLSurfaceView.setRenderer(new GLSurfaceView.Renderer() {
            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config) {
                mBaseCamera.initGL();
                mBase2DTexturePainter.init();
            }

            @Override
            public void onSurfaceChanged(GL10 gl, int width, int height) {
                surfaceWidth = width;
                surfaceHeight = height;
            }

            @Override
            public void onDrawFrame(GL10 gl) {
                int cameraOutputTexture = mBaseCamera.render();
                mBase2DTexturePainter.render(cameraOutputTexture, mBaseCamera.getOutputTextureWidth(), mBaseCamera.getOutputTextureHeight(), surfaceWidth, surfaceHeight);
            }
        });

        mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        mGLSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                mBaseCamera.initCamera();
                mBaseCamera.setupCamera();
                mBaseCamera.startPreview();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                mBaseCamera.stopPreview();
                mBaseCamera.releaseCamera();
            }
        });

        mBaseCamera.setBaseCameraCallback(new BaseCamera.BaseCameraCallback() {
            @Override
            public void onFrameAvailable() {
                mGLSurfaceView.requestRender();
            }

            @Override
            public void onInitGLComplete() {
                mBaseCamera.initCamera();
                mBaseCamera.setupCamera();
                mBaseCamera.startPreview();
            }

            @Override
            public void onTakePictureEnd(Bitmap bitmap) {
                mGLSurfaceView.requestRender();
            }
        });
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
}