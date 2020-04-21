//
// Created by HW on 2020/4/21.
//

#ifndef ANDROIDLIBRARYBASE_JNIHELPER_H
#define ANDROIDLIBRARYBASE_JNIHELPER_H

#include <jni.h>
#include <android/asset_manager.h>

namespace librarybase {

    typedef struct JniMethodInfo {
        JNIEnv *env;
        jclass classID;
        jmethodID methodID;
    } JniMethodInfo;

    class JniHelper {
    public:
        static void setJavaVM(JavaVM *javaVM);

        static JavaVM *getJavaVM();

        static JNIEnv *getEnv();

        static bool getStaticMethodInfo(JniMethodInfo &methodInfo, const char *className,
                                        const char *methodName, const char *paramCode);

        static void setAssetManager(AAssetManager *assetManager);

        static AAssetManager *getAssetManager();

        static jobject getContext();

        static char *readFileToData(const char *filePath, long *size);

    private:
        static JNIEnv *getCacheEnv(JavaVM *jvm);

        static JavaVM *_psJavaVM;

        static AAssetManager *s_assetManager;

        static char *readSDCardFileToData(const char *filePath, long *size);

        static char *readAssetFileToData(const char *filePath, long *size);
    };

}


#endif //ANDROIDLIBRARYBASE_JNIHELPER_H
