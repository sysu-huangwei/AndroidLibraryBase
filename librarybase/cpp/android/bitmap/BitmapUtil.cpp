//
// Created by HW on 2020/4/21.
//

#include "BitmapUtil.h"
#include <android/bitmap.h>
#include <cstring>

#include "BaseLog.h"

namespace librarybase {

    jobject BitmapUtil::createBitmap(JNIEnv *env, int width, int height,
                                     BitmapColorSpace colorSpace) {
        const char *colorSpaceStr = "ARGB_8888";
        switch (colorSpace) {
            case BitmapColorSpace_RGBA:
                colorSpaceStr = "ARGB_8888";
                break;
            case BitmapColorSpace_GRAY:
                colorSpaceStr = "ALPHA_8";
                break;
            case BitmapColorSpace_UNDEFINE:
            default:
                LOGE("BitmapUtil::createBitmap: failed: could not find BitmapColorSpace = %d",
                     colorSpace);
                break;
        }

        // 创建 Bitmap 的 Config 对象
        jclass bitmapConfigClass = env->FindClass("android/graphics/Bitmap$Config");
        jmethodID valueOfBitmapConfigMethod = env->GetStaticMethodID(bitmapConfigClass, "valueOf",
                                                                     "(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/Enum;");
        jobject bitmapConfigObject = env->CallStaticObjectMethod(bitmapConfigClass,
                                                                 valueOfBitmapConfigMethod,
                                                                 bitmapConfigClass,
                                                                 env->NewStringUTF(colorSpaceStr));

        // 创建 Bitmap 对象
        jclass bitmapClass = env->FindClass("android/graphics/Bitmap");
        jmethodID createBitmapMethod = env->GetStaticMethodID(bitmapClass, "createBitmap",
                                                              "(IILandroid/graphics/Bitmap/Config;)Landroid/graphics/Bitmap;");
        jobject bitmapObject = env->CallStaticObjectMethod(bitmapClass, createBitmapMethod, width,
                                                           height, bitmapConfigObject);

        return bitmapObject;
    }

    void BitmapUtil::getBitmapSize(JNIEnv *env, jobject bitmap, int &width, int &height) {
        AndroidBitmapInfo info;
        int ret = AndroidBitmap_getInfo(env, bitmap, &info);
        if (ret >= 0) {
            width = info.width;
            height = info.height;
        } else {
            LOGE("BitmapUtil::getBitmapSize: AndroidBitmap_getInfo failed: ret = %d", ret);
        }
    }

    unsigned char *
    BitmapUtil::lock(JNIEnv *env, jobject bitmap, int &width, int &height,
                     BitmapColorSpace &colorSpace) {
        AndroidBitmapInfo info;
        void *pixels = NULL;
        int ret;
        if (bitmap) {
            ret = AndroidBitmap_getInfo(env, bitmap, &info);
            if (ret < 0) {
                LOGE("BitmapUtil::lock: AndroidBitmap_getInfo failed: ret = %d", ret);
            }

            ret = AndroidBitmap_lockPixels(env, bitmap, &pixels);
            if (ret < 0) {
                LOGE("BitmapUtil::lock: AndroidBitmap_lockPixels failed: ret = %d", ret);
            }

            width = info.width;
            height = info.height;
            switch (info.format) {
                case ANDROID_BITMAP_FORMAT_A_8:
                    colorSpace = BitmapColorSpace_GRAY;
                    break;
                case ANDROID_BITMAP_FORMAT_RGBA_8888:
                    colorSpace = BitmapColorSpace_RGBA;
                    break;
                default:
                    colorSpace = BitmapColorSpace_UNDEFINE;
                    LOGE("BitmapUtil::lock: Bitmap format not support: %d", info.format);
                    break;
            }
        }

        //不是rgba 和 alpha 单通道图片，返回null，避免不会解析，565、4444等数据出现错误。
        if (colorSpace == BitmapColorSpace_UNDEFINE) {
            return NULL;
        }

        return (unsigned char *) pixels;
    }

    void BitmapUtil::unlock(JNIEnv *env, jobject bitmap) {
        if (bitmap) {
            AndroidBitmap_unlockPixels(env, bitmap);
        }
    }

