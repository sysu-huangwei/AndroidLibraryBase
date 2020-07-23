package com.example.androidlibrarybase;

import android.content.res.AssetManager;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Button;

import com.example.librarybase.opengl.Base2DTexturePainter;
import com.example.librarybase.opengl.Base3DPainter;
import com.example.librarybase.opengl.BaseCamera;
import com.example.librarybase.opengl.BaseGLUtils;
import com.example.librarybase.opengl.BasePointPainter;
import com.example.librarybase.opengl.BaseRectPainter;

import java.io.IOException;
import java.io.InputStream;
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

    Bitmap bitmap;
    int bitmapTextureID;

    Bitmap depth;
    int depthTextureID;

    Bitmap material;
    int materialTextureID;

    Base3DPainter base3DPainter = new Base3DPainter();

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


        AssetManager assetManager = this.getAssets();
        InputStream inputStream = null;
        InputStream inputStreamDepth = null;
        InputStream inputStreamMaterial = null;
        try {
            inputStream = assetManager.open("input.jpg");
            inputStreamDepth = assetManager.open("depth.png");
            inputStreamMaterial = assetManager.open("material.png");
        } catch (IOException e) {
            e.printStackTrace();
        }
        bitmap = BitmapFactory.decodeStream(inputStream);
        depth = BitmapFactory.decodeStream(inputStreamDepth);
        material = BitmapFactory.decodeStream(inputStreamMaterial);


        mGLSurfaceView = findViewById(R.id.gl_surface_view);
        mGLSurfaceView.setEGLContextClientVersion(2);

        mGLSurfaceView.setRenderer(new GLSurfaceView.Renderer() {
            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config) {
                bitmapTextureID = BaseGLUtils.createTextures2DWithBitmap(bitmap, GLES20.GL_RGBA);
                depthTextureID = BaseGLUtils.createTextures2DWithBitmap(depth, GLES20.GL_RGBA);
                materialTextureID = BaseGLUtils.createTextures2DWithBitmap(material, GLES20.GL_RGBA);
                base3DPainter.init(bitmap.getWidth(), bitmap.getHeight());
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
//                int cameraOutputTexture = mBaseCamera.render();
//                mBase2DTexturePainter.render(cameraOutputTexture, mBaseCamera.getOutputTextureWidth(), mBaseCamera.getOutputTextureHeight(), surfaceWidth, surfaceHeight);
//                mBaseRectPainter.setRectPoints(new float[] {0.25f, 0.25f, 0.75f, 0.75f, 0.3f, 0.3f, 0.6f, 0.6f});
                int texture = base3DPainter.render(bitmapTextureID, depthTextureID, materialTextureID);
//                int pointOutTexture = mBasePointPainter.renderToInnerFBO(bitmapTextureID, bitmap.getWidth(), bitmap.getHeight());
//                int rectOutTexture = mBaseRectPainter.renderToInnerFBO(pointOutTexture, mBasePointPainter.getOutputTextureWidth(), mBasePointPainter.getOutputTextureHeight());
                mBase2DTexturePainter.render(texture, bitmap.getWidth(), bitmap.getHeight(), surfaceWidth, surfaceHeight);
            }
        });

        mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

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
}
