//
// Created by HW on 2020/4/24.
//

#include "NativeBitmapJni.h"
#include "NativeBitmap.h"
#include "BaseDefine.h"
#include "BaseLog.h"
#include "BitmapUtil.h"

#define CLASS_NAME_PATH "com/example/librarybase/nativebitmap/NativeBitmap"


namespace librarybase {

    class NativeBitmapJni {
    public:
        static jlong create(JNIEnv *env, jclass clazz) {
            NativeBitmap *nativeBitmap = new NativeBitmap();
            return reinterpret_cast<jlong>(nativeBitmap);
        }

        static void release(JNIEnv *env, jclass clazz, jlong native_instance) {
            NativeBitmap *nativeBitmap = reinterpret_cast<NativeBitmap *>(native_instance);
            SAFE_DELETE(nativeBitmap);
        }

        static jlong copy(JNIEnv *env, jclass clazz, jlong native_instance) {
            NativeBitmap *nativeBitmap = reinterpret_cast<NativeBitmap *>(native_instance);
            NativeBitmap *nativeBitmapCopy = NULL;
            if (nativeBitmap) {
                nativeBitmapCopy = new NativeBitmap(*nativeBitmap);
            } else {
                LOGE("NativeBitmapJni::copy: nativeBitmap obj is NULL!");
            }
            return reinterpret_cast<jlong>(nativeBitmapCopy);
        }

        static jlong getPixelsPointer(JNIEnv *env, jclass clazz, jlong native_instance) {
            NativeBitmap *nativeBitmap = reinterpret_cast<NativeBitmap *>(native_instance);
            jlong pixelsPointer = 0;
            if (nativeBitmap) {
                pixelsPointer = reinterpret_cast<jlong>(nativeBitmap->getPixelsRef());
            } else {
                LOGE("NativeBitmapJni::getPixelsPointer: nativeBitmap obj is NULL!");
            }
            return pixelsPointer;
        }

        static jint getWidth(JNIEnv *env, jclass clazz, jlong native_instance) {
            NativeBitmap *nativeBitmap = reinterpret_cast<NativeBitmap *>(native_instance);
            jint width = 0;
            if (nativeBitmap) {
                width = nativeBitmap->getWidth();
            } else {
                LOGE("NativeBitmapJni::getWidth: nativeBitmap obj is NULL!");
            }
            return width;
        }

        static jint getHeight(JNIEnv *env, jclass clazz, jlong native_instance) {
            NativeBitmap *nativeBitmap = reinterpret_cast<NativeBitmap *>(native_instance);
            jint height = 0;
            if (nativeBitmap) {
                height = nativeBitmap->getHeight();
            } else {
                LOGE("NativeBitmapJni::getHeight: nativeBitmap obj is NULL!");
            }
            return height;
        }

        static jint getColorSpace(JNIEnv *env, jclass clazz, jlong native_instance) {
            NativeBitmap *nativeBitmap = reinterpret_cast<NativeBitmap *>(native_instance);
            jint colorSpace = 0;
            if (nativeBitmap) {
                colorSpace = nativeBitmap->getColorSpace();
            } else {
                LOGE("NativeBitmapJni::getColorSpace: nativeBitmap obj is NULL!");
            }
            return colorSpace;
        }

        static jbyteArray getByteArrayCopy(JNIEnv *env, jclass clazz, jlong native_instance) {
            NativeBitmap *nativeBitmap = reinterpret_cast<NativeBitmap *>(native_instance);
            jbyteArray byteArrayCopy = 0;
            if (nativeBitmap && nativeBitmap->getPixelsRef() && nativeBitmap->getWidth() > 0 &&
                nativeBitmap->getHeight() > 0) {
                int length = nativeBitmap->getPixelsArrayLength();
                byteArrayCopy = env->NewByteArray(length);
                env->SetByteArrayRegion(byteArrayCopy, 0, length,
                                        (jbyte *) nativeBitmap->getPixelsRef());
            } else {
                LOGE("NativeBitmapJni::getByteArrayCopy: nativeBitmap obj is NULL!  or data invalid!");
            }
            return byteArrayCopy;
        }