    unsigned char *BitmapUtil::getPixels(JNIEnv *env, jobject bitmap, int &width, int &height,
                                         BitmapColorSpace outputColorSpace,
                                         bool needPreMultiplyAlpha) {
        unsigned char *pixels = NULL;
        BitmapColorSpace inputColorSpace;
        unsigned char *orgPixels = lock(env, bitmap, width, height, inputColorSpace);

        if (orgPixels && width > 0 && height > 0) {
            int channelNum;
            if (outputColorSpace == BitmapColorSpace_GRAY) {
                channelNum = 1;
            } else {
                channelNum = 4;
            }
            int pixelCount = width * height;
            pixels = new unsigned char[pixelCount * channelNum];
            unsigned char *dstCursor = pixels;
            unsigned char *srcCursor = orgPixels;

            float alphaArray[256] = {0};
            if (needPreMultiplyAlpha) {
                for (int i = 0; i < 256; ++i) {
                    alphaArray[i] = i / 255.0f;
                }
            }

            if (inputColorSpace == BitmapColorSpace_GRAY &&
                outputColorSpace == BitmapColorSpace_GRAY) {
                memcpy(pixels, orgPixels, sizeof(unsigned char) * pixelCount * channelNum);
            } else if (inputColorSpace == BitmapColorSpace_RGBA &&
                       outputColorSpace == BitmapColorSpace_GRAY) {
                for (int i = 0; i < pixelCount; ++i) {
                    dstCursor[i] = (unsigned char) (
                            (srcCursor[2] * 19595 + srcCursor[1] * 38469 + srcCursor[0] * 7472)
                                    >> 16);
                    srcCursor += 4;
                }
            } else if (inputColorSpace == BitmapColorSpace_GRAY &&
                       outputColorSpace == BitmapColorSpace_RGBA) {
                for (int i = 0; i < pixelCount; ++i) {
                    dstCursor[0] = srcCursor[i];
                    dstCursor[1] = srcCursor[i];
                    dstCursor[2] = srcCursor[i];
                    dstCursor[3] = srcCursor[i];
                    dstCursor += 4;
                }
            } else if (inputColorSpace == BitmapColorSpace_RGBA &&
                       outputColorSpace == BitmapColorSpace_RGBA) {
                if (needPreMultiplyAlpha) {
                    for (int i = 0; i < pixelCount; ++i) {
                        float alphaRat = alphaArray[srcCursor[3]];
                        dstCursor[0] = (unsigned char) (srcCursor[0] * alphaRat);
                        dstCursor[1] = (unsigned char) (srcCursor[1] * alphaRat);
                        dstCursor[2] = (unsigned char) (srcCursor[2] * alphaRat);
                        dstCursor[3] = srcCursor[3];
                        srcCursor += 4;
                        dstCursor += 4;
                    }
                } else {
                    memcpy(pixels, orgPixels, sizeof(unsigned char) * pixelCount * channelNum);
                }
            } else {
                LOGE("BitmapUtil::getPixels: failed: could not parse input color space = %d; output color space = %d;",
                     inputColorSpace, outputColorSpace);
            }
        }

        unlock(env, bitmap);

        return pixels;
    }

    void
    BitmapUtil::setPixels(JNIEnv *env, jobject bitmap, unsigned char *pixels, int width, int height,
                          BitmapColorSpace inputColorSpace,
                          bool needPreMultiplyAlpha) {
        int srcWidth = 0, srcHeight = 0;

        BitmapColorSpace outputColorSpace;
        unsigned char *orgPixels = lock(env, bitmap, srcWidth, srcHeight, outputColorSpace);

        if (orgPixels && srcWidth == width && srcHeight == height) {
            int pixelCount = width * height;
            unsigned char *dstCursor = orgPixels;
            unsigned char *srcCursor = pixels;

            float alphaArray[256] = {0};
            if (needPreMultiplyAlpha) {
                for (int i = 0; i < 256; ++i) {
                    alphaArray[i] = i / 255.0f;
                }
            }
            if (inputColorSpace == BitmapColorSpace_RGBA &&
                outputColorSpace == BitmapColorSpace_RGBA) {
                if (needPreMultiplyAlpha) {
                    for (int i = 0; i < pixelCount; ++i) {
                        float alphaRat = alphaArray[dstCursor[3]];
                        dstCursor[0] = (unsigned char) (srcCursor[0] / alphaRat);
                        dstCursor[1] = (unsigned char) (srcCursor[1] / alphaRat);
                        dstCursor[2] = (unsigned char) (srcCursor[2] / alphaRat);
                        dstCursor[3] = srcCursor[3];
                        dstCursor += 4;
                        srcCursor += 4;
                    }
                } else {
                    memcpy(orgPixels, pixels, sizeof(unsigned char) * width * height * 4);
                }
            } else if (inputColorSpace == BitmapColorSpace_RGBA &&
                       outputColorSpace == BitmapColorSpace_GRAY) {
                for (int i = 0; i < pixelCount; ++i) {
                    dstCursor[0] = (unsigned char) (
                            (srcCursor[0] * 19595 + srcCursor[1] * 38469 + srcCursor[2] * 7472)
                                    >> 16);
                    dstCursor += 1;
                    srcCursor += 4;
                }
            } else if (inputColorSpace == BitmapColorSpace_GRAY &&
                       outputColorSpace == BitmapColorSpace_RGBA) {
                for (int i = 0; i < pixelCount; ++i) {
                    dstCursor[0] = srcCursor[0];
                    dstCursor[1] = srcCursor[0];
                    dstCursor[2] = srcCursor[0];
                    dstCursor[3] = srcCursor[0];
                    dstCursor += 4;
                    srcCursor += 1;
                }
            } else if (inputColorSpace == BitmapColorSpace_GRAY &&
                       outputColorSpace == BitmapColorSpace_GRAY) {
                memcpy(dstCursor, srcCursor, sizeof(unsigned char) * width * height);
            } else {
                LOGE("BitmapUtil::setPixels failed: could not parse input color space = %d, output color space = %d",
                     inputColorSpace, outputColorSpace);
            }
        }

        unlock(env, bitmap);
    }

}