//
// Created by HW on 2020/4/21.
//

#include "JniHelper.h"
#include <pthread.h>
#include <android/asset_manager_jni.h>

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

}