        static jobject getByteBufferCopy(JNIEnv *env, jclass clazz, jlong native_instance) {
            NativeBitmap *nativeBitmap = reinterpret_cast<NativeBitmap *>(native_instance);
            jobject byteBufferCopy = 0;
            if (nativeBitmap && nativeBitmap->getPixelsRef() && nativeBitmap->getWidth() > 0 &&
                nativeBitmap->getHeight() > 0) {
                int length = nativeBitmap->getPixelsArrayLength();
                byteBufferCopy = env->NewDirectByteBuffer(nativeBitmap->getPixelsCopy(), length);
            } else {
                LOGE("NativeBitmapJni::getByteBufferCopy: nativeBitmap obj is NULL!  or data invalid!");
            }
            return byteBufferCopy;
        }

        static jobject toBitmap(JNIEnv *env, jclass clazz, jlong native_instance) {
            NativeBitmap *nativeBitmap = reinterpret_cast<NativeBitmap *>(native_instance);
            jobject bitmap = 0;
            if (nativeBitmap && nativeBitmap->getPixelsRef() && nativeBitmap->getWidth() > 0 &&
                nativeBitmap->getHeight() > 0) {
                NativeBitmap::NativeBitmapColorSpace nativeBitmapColorSpace = nativeBitmap->getColorSpace();
                BitmapUtil::BitmapColorSpace bitmapColorSpace = BitmapUtil::BitmapColorSpace_UNDEFINE;
                if (nativeBitmap->getColorSpace() == NativeBitmap::NativeBitmapColorSpace_RGBA) {
                    bitmapColorSpace = BitmapUtil::BitmapColorSpace_RGBA;
                } else if (nativeBitmap->getColorSpace() ==
                           NativeBitmap::NativeBitmapColorSpace_GRAY) {
                    bitmapColorSpace = BitmapUtil::BitmapColorSpace_GRAY;
                }
                bitmap = BitmapUtil::createBitmap(env, nativeBitmap->getWidth(),
                                                  nativeBitmap->getHeight(), bitmapColorSpace);
                BitmapUtil::setPixels(env, bitmap, nativeBitmap->getPixelsRef(),
                                      nativeBitmap->getWidth(), nativeBitmap->getHeight(),
                                      bitmapColorSpace);
            } else {
                LOGE("NativeBitmapJni::toBitmap: nativeBitmap obj is NULL!  or data invalid!");
            }
            return bitmap;
        }

        static jobject toRGBABitmap(JNIEnv *env, jclass clazz, jlong native_instance) {
            NativeBitmap *nativeBitmap = reinterpret_cast<NativeBitmap *>(native_instance);
            jobject bitmap = 0;
            if (nativeBitmap && nativeBitmap->getPixelsRef() && nativeBitmap->getWidth() > 0 &&
                nativeBitmap->getHeight() > 0) {
                NativeBitmap::NativeBitmapColorSpace nativeBitmapColorSpace = nativeBitmap->getColorSpace();
                BitmapUtil::BitmapColorSpace bitmapColorSpace = BitmapUtil::BitmapColorSpace_UNDEFINE;
                if (nativeBitmap->getColorSpace() == NativeBitmap::NativeBitmapColorSpace_RGBA) {
                    bitmapColorSpace = BitmapUtil::BitmapColorSpace_RGBA;
                } else if (nativeBitmap->getColorSpace() ==
                           NativeBitmap::NativeBitmapColorSpace_GRAY) {
                    bitmapColorSpace = BitmapUtil::BitmapColorSpace_GRAY;
                }
                bitmap = BitmapUtil::createBitmap(env, nativeBitmap->getWidth(),
                                                  nativeBitmap->getHeight(),
                                                  BitmapUtil::BitmapColorSpace_RGBA);
                BitmapUtil::setPixels(env, bitmap, nativeBitmap->getPixelsRef(),
                                      nativeBitmap->getWidth(), nativeBitmap->getHeight(),
                                      bitmapColorSpace);
            } else {
                LOGE("NativeBitmapJni::toRGBABitmap: nativeBitmap obj is NULL!  or data invalid!");
            }
            return bitmap;
        }

