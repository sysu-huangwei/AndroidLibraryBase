//
// Created by HW on 2020/4/21.
//

#ifndef ANDROIDLIBRARYBASE_BITMAPUTIL_H
#define ANDROIDLIBRARYBASE_BITMAPUTIL_H

#include <jni.h>

namespace librarybase {

    class BitmapUtil {
    public:
        /**
         * 像素颜色空间格式，目前只支持RGBA和GRAY两种常用的格式
         */
        typedef enum BitmapColorSpace {
            BitmapColorSpace_UNDEFINE = 0,
            BitmapColorSpace_RGBA = 1,
            BitmapColorSpace_GRAY = 2
        } BitmapColorSpace;

        /**
         * 创建一个Bitmap对象
         * @param width 宽
         * @param height 高
         * @param colorSpace 像素颜色空间格式，默认是RGBA
         * @return
         */
        static jobject createBitmap(JNIEnv *env, int width, int height,
                                    BitmapColorSpace colorSpace = BitmapColorSpace_RGBA);

        /**
         * 获取一个Bitmap对象的尺寸
         * @param bitmap Bitmap对象
         * @param width 输出宽
         * @param height 输出高
         */
        static void getBitmapSize(JNIEnv *env, jobject bitmap, int &width, int &height);

        /**
         * 锁定Bitmap原生像素缓存并获取Bitmap原生像素缓存地址，lock期间Bitmap原生像素缓存不会被改变，lock需要和unlock配套使用
         * @param bitmap Bitmap对象
         * @param width 输出宽
         * @param height 输出高
         * @param colorSpace 输出像素颜色空间格式
         * @return 原生像素缓存
         */
        static unsigned char *
        lock(JNIEnv *env, jobject bitmap, int &width, int &height, BitmapColorSpace &colorSpace);

        /**
         * 解锁Bitmap原生像素缓存，需要和lock配套使用
         * @param bitmap Bitmap对象
         */
        static void unlock(JNIEnv *env, jobject bitmap);

        /**
         * 获取一份拷贝的图像数据，需要外部释放
         * @param bitmap Bitmap对象
         * @param width 输出宽
         * @param height 输出高
         * @param outputColorSpace 期望得到的像素数据格式，默认是RGBA
         * @param needPreMultiplyAlpha 是否需要预乘，默认否。因为Android Bitmap对象存储的C图像数据已经经过预乘处理(RGB三个通道与Alpha通道做乘法)，所以如果需要得到正确的RGB值，需要将RGB数据反推回来。(非半透明图无影响)
         * @return 像素数据，需要外部释放
         */
        static unsigned char *getPixels(JNIEnv *env, jobject bitmap, int &width, int &height,
                                        BitmapColorSpace outputColorSpace = BitmapColorSpace_RGBA,
                                        bool needPreMultiplyAlpha = false);

        /**
         * 设置外部像素数据给一个Bitmap对象
         * @param bitmap Bitmap对象
         * @param pixels 外部像素数据
         * @param width 宽
         * @param height 高
         * @param inputColorSpace 输入像素数据的格式，默认当成是RGBA格式
         * @param needPreMultiplyAlpha 是否需要预乘，默认否。因为Android Bitmap对象存储的C图像数据已经经过预乘处理(RGB三个通道与Alpha通道做乘法)，所以如果需要得到正确的RGB值，需要将RGB数据反推回来。(非半透明图无影响)
         */
        static void
        setPixels(JNIEnv *env, jobject bitmap, unsigned char *pixels, int width, int height,
                  BitmapColorSpace inputColorSpace = BitmapColorSpace_RGBA,
                  bool needPreMultiplyAlpha = false);
    };

}


#endif //ANDROIDLIBRARYBASE_BITMAPUTIL_H
