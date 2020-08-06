//
// Created by rayyy on 2020/8/5.
//

#include "ThreeDimensionalOneFrameDataJni.h"
#include "ThreeDimensionalOneFrameData.h"
#include "BaseDefine.h"

#define CLASS_NAME_PATH "com/example/librarybase/threedimensional/ThreeDimensionalOneFrameData"

namespace threedimensional {

class ThreeDimensionalOneFrameDataJni {
 public:
  static jlong nativeCreate(JNIEnv *env, jobject object) {
    ThreeDimensionalOneFrameData *threeDimensionalOneFrameData = new ThreeDimensionalOneFrameData();
    return reinterpret_cast<jlong>(threeDimensionalOneFrameData);
  }

  static void nativeDestroy(JNIEnv *env, jobject object, jlong nativeInstance) {
    ThreeDimensionalOneFrameData *threeDimensionalOneFrameData = reinterpret_cast<ThreeDimensionalOneFrameData *>(nativeInstance);
    SAFE_DELETE(threeDimensionalOneFrameData);
  }

  static jfloat nativeGetDepthScale(JNIEnv *env, jobject object, jlong nativeInstance) {
    ThreeDimensionalOneFrameData *threeDimensionalOneFrameData = reinterpret_cast<ThreeDimensionalOneFrameData *>(nativeInstance);
    if (threeDimensionalOneFrameData) {
      return threeDimensionalOneFrameData->depthScale;
    }
    return 0;
  }

  static jfloat nativeGetPerspectiveScale(JNIEnv *env, jobject object, jlong nativeInstance) {
    ThreeDimensionalOneFrameData *threeDimensionalOneFrameData = reinterpret_cast<ThreeDimensionalOneFrameData *>(nativeInstance);
    if (threeDimensionalOneFrameData) {
      return threeDimensionalOneFrameData->perspectiveScale;
    }
    return 0;
  }

  static jfloat nativeGetXShift(JNIEnv *env, jobject object, jlong nativeInstance) {
    ThreeDimensionalOneFrameData *threeDimensionalOneFrameData = reinterpret_cast<ThreeDimensionalOneFrameData *>(nativeInstance);
    if (threeDimensionalOneFrameData) {
      return threeDimensionalOneFrameData->xShift;
    }
    return 0;
  }

  static jfloat nativeGetYShift(JNIEnv *env, jobject object, jlong nativeInstance) {
    ThreeDimensionalOneFrameData *threeDimensionalOneFrameData = reinterpret_cast<ThreeDimensionalOneFrameData *>(nativeInstance);
    if (threeDimensionalOneFrameData) {
      return threeDimensionalOneFrameData->yShift;
    }
    return 0;
  }

  static jfloatArray nativeGetMvpMatrix(JNIEnv *env, jobject object, jlong nativeInstance) {
    ThreeDimensionalOneFrameData *threeDimensionalOneFrameData = reinterpret_cast<ThreeDimensionalOneFrameData *>(nativeInstance);
    if (threeDimensionalOneFrameData) {
      jfloatArray result = env->NewFloatArray(16);
      env->SetFloatArrayRegion(result, 0, 16, threeDimensionalOneFrameData->mvpMatrix);
      return result;
    }
    return 0;
  }
};

// jni 接口映射
static JNINativeMethod methods[] = {
    {"nativeCreate",               "()J",   (void *) ThreeDimensionalOneFrameDataJni::nativeCreate},
    {"nativeDestroy",              "(J)V",  (void *) ThreeDimensionalOneFrameDataJni::nativeDestroy},
    {"nativeGetDepthScale",        "(J)F",  (void *) ThreeDimensionalOneFrameDataJni::nativeGetDepthScale},
    {"nativeGetPerspectiveScale",  "(J)F",  (void *) ThreeDimensionalOneFrameDataJni::nativeGetPerspectiveScale},
    {"nativeGetXShift",            "(J)F",  (void *) ThreeDimensionalOneFrameDataJni::nativeGetXShift},
    {"nativeGetYShift",            "(J)F",  (void *) ThreeDimensionalOneFrameDataJni::nativeGetYShift},
    {"nativeGetMvpMatrix",         "(J)[F", (void *) ThreeDimensionalOneFrameDataJni::nativeGetMvpMatrix},
};

int registerThreeDimensionalOneFrameDataNativeMethods(JNIEnv *env, void *reserved) {
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