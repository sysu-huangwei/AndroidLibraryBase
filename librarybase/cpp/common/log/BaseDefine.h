//
// Created by HW on 2020/4/20.
//

#ifndef ANDROIDLIBRARYBASE_BASEDEFINE_H
#define ANDROIDLIBRARYBASE_BASEDEFINE_H


#if defined(WIN32) || defined(_WIN32) || defined(_WIN32_) || defined(WIN64) || defined(_WIN64) || defined(_WIN64_)
#define PLATFORM_WINDOWS 1 //Windows平台
#elif defined(ANDROID) || defined(_ANDROID_)
#define PLATFORM_ANDROID 1 //Android平台
#elif defined(__linux__)
#define PLATFORM_LINUX	 1 //Linux平台
#elif defined(__APPLE__) || defined(TARGET_OS_IPHONE) || defined(TARGET_IPHONE_SIMULATOR) || defined(TARGET_OS_MAC)
#define PLATFORM_IOS	 1 //iOS、Mac平台
#else
#define PLATFORM_UNKNOWN 1
#endif

#include <stdio.h>
#ifndef SAFE_DELETE
#define SAFE_DELETE(x) { if (x) delete (x); (x) = NULL; }
#endif
#ifndef SAFE_DELETE_ARRAY
#define SAFE_DELETE_ARRAY(x) { if (x) delete [] (x); (x) = NULL; }
#endif
#ifndef SAFE_FREE
#define SAFE_FREE(p) if(p != NULL) {free(p); p = NULL;}
#endif


#endif //ANDROIDLIBRARYBASE_BASEDEFINE_H
