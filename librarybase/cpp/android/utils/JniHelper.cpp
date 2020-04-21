//
// Created by HW on 2020/4/21.
//

#include "JniHelper.h"
#include <pthread.h>
#include <android/asset_manager_jni.h>
#include <stdio.h>

#include "BaseLog.h"

namespace librarybase {

    JavaVM *JniHelper::_psJavaVM = NULL;
    AAssetManager *JniHelper::s_assetManager = NULL;
    static pthread_key_t g_key;

    void JniHelper::setJavaVM(JavaVM *javaVM) {
        pthread_t thisThread = pthread_self();
        LOGD("JniHelper::setJavaVM: javaVM = %p, pthread_self() = %ld", javaVM, thisThread);
        _psJavaVM = javaVM;

        void (*detachCurrentThreadFunction)(void *) = [](
                void *) { JniHelper::getJavaVM()->DetachCurrentThread(); };
        pthread_key_create(&g_key, detachCurrentThreadFunction);
    }

    JavaVM *JniHelper::getJavaVM() {
        pthread_t thisThread = pthread_self();
        LOGD("JniHelper::getJavaVM(): pthread_self() = %ld", thisThread);
        return _psJavaVM;
    }

    JNIEnv *JniHelper::getEnv() {
        JNIEnv *_env = (JNIEnv *) pthread_getspecific(g_key);
        if (_env == NULL)
            _env = JniHelper::getCacheEnv(_psJavaVM);
        return _env;
    }

    JNIEnv *JniHelper::getCacheEnv(JavaVM *jvm) {
        JNIEnv *_env = NULL;
        // get jni environment
        jint ret = jvm->GetEnv((void **) &_env, JNI_VERSION_1_4);

        switch (ret) {
            case JNI_OK :
                // Success!
                pthread_setspecific(g_key, _env);
                return _env;
            case JNI_EDETACHED :
                // Thread not attached
                if (jvm->AttachCurrentThread(&_env, NULL) < 0) {
                    LOGE("JniHelper::getCacheEnv: Failed to get the environment using AttachCurrentThread()");
                    return NULL;
                } else {
                    // Success : Attached and obtained JNIEnv!
                    pthread_setspecific(g_key, _env);
                    return _env;
                }
            case JNI_EVERSION :
                // Cannot recover from this error
                LOGE("JniHelper::getCacheEnv: JNI interface version 1.4 not supported");
            default :
                LOGE("JniHelper::getCacheEnv: Failed to get the environment using GetEnv()");
                return NULL;
        }
    }

    bool JniHelper::getStaticMethodInfo(JniMethodInfo &methodInfo, const char *className,
                                        const char *methodName, const char *paramCode) {
        if ((NULL == className) || (NULL == methodName) || (NULL == paramCode)) {
            return false;
        }

        JNIEnv *env = JniHelper::getEnv();
        if (!env) {
            LOGE("JniHelper::getStaticMethodInfo: Failed to get JNIEnv");
            return false;
        }

        jclass classID = env->FindClass(className);
        if (!classID) {
            LOGE("JniHelper::getStaticMethodInfo: Failed to find class %s", className);
            env->ExceptionClear();
            return false;
        }

        jmethodID methodID = env->GetStaticMethodID(classID, methodName, paramCode);
        if (!methodID) {
            LOGE("JniHelper::getStaticMethodInfo: Failed to find static method id of %s",
                 methodName);
            env->ExceptionClear();
            return false;
        }

        methodInfo.classID = classID;
        methodInfo.env = env;
        methodInfo.methodID = methodID;
        return true;
    }

    void JniHelper::setAssetManager(AAssetManager *assetManager) {
        s_assetManager = assetManager;
    }

    AAssetManager *JniHelper::getAssetManager() {
        JniMethodInfo jMethodInfo;
        const char *className = "com/example/librarybase/LibraryBase";
        const char *methodName = "getAssetManager";
        const char *paramCode = "()Landroid/content/res/AssetManager;";

        //反射调用寻找getAssetManager方法
        if (!JniHelper::getStaticMethodInfo(jMethodInfo, className, methodName, paramCode)) {
            LOGE("JniHelper::getAssetManager: failed to get method info");
            return s_assetManager;
        }

        //调用getAssetManager方法
        jobject jAssetManager = jMethodInfo.env->CallStaticObjectMethod(jMethodInfo.classID,
                                                                        jMethodInfo.methodID);
        if (jAssetManager == NULL) {
            LOGE("JniHelper::getAssetManager: failed to get AssetManager from context");
            jMethodInfo.env->DeleteLocalRef(jMethodInfo.classID);
            return s_assetManager;
        }

        //AssetManager
        jMethodInfo.env->DeleteLocalRef(jMethodInfo.classID);
        AAssetManager *assetM = AAssetManager_fromJava(jMethodInfo.env, jAssetManager);
        if (assetM == NULL) {
            LOGE("JniHelper::getAssetManager: failed to access AssetManager from java");
            return s_assetManager;
        }
        return assetM;
    }

