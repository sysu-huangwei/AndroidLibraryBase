//
// Created by rayyy on 2020/8/5.
//

#include "ThreeDimensionalFilterJni.h"
#include "ThreeDimensionalFilter.h"
#include "BaseDefine.h"

#define CLASS_NAME_PATH "com/example/librarybase/threedimensional/ThreeDimensionalFilter"

namespace threedimensional {

class ThreeDimensionalFilterJni {
 public:
  static jlong nativeCreate(JNIEnv *env, jobject object) {
    ThreeDimensionalFilter *threeDimensionalFilter = new ThreeDimensionalFilter();
    return reinterpret_cast<jlong>(threeDimensionalFilter);
  }

  static void nativeDestroy(JNIEnv *env, jobject object, jlong nativeInstance) {
    ThreeDimensionalFilter *threeDimensionalFilter = reinterpret_cast<ThreeDimensionalFilter *>(nativeInstance);
    SAFE_DELETE(threeDimensionalFilter);
  }

  static void nativeInit(JNIEnv *env,
                         jobject object, jlong nativeInstance, jint width, jint height) {
    ThreeDimensionalFilter *threeDimensionalFilter = reinterpret_cast<ThreeDimensionalFilter *>(nativeInstance);
    if (threeDimensionalFilter) {
      threeDimensionalFilter->init(width, height);
    }
  }

  static void nativeRelease(JNIEnv *env, jobject object, jlong nativeInstance) {
    ThreeDimensionalFilter *threeDimensionalFilter = reinterpret_cast<ThreeDimensionalFilter *>(nativeInstance);
    if (threeDimensionalFilter) {
      threeDimensionalFilter->release();
    }
  }

  static void nativeReset(JNIEnv *env, jobject object, jlong nativeInstance) {
    ThreeDimensionalFilter *threeDimensionalFilter = reinterpret_cast<ThreeDimensionalFilter *>(nativeInstance);
    if (threeDimensionalFilter) {
      threeDimensionalFilter->reset();
    }
  }

  static jint nativeRender(JNIEnv *env,
                           jobject object, jlong nativeInstance,
                                 jint imageTextureID, jint originDepthTextureID,
                                 jintArray materialTextureIDs, jfloatArray materialDepths,
                                 jlong threeDimensionalOneFrameDataPointer) {
    jint resultTextureID = imageTextureID;
    ThreeDimensionalFilter *threeDimensionalFilter = reinterpret_cast<ThreeDimensionalFilter *>(nativeInstance);
    if (threeDimensionalFilter) {
      std::vector<std::pair<int, float> > materialTextureAndDepth;
      jsize materialTextureIDsLength = env->GetArrayLength(materialTextureIDs);
      jsize materialDepthsLength = env->GetArrayLength(materialDepths);
      if (materialTextureIDsLength == materialDepthsLength) {
        jint *materialTextureIDsRef = env->GetIntArrayElements(materialTextureIDs, JNI_FALSE);
        jfloat *materialDepthsRef = env->GetFloatArrayElements(materialDepths, JNI_FALSE);
        std::vector<std::pair<int, float> > materialTextureAndDepth;
        for (int i = 0; i < materialTextureIDsLength; ++i) {
          materialTextureAndDepth.push_back(std::pair<int, float>(materialTextureIDsRef[i], materialDepthsRef[i]));
        }
        ThreeDimensionalOneFrameData *threeDimensionalOneFrameData = reinterpret_cast<ThreeDimensionalOneFrameData *>(threeDimensionalOneFrameDataPointer);
        if (threeDimensionalOneFrameData) {
          resultTextureID = threeDimensionalFilter->render(imageTextureID, originDepthTextureID, materialTextureAndDepth, *threeDimensionalOneFrameData);
        }
        env->ReleaseIntArrayElements(materialTextureIDs,materialTextureIDsRef, JNI_ABORT);
        env->ReleaseFloatArrayElements(materialDepths, materialDepthsRef, JNI_ABORT);
      }
    }
    return resultTextureID;
  }
};

// jni 接口映射
static JNINativeMethod methods[] = {
    {"nativeCreate", "()J",             (void *) ThreeDimensionalFilterJni::nativeCreate},
    {"nativeDestroy","(J)V",            (void *) ThreeDimensionalFilterJni::nativeDestroy},
    {"nativeInit",   "(JII)V",          (void *) ThreeDimensionalFilterJni::nativeInit},
    {"nativeRelease","(J)V",            (void *) ThreeDimensionalFilterJni::nativeRelease},
    {"nativeReset",  "(J)V",            (void *) ThreeDimensionalFilterJni::nativeReset},
    {"nativeRender", "(JII[I[FJ)I",     (void *) ThreeDimensionalFilterJni::nativeRender},
};

int registerThreeDimensionalFilterNativeMethods(JNIEnv *env, void *reserved) {
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