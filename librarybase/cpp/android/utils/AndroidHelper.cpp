//
// Created by HW on 2020/4/21.
//

#include "AndroidHelper.h"

#include <cstring>
#include <unistd.h>

namespace librarybase {

    char *AndroidHelper::getAndroidPackageName(JNIEnv *env, jobject context) {
        if (context == 0) {
            return NULL;
        }

        //Context类
        jclass contextClass = env->FindClass("android/content/Context");
        if (contextClass == 0) {
            return NULL;
        }

        //getPackageName方法
        jmethodID getPackageNameMethod = env->GetMethodID(contextClass, "getPackageName",
                                                          "()Ljava/lang/String;");
        if (getPackageNameMethod == 0) {
            return NULL;
        }

        //调用方法获取包名
        jstring jstrPackageName = (jstring) env->CallObjectMethod(context, getPackageNameMethod);
        if (jstrPackageName == 0) {
            return NULL;
        }

        const char *strPackageName = env->GetStringUTFChars(jstrPackageName, 0);
        char *strPackageNameReturn = new char[strlen(strPackageName) + 1];
        strcpy(strPackageNameReturn, strPackageName);
        strPackageNameReturn[strlen(strPackageName)] = '\0';
        env->ReleaseStringUTFChars(jstrPackageName, strPackageName);

        return strPackageNameReturn;
    }

    char *AndroidHelper::getAndroidDataDir(JNIEnv *env, jobject obj, jobject context) {
        if (context == 0) {
            return NULL;
        }

        //Context类
        jclass contextClass = env->FindClass("android/content/Context");
        if (contextClass == 0) {
            return NULL;
        }

        //getPackageName方法
        jmethodID getPackageName = env->GetMethodID(contextClass, "getPackageName",
                                                    "()Ljava/lang/String;");
        if (getPackageName == 0) {
            return NULL;
        }

        //getPackageManager方法
        jmethodID getPackageManager = env->GetMethodID(contextClass, "getPackageManager",
                                                       "()Landroid/content/pm/PackageManager;");
        if (getPackageManager == 0) {
            return NULL;
        }

        //调用方法获取包名
        jobject jstrPackageName = env->CallObjectMethod(context, getPackageName);
        if (jstrPackageName == 0) {
            return NULL;
        }

        //调用方法获取Packagemanager对象
        jobject objPackageManager = env->CallObjectMethod(context, getPackageManager);
        if (objPackageManager == 0) {
            return NULL;
        }

        //PackageManager类
        jclass packageManagerClass = env->FindClass("android/content/pm/PackageManager");
        if (packageManagerClass == 0) {
            return NULL;
        }

        //getApplicationInfo方法
        jmethodID getApplicationInfoMethod = env->GetMethodID(packageManagerClass,
                                                              "getApplicationInfo",
                                                              "(Ljava/lang/String;I)Landroid/content/pm/ApplicationInfo;");
        if (getApplicationInfoMethod == 0) {
            return NULL;
        }

        //调用方法获取ApplicationInfo对象
        jobject objApplicationInfo = env->CallObjectMethod(objPackageManager,
                                                           getApplicationInfoMethod,
                                                           jstrPackageName, 0);
        if (objApplicationInfo == 0) {
            return NULL;
        }

        //ApplicationInfo类
        jclass ApplicationInfoClass = env->FindClass("android/content/pm/ApplicationInfo");
        if (ApplicationInfoClass == 0) {
            return NULL;
        }

        //sourceDir属性
        jfieldID sourceDir = env->GetFieldID(ApplicationInfoClass, "dataDir", "Ljava/lang/String;");
        if (sourceDir == 0) {
            return NULL;
        }

        //获取路径
        jstring jstrSourceDir = (jstring) env->GetObjectField(objApplicationInfo, sourceDir);
        if (jstrSourceDir == 0) {
            return NULL;
        }

        const char *strSourceDir = env->GetStringUTFChars(jstrSourceDir, 0);
        char *strSourceDirReturn = new char[strlen(strSourceDir) + 1];
        strcpy(strSourceDirReturn, strSourceDir);
        strSourceDirReturn[strlen(strSourceDir)] = '\0';
        env->ReleaseStringUTFChars(jstrSourceDir, strSourceDir);
        return strSourceDirReturn;
    }

    int AndroidHelper::getAndroidSDKVersion(JNIEnv *env) {
        jclass versionClass = env->FindClass("android/os/Build$VERSION");
        if (versionClass == 0) {
            return 0;
        }

        jfieldID SDK_INT = env->GetStaticFieldID(versionClass, "SDK_INT", "I");

        if (SDK_INT == 0) {
            return 0;
        }

        jint version = env->GetStaticIntField(versionClass, SDK_INT);

        return (int) version;
    }

