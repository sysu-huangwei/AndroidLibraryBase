package com.example.librarybase;

import com.example.librarybase.soloader.BaseSoLoader;

/**
 * User: HW
 * Date: 2020/4/20
 * Description: 底层类的基类，防止库加载失败或中途被卸载引发崩溃问题
 */
public class LibraryBase {

    private static void loadBaseLibrary() {
        try {
            BaseSoLoader.loadLibrary("gnustl_shared");
        } catch (Throwable e) {
            e.printStackTrace();
        }
        try {
            BaseSoLoader.loadLibrary("c++_shared");
        } catch (Throwable e) {
            e.printStackTrace();
        }
        try {
            BaseSoLoader.loadLibrary("librarybase");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    static {
        loadBaseLibrary();
    }

}
