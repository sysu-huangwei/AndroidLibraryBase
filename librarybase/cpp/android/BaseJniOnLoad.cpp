#include <jni.h>

#include "BaseLog.h"
#include "JniHelper.h"
#include "LibraryBaseJni.h"
#include "LibraryBaseTestJni.h"
#include "NativeBitmapJni.h"
#include "ThreeDimensionalFilterJni.h"
#include "ThreeDimensionalOneFrameDataJni.h"
#include "ThreeDimensionalUtilsJni.h"

using namespace librarybase;

//Load so
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *jvm, void *reserved) {

    LOGD("JNI_OnLoad: liblibrarybase.so is beginning attaching to system!");

    JNIEnv *env = NULL;
    if (jvm->GetEnv((void **) &env, JNI_VERSION_1_6)) {
        LOGE("JNI_OnLoad error: failed to getEnv!");
        return JNI_ERR;
    }

    JniHelper::setJavaVM(jvm);

    if (registerLibraryBaseNativeMethods(env, reserved) == JNI_ERR) {
        LOGE("failed to registerLibraryBaseNativeMethods");
        return JNI_ERR;
    }

    if (registerLibraryBaseTestNativeMethods(env, reserved) == JNI_ERR) {
        LOGE("failed to registerLibraryBaseTestNativeMethods");
        return JNI_ERR;
    }

    if (registerNativeBitmapNativeMethods(env, reserved) == JNI_ERR) {
        LOGE("failed to registerNativeBitmapNativeMethods");
        return JNI_ERR;
    }

    if (threedimensional::registerThreeDimensionalFilterNativeMethods(env, reserved) == JNI_ERR) {
        LOGE("failed to registerThreeDimensionalFilterNativeMethods");
        return JNI_ERR;
    }

  if (threedimensional::registerThreeDimensionalOneFrameDataNativeMethods(env, reserved) == JNI_ERR) {
    LOGE("failed to registerThreeDimensionalOneFrameDataNativeMethods");
    return JNI_ERR;
  }

  if (threedimensional::registerThreeDimensionalUtilsNativeMethods(env, reserved) == JNI_ERR) {
    LOGE("failed to registerThreeDimensionalUtilsNativeMethods");
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



