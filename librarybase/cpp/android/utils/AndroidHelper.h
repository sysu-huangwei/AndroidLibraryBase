//
// Created by HW on 2020/4/21.
//

#ifndef ANDROIDLIBRARYBASE_ANDROIDHELPER_H
#define ANDROIDLIBRARYBASE_ANDROIDHELPER_H

#include <jni.h>

namespace librarybase {

    class AndroidHelper {
    public:
        /**
         * 获取app的包名
         * @return 包名，char*内存需要外部释放
         */
        static char *getAndroidPackageName(JNIEnv *env, jobject context);

        /**
         * 获取app的Data目录，相当于 java Context 类的 getDataDir() 方法
         * @return app的Data目录，char*内存需要外部释放
         */
        static char *getAndroidDataDir(JNIEnv *env, jobject context);

        /**
         * 获取app的Cache目录，相当于 java Context 类的 getCacheDir() 方法
         * @return app的Cache目录，char*内存需要外部释放
         */
        static char *getAndroidCacheDir(JNIEnv *env, jobject context);

        /**
         * 获取app的Files目录，相当于 java Context 类的 getFilesDir() 方法
         * @return app的Files目录，char*内存需要外部释放
         */
        static char *getAndroidFilesDir(JNIEnv *env, jobject context);

        /**
         * 获取app的外部Cache目录（SD卡下），相当于 java Context 类的 getExternalCacheDir() 方法
         * @return app的ExternalCache目录，char*内存需要外部释放
         */
        static char *getAndroidExternalCacheDir(JNIEnv *env, jobject context);

        /**
         * 获取app的外部Files目录（SD卡下），相当于 java Context 类的 getExternalFilesDir() 方法
         * @return app的ExternalFiles目录，char*内存需要外部释放
         */
        static char *getAndroidExternalFilesDir(JNIEnv *env, jobject context);

        /**
         * 获取当前的安卓sdk版本
         * @return 当前的安卓sdk版本
         */
        static int getAndroidSDKVersion(JNIEnv *env);

        /**
         * 获取当前设备cpu数量
         * @return 当前设备cpu数量
         */
        static int getAndroidCpuCount();

        /**
         * 获取sd卡根目录的绝对路径
         * @return sd卡根目录的绝对路径，char*内存需要外部释放
         */
        static char *getAndroidExternalStorageDirectory(JNIEnv *env);

        /**
         * 创建文件夹
         * @param folderPath 文件夹路径，会递归创建
         * @return 创建成功与否
         */
        static bool mkDirs(JNIEnv *env, const char *folderPath);

        /**
         * 删除指定路径下的文件
         * @return 删除成功与否
         */
        static bool deleteFile(JNIEnv *env, const char *filePath);
    };

}


#endif //ANDROIDLIBRARYBASE_ANDROIDHELPER_H
