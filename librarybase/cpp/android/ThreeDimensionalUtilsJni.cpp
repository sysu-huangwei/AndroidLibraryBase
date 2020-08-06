//
// Created by rayyy on 2020/8/5.
//

#include "ThreeDimensionalUtilsJni.h"
#include <vector>
#include "ThreeDimensionalUtils.h"
#include "ThreeDimensionalOneFrameData.h"

#define CLASS_NAME_PATH "com/example/librarybase/threedimensional/ThreeDimensionalUtils"

namespace threedimensional {

class ThreeDimensionalUtilsJni {
 public:
  static jlongArray nativeCalculateThreeDimensionalData(JNIEnv *env, jclass clazz, jint directionType, jfloat depthScale, jfloat perspectiveScale, jfloat speed) {
    jlongArray threeDimensionalDataNativeInstances = 0;
    std::vector<ThreeDimensionalOneFrameData> threeDimensionalOneFrameDatas = ThreeDimensionalUtils::calculateThreeDimensionalData(directionType, depthScale, perspectiveScale, speed);
    if (!threeDimensionalOneFrameDatas.empty()) {
      threeDimensionalDataNativeInstances = env->NewLongArray(threeDimensionalOneFrameDatas.size());
      for (int i = 0; i < threeDimensionalOneFrameDatas.size(); ++i) {
        ThreeDimensionalOneFrameData *threeDimensionalOneFrameDataCopy = new ThreeDimensionalOneFrameData(threeDimensionalOneFrameDatas.at(i));
        env->SetLongArrayRegion(threeDimensionalDataNativeInstances,
                                i,
                                1,
                                reinterpret_cast<jlong *>(&threeDimensionalOneFrameDataCopy));
      }
    }
    return threeDimensionalDataNativeInstances;
  }
};

// jni 接口映射
static JNINativeMethod methods[] = {
    {"nativeCalculateThreeDimensionalData", "(IFFF)[J",  (void *) ThreeDimensionalUtilsJni::nativeCalculateThreeDimensionalData},
};

int registerThreeDimensionalUtilsNativeMethods(JNIEnv *env, void *reserved) {
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