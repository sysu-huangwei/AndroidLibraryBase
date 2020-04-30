package com.example.androidlibrarybase;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

import com.example.librarybase.LibraryBase;
import com.example.librarybase.LibraryBaseTest;
import com.example.librarybase.opengl.Base2DTexturePainter;
import com.example.librarybase.opengl.BaseGLUtils;
import com.example.librarybase.soloader.BaseSoLoader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static com.example.librarybase.LibraryBase.BASE_LOG_LEVEL_ALL;

public class MainActivity extends AppCompatActivity {

    // 动态申请定位权限的请求码
    public static final int WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 1;
    public static final int READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 2;
    public static final int CAMERA_PERMISSION_REQUEST_CODE = 3;

    Base2DTexturePainter mBase2DTexturePainter = new Base2DTexturePainter();

    private Bitmap mBitmap = null;
    private int mTexture = 0;

    private GLSurfaceView mGLSurfaceView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkAndRequestPermissions();

        BaseSoLoader.setContext(this);

//        LibraryBase libraryBase = new LibraryBase();
        LibraryBase.setLogLevel(BASE_LOG_LEVEL_ALL);
        LibraryBase.setContext(this);


        AssetManager assetManager = this.getAssets();
        InputStream inputStream = null;
        try {
            inputStream = assetManager.open("风景.jpeg");
        } catch (IOException e) {
            e.printStackTrace();
        }

        mBitmap = BitmapFactory.decodeStream(inputStream);

        LibraryBaseTest libraryBaseTest = new LibraryBaseTest();
        libraryBaseTest.runTest();

        mGLSurfaceView = findViewById(R.id.gl_surface_view);
        mGLSurfaceView.setEGLContextClientVersion(2);

        mGLSurfaceView.setRenderer(new GLSurfaceView.Renderer() {
            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config) {
                mTexture = BaseGLUtils.createTextures2DWithBitmap(mBitmap, GLES20.GL_RGBA);
                mBase2DTexturePainter.init();
            }

            @Override
            public void onSurfaceChanged(GL10 gl, int width, int height) {
                mBase2DTexturePainter.viewPort(0, 0, width, height);
            }

            @Override
            public void onDrawFrame(GL10 gl) {
                mBase2DTexturePainter.render(mTexture, mBitmap.getWidth(), mBitmap.getHeight());
            }
        });

        mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        mGLSurfaceView.requestRender();


    }

    /**
     * 动态申请权限
     */
    private void checkAndRequestPermissions() {
        //写权限
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE);
        }
        //读权限
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE);
        }
        //相机权限
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        }
    }
}
