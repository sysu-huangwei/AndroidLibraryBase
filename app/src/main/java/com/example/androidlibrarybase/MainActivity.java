package com.example.androidlibrarybase;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import com.example.librarybase.LibraryBase;
import com.example.librarybase.LibraryBaseTest;
import com.example.librarybase.soloader.BaseSoLoader;

import java.io.IOException;
import java.io.InputStream;

import static com.example.librarybase.LibraryBase.BASE_LOG_LEVEL_ALL;

public class MainActivity extends AppCompatActivity {

    // 动态申请定位权限的请求码
    public static final int WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 1;
    public static final int READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 2;
    public static final int CAMERA_PERMISSION_REQUEST_CODE = 3;

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
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

        LibraryBaseTest libraryBaseTest = new LibraryBaseTest();
        libraryBaseTest.runTest();
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
