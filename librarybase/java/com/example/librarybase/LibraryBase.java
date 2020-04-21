package com.example.librarybase;

import android.content.Context;
import android.content.res.AssetManager;

import androidx.annotation.IntDef;

import com.example.librarybase.soloader.BaseSoLoader;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * User: HW
 * Date: 2020/4/20
 * Description: 底层类的基类，防止库加载失败或中途被卸载引发崩溃问题
 */
public abstract class LibraryBase {

    private static Context sApplicationContext = null;

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

    /**
     * 尝试运行native方法，失败时会重新加载动态库，防止库加载失败或中途被卸载导致崩溃问题
     * @param runnable 里面包含需要运行的native方法
     */
    public static void tryRunNativeMethod(Runnable runnable) {
        try {
            runnable.run();
        } catch (Throwable e) {
            loadBaseLibrary();
            runnable.run();
        }
    }


    /**
     * log等级
     */
    public static final int BASE_LOG_LEVEL_ALL = 0;
    public static final int BASE_LOG_LEVEL_VERBOSE = 1;
    public static final int BASE_LOG_LEVEL_DEBUG = 2;
    public static final int BASE_LOG_LEVEL_INFO = 3;
    public static final int BASE_LOG_LEVEL_WARN = 4;
    public static final int BASE_LOG_LEVEL_ERROR = 5;
    public static final int BASE_LOG_LEVEL_FATAL = 6;
    public static final int BASE_LOG_LEVEL_OFF = 7;

    @IntDef({BASE_LOG_LEVEL_ALL, BASE_LOG_LEVEL_VERBOSE, BASE_LOG_LEVEL_DEBUG, BASE_LOG_LEVEL_INFO,
            BASE_LOG_LEVEL_WARN, BASE_LOG_LEVEL_ERROR, BASE_LOG_LEVEL_FATAL, BASE_LOG_LEVEL_OFF})
    @Retention(RetentionPolicy.SOURCE)
    public @interface BaseLogLevel {
    }

    /**
     * 设置log等级
     *
     * @param logLevel 设置log等级
     */
    public static void setLogLevel(@BaseLogLevel int logLevel) {
        nativeSetLogLevel(logLevel);
    }


    /**
     * 设置设备上下文，主要用于底层获取AssetManager
     *
     * @param context 设备上下文
     */
    public static void setContext(Context context) {
        sApplicationContext = context.getApplicationContext();
        nativeSetAssetManager(context.getAssets());
    }

    /**
     * 给底层反射调用获取最新可用的AssetManager
     *
     * @return 返回最新可用的AssetManager
     */
    public static AssetManager getAssetManager() {
        if (sApplicationContext == null) {
            return null;
        }
        return sApplicationContext.getAssets();
    }


    private static native void nativeSetLogLevel(int logLevel);

    private static native void nativeSetAssetManager(AssetManager assetManager);

}
