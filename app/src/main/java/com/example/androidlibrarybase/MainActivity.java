package com.example.androidlibrarybase;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.librarybase.LibraryBase;
import com.example.librarybase.soloader.BaseSoLoader;

import static com.example.librarybase.LibraryBase.BASE_LOG_LEVEL_ALL;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BaseSoLoader.setContext(this);

//        LibraryBase libraryBase = new LibraryBase();
        LibraryBase.setLogLevel(BASE_LOG_LEVEL_ALL);
        LibraryBase.setContext(this);
    }
}
