//
// Created by HW on 2020/4/24.
//

#ifndef ANDROIDLIBRARYBASE_NATIVEBITMAP_H
#define ANDROIDLIBRARYBASE_NATIVEBITMAP_H

namespace librarybase {

    /**
     * 安卓底层图片数据，可以不占用java内存
     */
    class NativeBitmap {
    public:
        /**
          * 像素颜色空间格式，目前只支持RGBA和GRAY两种常用的格式
          */
        typedef enum NativeBitmapColorSpace {
            NativeBitmapColorSpace_RGBA = 1,
            NativeBitmapColorSpace_GRAY = 2
        } NativeBitmapColorSpace;

        NativeBitmap();

        NativeBitmap(NativeBitmap &nativeBitmap);

        ~NativeBitmap();


        /**
         * 获取像素数组的长度
         * @return 像素数组的长度
         */
        int getPixelsArrayLength();

        /**
         * 获取底层像素数据的指针引用，外部不能释放
         * @return 底层像素数据的指针引用
         */
        unsigned char *getPixelsRef();

        /**
         * 获取底层像素数据的指针引用，外部不能释放
         * @param width 返回宽
         * @param height 返回高
         * @param colorSpace 返回颜色空间
         * @return 底层像素数据的指针引用
         */
        unsigned char *getPixelsRef(int &width, int &height, NativeBitmapColorSpace &colorSpace);

        /**
         * 获取一份拷贝的像素数据，内存交由外部管理
         * @return 拷贝的像素数据，内存交由外部管理
         */
        unsigned char *getPixelsCopy();

        /**
         * 获取一份拷贝的像素数据，内存交由外部管理
         * @param width 返回宽
         * @param height 返回高
         * @param colorSpace 返回像素格式
         * @return 拷贝的像素数据，内存交由外部管理
         */
        unsigned char *getPixelsCopy(int &width, int &height, NativeBitmapColorSpace &colorSpace);

        /**
         * 设置新的像素数据，内部会先把旧的图像数据释放掉
         * @param pixels 新的像素数据
         * @param width 新的宽
         * @param height 新的高
         * @param colorSpace 新的颜色空间
         */
        void
        setPixels(unsigned char *pixels, int width, int height, NativeBitmapColorSpace &colorSpace);

        /**
         * 获取图像宽
         * @return 图像宽
         */
        int getWidth();

        /**
         * 获取图像高
         * @return 图像高
         */
        int getHeight();

        /**
         * 获取图像颜色空间
         * @return 图像颜色空间
         */
        NativeBitmapColorSpace getColorSpace();

        /**
         * 主动释放图像数据接口，释放后内部不再有图像数据，如果需要重新使用这个对象需要设置新的图像数据
         */
        void release();

    private:
        unsigned char *pixels;
        int width;
        int height;
        NativeBitmapColorSpace colorSpace;
    };

}
#endif //ANDROIDLIBRARYBASE_NATIVEBITMAP_H
