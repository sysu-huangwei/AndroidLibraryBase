//
// Created by HW on 2020/4/24.
//

#include "NativeBitmap.h"
#include <cstring>
#include "BaseDefine.h"

namespace librarybase {

    NativeBitmap::NativeBitmap() {
        pixels = NULL;
        width = 0;
        height = 0;
        colorSpace = NativeBitmapColorSpace_RGBA;
    }

    NativeBitmap::NativeBitmap(NativeBitmap &nativeBitmap) {
        if (nativeBitmap.pixels && nativeBitmap.width > 0 && nativeBitmap.height > 0) {
            width = nativeBitmap.width;
            height = nativeBitmap.width;
            colorSpace = nativeBitmap.colorSpace;
            int pixelsCount = width * height *
                              (nativeBitmap.colorSpace == NativeBitmapColorSpace_RGBA ? 4 : 1);
            pixels = new unsigned char[pixelsCount];
            memcpy(pixels, nativeBitmap.pixels, sizeof(unsigned char) * pixelsCount);
        }
    }

    NativeBitmap::~NativeBitmap() {
        release();
    }


    int NativeBitmap::getPixelsArrayLength() {
        return width * height * (this->colorSpace == NativeBitmapColorSpace_RGBA ? 4 : 1);
    }

    unsigned char *NativeBitmap::getPixelsRef() {
        return pixels;
    }

    unsigned char *
    NativeBitmap::getPixelsRef(int &width, int &height, NativeBitmapColorSpace &colorSpace) {
        width = this->width;
        height = this->height;
        colorSpace = this->colorSpace;
        return pixels;
    }

    unsigned char *NativeBitmap::getPixelsCopy() {
        if (pixels && width > 0 && height > 0) {
            int length = getPixelsArrayLength();
            unsigned char *pixelsCopy = new unsigned char[length];
            memcpy(pixelsCopy, pixels, sizeof(unsigned char) * length);
            return pixelsCopy;
        }
        return NULL;
    }

    unsigned char *
    NativeBitmap::getPixelsCopy(int &width, int &height, NativeBitmapColorSpace &colorSpace) {
        width = this->width;
        height = this->height;
        colorSpace = this->colorSpace;
        if (pixels && width > 0 && height > 0) {
            int length = getPixelsArrayLength();
            unsigned char *pixelsCopy = new unsigned char[length];
            memcpy(pixelsCopy, pixels, sizeof(unsigned char) * length);
            return pixelsCopy;
        }
        return NULL;
    }

    void NativeBitmap::setPixels(unsigned char *pixels, int width, int height,
                                 NativeBitmapColorSpace &colorSpace) {
        if (pixels && width > 0 && height > 0) {
            release();
            this->width = width;
            this->height = height;
            this->colorSpace = colorSpace;
            int pixelsCount =
                    width * height * (colorSpace == NativeBitmapColorSpace_RGBA ? 4 : 1);
            memcpy(this->pixels, pixels, sizeof(unsigned char) * pixelsCount);
        }
    }

    int NativeBitmap::getWidth() {
        return width;
    }

    int NativeBitmap::getHeight() {
        return height;
    }

    NativeBitmap::NativeBitmapColorSpace NativeBitmap::getColorSpace() {
        return colorSpace;
    }

    void NativeBitmap::release() {
        SAFE_DELETE_ARRAY(pixels);
        width = 0;
        height = 0;
    }

}