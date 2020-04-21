//
// Created by HW on 2020/4/21.
//

#include "LibraryBaseJni.h"
#include "BaseLog.h"

#define CLASS_NAME_PATH "com/example/librarybase/LibraryBase"

namespace librarybase {

    class LibraryBaseJni {
    public:
        static void setLogLevel(JNIEnv *env, jclass obj, jint logLevel) {
            BASE_SetLogLevel(logLevel);
        }
    };

    // jni 接口映射
    static JNINativeMethod methods[] = {
            {"nativeSetLogLevel", "(I)V", (void *) LibraryBaseJni::setLogLevel},
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