    jobject JniHelper::getContext() {
        JNIEnv *env = JniHelper::getEnv();
        if (!env) {
            LOGE("JniHelper::getContext: Failed to get JNIEnv");
            return NULL;
        }

        jclass libraryBaseClass = env->FindClass("com/example/librarybase/LibraryBase");
        if (!libraryBaseClass) {
            LOGE("JniHelper::getContext: Failed to find class %s", "com/example/librarybase/LibraryBase");
            env->ExceptionClear();
            return NULL;
        }

        jfieldID contextFileID = env->GetStaticFieldID(libraryBaseClass, "sApplicationContext", "Landroid/content/Context;");
        if (!contextFileID) {
            LOGE("JniHelper::getContext: Failed to GetStaticFieldID: sApplicationContext");
            env->ExceptionClear();
            return NULL;
        }

        jobject context = env->GetStaticObjectField(libraryBaseClass, contextFileID);
        if (!context) {
            LOGE("JniHelper::getContext: Failed to GetStaticObjectField");
            env->ExceptionClear();
            return NULL;
        }

        return context;
    }

    char *JniHelper::readFileToData(const char *filePath, long *size) {
        FILE *file = fopen(filePath, "rb");
        if (file) {
            return readSDCardFileToData(filePath, size);
        } else {
            return readAssetFileToData(filePath, size);
        }
    }

    char *JniHelper::readSDCardFileToData(const char *filePath, long *size) {
        FILE *file = fopen(filePath, "rb");

        fseek(file, 0, SEEK_END);
        long dataSize = ftell(file);

        fseek(file, 0, SEEK_SET);

        if (dataSize > sizeof(int)) {
            int firstInt = 0;
            fread(&firstInt,sizeof(int), 1, file);
            if ((dataSize - sizeof(int)) == firstInt) {// 带头数据
                dataSize -= sizeof(int);
            } else{
                fseek(file, 0, SEEK_SET);
            }
        } else {
            fseek(file, 0, SEEK_SET);
        }

        char *data = NULL;
        if (dataSize > 0) {
            data = new char[dataSize + 1];
            fread(data, (size_t)dataSize, 1, file);
            data[dataSize] = 0;
        }
        if (size) {
            *size = dataSize;
        }

        fclose(file);

        return data;
    }

    char *JniHelper::readAssetFileToData(const char *filePath, long *size) {
        long dataSize = 0;
        char *data = NULL;
        AAssetManager* assetManager = JniHelper::getAssetManager();
        if (filePath && assetManager) {
            AAsset* fileInstance = AAssetManager_open(assetManager, filePath, AASSET_MODE_UNKNOWN);
            if (fileInstance) {
                dataSize = AAsset_getLength(fileInstance);
                if (dataSize > sizeof(int)) {
                    int firstInt = 0;
                    AAsset_read(fileInstance, &firstInt, sizeof(int));
                    if ((dataSize - sizeof(int)) == firstInt) { // 带头数据
                        dataSize -= sizeof(int);
                    } else {
                        AAsset_seek(fileInstance, 0, 0);
                    }
                } else {
                    AAsset_seek(fileInstance, 0, 0);
                }
                if (dataSize > 0) {
                    data = new char[dataSize+1];
                    AAsset_read(fileInstance, data, dataSize);
                    data[dataSize] = 0;
                }
                AAsset_close(fileInstance);
                *size = dataSize;
            } else {
                LOGE("JniHelper::readFileToData: AAssetManager_open failed: assetManager = %p, filePath = %s; result: fileInstance = %p", assetManager, filePath, fileInstance);
            }
        } else{
            LOGE("JniHelper::readFileToData: filePath = %s, g_AAssetManager = %p", filePath, assetManager);
        }

        if (size) {
            *size = dataSize;
        }

        return data;
    }

}