//
// Created by HW on 2020/4/23.
//

#include "LibraryBaseTestJni.h"

#define CLASS_NAME_PATH "com/example/librarybase/LibraryBaseTest"

namespace librarybase {

    class LibraryBaseTestJni {
    public:
        static void runTest(JNIEnv *env, jclass obj) {

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