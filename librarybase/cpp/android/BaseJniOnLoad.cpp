#include <jni.h>
#include <log/BaseLog.h>

//Load so
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *jvm, void *reserved) {

    LOGD("JNI_OnLoad: liblibrarybase.so is beginning attaching to system!");

    JNIEnv *env = NULL;
    if (jvm->GetEnv((void **) &env, JNI_VERSION_1_6)) {
        LOGE("JNI_OnLoad error: failed to getEnv!");
        return JNI_ERR;
    }

    LOGD("JNI_OnLoad: liblibrarybase.so has attached to system!");

    return JNI_VERSION_1_6;
}

//Unload so
JNIEXPORT void JNICALL JNI_OnUnload(JavaVM *jvm, void *reserved) {

    LOGD("JNI_OnUnload liblibrarybase.so is beginning dettaching from system!");

    JNIEnv *env = NULL;
    if ((jvm)->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK) {
        LOGE("JNI_OnUnload error: failed to getEnv!");
    }

    LOGD("JNI_OnUnload liblibrarybase.so has dettached from system!");
}


