//
// Created by HW on 2020/4/23.
//

#include "LibraryBaseTestJni.h"

#include "AndroidHelper.h"
#include "JniHelper.h"
#include "BaseLog.h"

#define CLASS_NAME_PATH "com/example/librarybase/LibraryBaseTest"

namespace librarybase {

    class LibraryBaseTestJni {
    public:
        static void runTest(JNIEnv *env, jclass obj) {

            char *packageName = AndroidHelper::getAndroidPackageName(env, JniHelper::getContext());
            LOGD("LibraryBaseTestJni::runTest: packageName = %s", packageName);
            SAFE_DELETE_ARRAY(packageName);

            char *androidDataDir = AndroidHelper::getAndroidDataDir(env, JniHelper::getContext());
            LOGD("LibraryBaseTestJni::runTest: androidDataDir = %s", androidDataDir);
            SAFE_DELETE_ARRAY(androidDataDir);

            char *androidCacheDir = AndroidHelper::getAndroidCacheDir(env, JniHelper::getContext());
            LOGD("LibraryBaseTestJni::runTest: androidCacheDir = %s", androidCacheDir);
            SAFE_DELETE_ARRAY(androidCacheDir);

            char *androidFilesDir = AndroidHelper::getAndroidFilesDir(env, JniHelper::getContext());
            LOGD("LibraryBaseTestJni::runTest: androidFilesDir = %s", androidFilesDir);
            SAFE_DELETE_ARRAY(androidFilesDir);

            char *androidExternalCacheDir = AndroidHelper::getAndroidExternalCacheDir(env, JniHelper::getContext());
            LOGD("LibraryBaseTestJni::runTest: androidExternalCacheDir = %s", androidExternalCacheDir);
            SAFE_DELETE_ARRAY(androidExternalCacheDir);

            char *androidExternalFilesDir = AndroidHelper::getAndroidExternalFilesDir(env, JniHelper::getContext());
            LOGD("LibraryBaseTestJni::runTest: androidExternalFilesDir = %s", androidExternalFilesDir);
            SAFE_DELETE_ARRAY(androidExternalFilesDir);

            char *androidExternalStorageDirectory = AndroidHelper::getAndroidExternalStorageDirectory(env);
            LOGD("LibraryBaseTestJni::runTest: androidExternalStorageDirectory = %s", androidExternalStorageDirectory);
            SAFE_DELETE_ARRAY(androidExternalStorageDirectory);

            int androidCpuCount = AndroidHelper::getAndroidCpuCount();
            LOGD("LibraryBaseTestJni::runTest: androidCpuCount = %d", androidCpuCount);

            int androidSDKVersion = AndroidHelper::getAndroidSDKVersion(env);
            LOGD("LibraryBaseTestJni::runTest: androidSDKVersion = %d", androidSDKVersion);

            bool mkDirsSuccess = AndroidHelper::mkDirs(env, "/mnt/sdcard/Android/data/com.example.androidlibrarybase/files/librarybase");
            LOGD("LibraryBaseTestJni::runTest: mkDirs = %s mkDirsSuccess = %d", "mnt/sdcard/librarybase", mkDirsSuccess);

            bool deleteFileSuccess = AndroidHelper::deleteFile(env, "/mnt/sdcard/Android/data/com.example.androidlibrarybase/files/librarybase");
            LOGD("LibraryBaseTestJni::runTest: deleteFile = %s deleteFileSuccess = %d", "mnt/sdcard/librarybase", deleteFileSuccess);
        }
    };

    // jni 接口映射
    static JNINativeMethod methods[] = {
            {"nativeRunTest", "()V", (void *) LibraryBaseTestJni::runTest},
    };

    int registerLibraryBaseTestNativeMethods(JNIEnv *env, void *reserved) {
        jclass cls = env->FindClass(CLASS_NAME_PATH);
        if (cls == NULL) {
            return JNI_ERR;
        }
        // 注册方法
        jint nRes = env->RegisterNatives(cls, methods, sizeof(methods) / sizeof(methods[0]));
        if (nRes < 0) {
            return JNI_ERR;
        }

        return JNI_OK;
    }

}