        static jobject toAlphaBitmap(JNIEnv *env, jclass clazz, jlong native_instance) {
            NativeBitmap *nativeBitmap = reinterpret_cast<NativeBitmap *>(native_instance);
            jobject bitmap = 0;
            if (nativeBitmap && nativeBitmap->getPixelsRef() && nativeBitmap->getWidth() > 0 &&
                nativeBitmap->getHeight() > 0) {
                NativeBitmap::NativeBitmapColorSpace nativeBitmapColorSpace = nativeBitmap->getColorSpace();
                BitmapUtil::BitmapColorSpace bitmapColorSpace = BitmapUtil::BitmapColorSpace_UNDEFINE;
                if (nativeBitmap->getColorSpace() == NativeBitmap::NativeBitmapColorSpace_RGBA) {
                    bitmapColorSpace = BitmapUtil::BitmapColorSpace_RGBA;
                } else if (nativeBitmap->getColorSpace() ==
                           NativeBitmap::NativeBitmapColorSpace_GRAY) {
                    bitmapColorSpace = BitmapUtil::BitmapColorSpace_GRAY;
                }
                bitmap = BitmapUtil::createBitmap(env, nativeBitmap->getWidth(),
                                                  nativeBitmap->getHeight(),
                                                  BitmapUtil::BitmapColorSpace_GRAY);
                BitmapUtil::setPixels(env, bitmap, nativeBitmap->getPixelsRef(),
                                      nativeBitmap->getWidth(), nativeBitmap->getHeight(),
                                      bitmapColorSpace);
            } else {
                LOGE("NativeBitmapJni::toAlphaBitmap: nativeBitmap obj is NULL!  or data invalid!");
            }
            return bitmap;
        }

        static void setBitmap(JNIEnv *env, jclass clazz, jlong native_instance, jobject bitmap) {
            NativeBitmap *nativeBitmap = reinterpret_cast<NativeBitmap *>(native_instance);
            if (nativeBitmap) {
                if (bitmap) {
                    int width = 0, height = 0;
                    BitmapUtil::BitmapColorSpace bitmapColorSpace;
                    unsigned char *pixelsRef = BitmapUtil::lock(env, bitmap, width, height,
                                                                bitmapColorSpace);
                    if (pixelsRef && width > 0 && height > 0 &&
                        bitmapColorSpace != BitmapUtil::BitmapColorSpace_UNDEFINE) {
                        NativeBitmap::NativeBitmapColorSpace nativeBitmapColorSpace;
                        if (bitmapColorSpace == BitmapUtil::BitmapColorSpace_RGBA) {
                            nativeBitmapColorSpace = NativeBitmap::NativeBitmapColorSpace_RGBA;
                        } else if (bitmapColorSpace == BitmapUtil::BitmapColorSpace_GRAY) {
                            nativeBitmapColorSpace = NativeBitmap::NativeBitmapColorSpace_GRAY;
                        }
                        nativeBitmap->setPixels(pixelsRef, width, height, nativeBitmapColorSpace);
                        BitmapUtil::unlock(env, bitmap);
                    }
                } else {
                    LOGE("NativeBitmapJni::setBitmap: bitmap obj is NULL!");
                }
            } else {
                LOGE("NativeBitmapJni::setBitmap: nativeBitmap obj is NULL!");
            }
        }

