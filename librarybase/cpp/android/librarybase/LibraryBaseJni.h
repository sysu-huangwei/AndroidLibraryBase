//
// Created by HW on 2020/4/21.
//

#ifndef ANDROIDLIBRARYBASE_LIBRARYBASEJNI_H
#define ANDROIDLIBRARYBASE_LIBRARYBASEJNI_H


#include <jni.h>

namespace librarybase {

    int registerLibraryBaseNativeMethods(JNIEnv *env, void *reserved);
}

#endif //ANDROIDLIBRARYBASE_LIBRARYBASEJNI_H
