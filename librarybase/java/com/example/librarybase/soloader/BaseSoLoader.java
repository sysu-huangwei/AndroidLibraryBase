package com.example.librarybase.soloader;
import android.util.Log;
import com.getkeepsafe.relinker.ReLinker;
import com.getkeepsafe.relinker.ReLinker.Logger;

import android.content.Context;

/**
 * User: HW
 * Date: 2020/4/20
 * Description: so动态库加载器
 */
public class BaseSoLoader {
    private static final String TAG = "BaseSoLoader";
    private static Context sContext = null;
    private static boolean sEnableLoger = true;

    public static void setContext(Context context) {
        sContext = context;
    }

    public static void enableLoger(boolean enable) {
        sEnableLoger = enable;
    }

    public static void loadLibrary(String libname) {
        if (sContext != null) {
            if (sEnableLoger) {
                ReLinker.log(new Logger() {
                    public void log(String message) {
                        Log.d(BaseSoLoader.TAG, message);
                    }
                }).loadLibrary(sContext, libname);
            } else {
                ReLinker.loadLibrary(sContext, libname);
            }
        } else {
            System.loadLibrary(libname);
        }
    }
}
