#include <jni.h>

//Load so
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *jvm, void *reserved) {
    JNIEnv *env = NULL;
    if (jvm->GetEnv((void **) &env, JNI_VERSION_1_6)) {
        return JNI_ERR;
    }

    return JNI_VERSION_1_6;
}

//Unload so
JNIEXPORT void JNICALL JNI_OnUnload(JavaVM *jvm, void *reserved) {
    JNIEnv *env = NULL;
    if ((jvm)->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK) {
    }
}



