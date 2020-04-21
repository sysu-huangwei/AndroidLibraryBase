package com.example.androidlibrarybase;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.librarybase.LibraryBase;
import com.example.librarybase.soloader.BaseSoLoader;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BaseSoLoader.setContext(this);

        LibraryBase libraryBase = new LibraryBase();
    }
}
