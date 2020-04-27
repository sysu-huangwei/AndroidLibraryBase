//
// Created by HW on 2020/4/24.
//

#ifndef ANDROIDLIBRARYBASE_NATIVEBITMAPJNI_H
#define ANDROIDLIBRARYBASE_NATIVEBITMAPJNI_H


#include <jni.h>

namespace librarybase {

    int registerNativeBitmapNativeMethods(JNIEnv *env, void *reserved);
}

#endif //ANDROIDLIBRARYBASE_NATIVEBITMAPJNI_H