        static void setByteArray(JNIEnv *env, jclass clazz, jlong native_instance, jbyteArray bytes,
                                 jint width, jint height, jint color_space) {
            NativeBitmap *nativeBitmap = reinterpret_cast<NativeBitmap *>(native_instance);
            if (nativeBitmap) {
                if (bytes && width > 0 && height > 0) {
                    NativeBitmap::NativeBitmapColorSpace nativeBitmapColorSpace = (NativeBitmap::NativeBitmapColorSpace) color_space;
                    jbyte *pixelsRef = env->GetByteArrayElements(bytes, JNI_FALSE);
                    nativeBitmap->setPixels((unsigned char *) pixelsRef, width, height,
                                            nativeBitmapColorSpace);
                    env->ReleaseByteArrayElements(bytes, pixelsRef, JNI_ABORT);
                } else {
                    LOGE("NativeBitmapJni::setByteArray: input data invalid: bytes = %p, width = %d, height = %d",
                         bytes, width, height);
                }
            } else {
                LOGE("NativeBitmapJni::setByteArray: nativeBitmap obj is NULL!");
            }
        }

        static void setByteBuffer(JNIEnv *env, jclass clazz, jlong native_instance,
                                  jobject byte_buffer, jint width, jint height, jint color_space) {
            NativeBitmap *nativeBitmap = reinterpret_cast<NativeBitmap *>(native_instance);
            if (nativeBitmap) {
                if (byte_buffer && width > 0 && height > 0) {
                    NativeBitmap::NativeBitmapColorSpace nativeBitmapColorSpace = (NativeBitmap::NativeBitmapColorSpace) color_space;
                    jbyte *pixelsRef = (jbyte *) env->GetDirectBufferAddress(byte_buffer);
                    nativeBitmap->setPixels((unsigned char *) pixelsRef, width, height,
                                            nativeBitmapColorSpace);
                } else {
                    LOGE("NativeBitmapJni::setByteBuffer: input data invalid: byte_buffer = %p, width = %d, height = %d",
                         byte_buffer, width, height);
                }
            } else {
                LOGE("NativeBitmapJni::setByteBuffer: nativeBitmap obj is NULL!");
            }
        }
    };

    // jni 接口映射
    static JNINativeMethod methods[] = {
            {"nativeCreate",            "()J",                           (void *) NativeBitmapJni::create},
            {"nativeRelease",           "(J)V",                          (void *) NativeBitmapJni::release},
            {"nativeCopy",              "(J)J",                          (void *) NativeBitmapJni::copy},
            {"nativeGetPixelsPointer",  "(J)J",                          (void *) NativeBitmapJni::getPixelsPointer},
            {"nativeGetWidth",          "(J)I",                          (void *) NativeBitmapJni::getWidth},
            {"nativeGetHeight",         "(J)I",                          (void *) NativeBitmapJni::getHeight},
            {"nativeGetColorSpace",     "(J)I",                          (void *) NativeBitmapJni::getColorSpace},
            {"nativeGetByteArrayCopy",  "(J)[B",                         (void *) NativeBitmapJni::getByteArrayCopy},
            {"nativeGetByteBufferCopy", "(J)Ljava/nio/ByteBuffer;",      (void *) NativeBitmapJni::getByteBufferCopy},
            {"nativeToBitmap",          "(J)Landroid/graphics/Bitmap;",  (void *) NativeBitmapJni::toBitmap},
            {"nativeToRGBABitmap",      "(J)Landroid/graphics/Bitmap;",  (void *) NativeBitmapJni::toRGBABitmap},
            {"nativeToAlphaBitmap",     "(J)Landroid/graphics/Bitmap;",  (void *) NativeBitmapJni::toAlphaBitmap},
            {"nativeSetBitmap",         "(JLandroid/graphics/Bitmap;)V", (void *) NativeBitmapJni::setBitmap},
            {"nativeSetByteArray",      "(J[BIII)V",                     (void *) NativeBitmapJni::setByteArray},
            {"nativeSetByteBuffer",     "(JLjava/nio/ByteBuffer;III)V",  (void *) NativeBitmapJni::setByteBuffer},
    };

    int registerNativeBitmapNativeMethods(JNIEnv *env, void *reserved) {
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