    int AndroidHelper::getAndroidCpuCount() {
        return (int) sysconf(_SC_NPROCESSORS_ONLN);
    }

    char *AndroidHelper::getAndroidExternalStorageDirectory(JNIEnv *env) {
        //Environment类
        jclass environmentClass = env->FindClass("android/os/Environment");
        if (environmentClass == 0) {
            return NULL;
        }

        //getExternalStorageDirectory方法
        jmethodID getExternalStorageDirectoryMethod = env->GetStaticMethodID(environmentClass,
                                                                             "getExternalStorageDirectory",
                                                                             "()Ljava/io/File;");
        if (getExternalStorageDirectoryMethod == 0) {
            return NULL;
        }

        //调用方法获取File对象
        jobject objFile = env->CallStaticObjectMethod(environmentClass,
                                                      getExternalStorageDirectoryMethod);
        if (objFile == 0) {
            return NULL;
        }

        //File类
        jclass fileClass = env->FindClass("java/io/File");
        if (fileClass == 0) {
            return NULL;
        }

        //getAbsolutePath方法
        jmethodID getAbsolutePathMethod = env->GetMethodID(fileClass, "getAbsolutePath",
                                                           "()Ljava/lang/String;");
        if (getAbsolutePathMethod == 0) {
            return NULL;
        }

        //调用方法获取路径
        jstring jsdcardPath = (jstring) env->CallObjectMethod(objFile, getAbsolutePathMethod);
        if (jsdcardPath == 0) {
            return NULL;
        }

        const char *sdcardPath = env->GetStringUTFChars(jsdcardPath, JNI_FALSE);
        char *sdcardPathReturn = new char[strlen(sdcardPath) + 1];
        strcpy(sdcardPathReturn, sdcardPath);
        sdcardPathReturn[strlen(sdcardPath)] = '\0';
        env->ReleaseStringUTFChars(jsdcardPath, sdcardPath);
        return sdcardPathReturn;
    }

    bool AndroidHelper::mkDirs(JNIEnv *env, const char *folderPath) {
        //File类
        jclass fileClass = env->FindClass("java/io/File");
        if (fileClass == 0) {
            return false;
        }

        //File构造方法
        jmethodID constructorMethod = env->GetMethodID(fileClass, "<init>",
                                                       "(Ljava/lang/String;)V");
        if (constructorMethod == 0) {
            return false;
        }

        //创建File文件对象
        jobject fileObj = env->NewObject(fileClass, constructorMethod,
                                         env->NewStringUTF(folderPath));
        if (fileObj == 0) {
            return false;
        }
        //exists()方法
        jmethodID existsMethod = env->GetMethodID(fileClass, "exists", "()Z");
        if (existsMethod == 0) {
            return false;
        }

        //调用exists方法，如果文件夹存在就不用创建了
        if (env->CallBooleanMethod(fileObj, existsMethod) == JNI_TRUE) {
            return true;
        }

        //mkdirs()方法
        jmethodID mkDirsMethod = env->GetMethodID(fileClass, "mkdirs", "()Z");
        if (mkDirsMethod == 0) {
            return false;
        }

        //调用方法创建文件夹
        jboolean res = env->CallBooleanMethod(fileObj, mkDirsMethod);
        return (bool) res;
    }

    bool AndroidHelper::deleteFile(JNIEnv *env, const char *filePath) {
        //File类
        jclass fileClass = env->FindClass("java/io/File");
        if (fileClass == 0) {
            return false;
        }

        //File构造方法
        jmethodID constructorMethod = env->GetMethodID(fileClass, "<init>",
                                                       "(Ljava/lang/String;)V");
        if (constructorMethod == 0) {
            return false;
        }

        //创建File文件对象
        jobject fileObj = env->NewObject(fileClass, constructorMethod, env->NewStringUTF(filePath));
        if (fileObj == 0) {
            return false;
        }

        //exists()方法
        jmethodID existsMethod = env->GetMethodID(fileClass, "exists", "()Z");
        if (existsMethod == 0) {
            return false;
        }

        //调用exists方法，如果文件不存在就不用删除了
        if (env->CallBooleanMethod(fileObj, existsMethod) == JNI_FALSE) {
            return true;
        }

        //mkdirs()方法
        jmethodID deleteMethod = env->GetMethodID(fileClass, "delete", "()Z");
        if (deleteMethod == 0) {
            return false;
        }

        jboolean res = env->CallBooleanMethod(fileObj, deleteMethod);
        return (bool) res;
    }
}
