//
// Created by HW on 2020/4/21.
//

#include "LibraryBaseJni.h"
#include <android/asset_manager_jni.h>

#include "BaseLog.h"
#include "JniHelper.h"

#define CLASS_NAME_PATH "com/example/librarybase/LibraryBase"

namespace librarybase {

    class LibraryBaseJni {
    public:
        static void setLogLevel(JNIEnv *env, jclass obj, jint logLevel) {
            BASE_SetLogLevel(logLevel);
        }

        static void setAssetManager(JNIEnv *env, jclass obj, jobject asset_manager) {
            if (asset_manager != 0) {
                AAssetManager *assetManager = AAssetManager_fromJava(env, asset_manager);
                if (assetManager == NULL) {
                    LOGE("failed to access AssetManager from java");
                }
                JniHelper::setAssetManager(assetManager);
            }
        }
    };

    // jni 接口映射
    static JNINativeMethod methods[] = {
            {"nativeSetLogLevel",     "(I)V",                                  (void *) LibraryBaseJni::setLogLevel},
            {"nativeSetAssetManager", "(Landroid/content/res/AssetManager;)V", (void *) LibraryBaseJni::setAssetManager},
    };

    int registerLibraryBaseNativeMethods(JNIEnv *env, void *reserved) {
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
