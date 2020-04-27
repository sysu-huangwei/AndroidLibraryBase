package com.example.librarybase.nativebitmap;

import android.graphics.Bitmap;

import androidx.annotation.IntDef;

import com.example.librarybase.LibraryBase;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.ByteBuffer;

/**
 * User: HW
 * Date: 2020/4/24
 * Description: 安卓底层图片数据，可以不占用java内存
 */
public class NativeBitmap extends LibraryBase {

    /**
     * 像素颜色空间格式，目前只支持RGBA和GRAY两种常用的格式
     */
    public static final int NativeBitmapColorSpace_RGBA = 1;
    public static final int NativeBitmapColorSpace_GRAY = 2;

    @IntDef({NativeBitmapColorSpace_RGBA, NativeBitmapColorSpace_GRAY})
    @Retention(RetentionPolicy.SOURCE)
    public @interface NativeBitmapColorSpace {
    }


    private long nativeInstance = 0; //底层实例指针，指向底层c++类对象的地址

    public NativeBitmap() {
        tryRunNativeMethod(new Runnable() {
            @Override
            public void run() {
                nativeInstance = nativeCreate();
            }
        });
    }

    /**
     * 使用Bitmap创建，会自动填充这个Bitmap的图像数据
     *
     * @param bitmap Bitmap图像
     */
    public NativeBitmap(Bitmap bitmap) {
        this();
        setBitmap(bitmap);
    }

    /**
     * 外部在使用完NativeBitmap的时候要马上调用这个接口释放底层内存，有的机子可能不会调用finalize导致内存泄漏
     */
    public void recycle() {
        nativeRelease(nativeInstance);
        nativeInstance = 0;
    }

    public NativeBitmap copy() {
        NativeBitmap nativeBitmapCopy = new NativeBitmap();
        nativeBitmapCopy.nativeInstance = nativeCopy(nativeInstance);
        return nativeBitmapCopy;
    }

    /**
     * 获取c++底层像素的内存地址
     *
     * @return c++底层像素的内存地址
     */
    public long getPixelsPointer() {
        return nativeGetPixelsPointer(nativeInstance);
    }

    /**
     * 获取图像宽
     *
     * @return 图像宽
     */
    public int getWidth() {
        return nativeGetWidth(nativeInstance);
    }

    /**
     * 获取图像高
     *
     * @return 图像高
     */
    public int getHeight() {
        return nativeGetHeight(nativeInstance);
    }

    /**
     * 获取图像颜色空间
     *
     * @return 图像颜色空间
     */
    public @NativeBitmapColorSpace
    int getColorSpace() {
        return nativeGetColorSpace(nativeInstance);
    }

    /**
     * 获取一份拷贝的图像像素数据
     *
     * @return 拷贝的图像像素数据
     */
    public byte[] getByteArrayCopy() {
        return nativeGetByteArrayCopy(nativeInstance);
    }

    /**
     * 获取一份拷贝的图像像素数据
     *
     * @return 拷贝的图像像素数据
     */
    public ByteBuffer getByteBufferCopy() {
        return nativeGetByteBufferCopy(nativeInstance);
    }

    /**
     * 转成Bitmap图象
     *
     * @return Bitmap图象
     */
    public Bitmap toBitmap() {
        return nativeToBitmap(nativeInstance);
    }

    /**
     * 强制转成RGBA格式的Bitmap图像
     *
     * @return RGBA格式的Bitmap图像
     */
    public Bitmap toRGBABitmap() {
        return nativeToRGBABitmap(nativeInstance);
    }

    /**
     * 强制转成GRAY格式的Bitmap图像
     *
     * @return GRAY格式的Bitmap图像
     */
    public Bitmap toAlphaBitmap() {
        return nativeToAlphaBitmap(nativeInstance);
    }

    /**
     * 设置外部Bitmap图像数据，会先清楚原来内部的图像数据
     *
     * @param bitmap 外部Bitmap图像
     */
    public void setBitmap(Bitmap bitmap) {
        nativeSetBitmap(nativeInstance, bitmap);
    }

    /**
     * 设置外部图像像素数据
     *
     * @param bytes      图像像素数据
     * @param width      宽
     * @param height     高
     * @param colorSpace 颜色空间
     */
    public void setByteArray(byte[] bytes, int width, int height, @NativeBitmapColorSpace int colorSpace) {
        nativeSetByteArray(nativeInstance, bytes, width, height, colorSpace);
    }

    /**
     * 设置外部图像像素数据
     *
     * @param byteBuffer 图像像素数据
     * @param width      宽
     * @param height     高
     * @param colorSpace 颜色空间
     */
    public void setByteBuffer(ByteBuffer byteBuffer, int width, int height, @NativeBitmapColorSpace int colorSpace) {
        nativeSetByteBuffer(nativeInstance, byteBuffer, width, height, colorSpace);
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            recycle();
        } finally {
            super.finalize();
        }
    }


    private static native long nativeCreate();

    private static native void nativeRelease(long nativeInstance);

    private static native long nativeCopy(long nativeInstance);

    private static native long nativeGetPixelsPointer(long nativeInstance);

    private static native int nativeGetWidth(long nativeInstance);

    private static native int nativeGetHeight(long nativeInstance);

    private static native @NativeBitmapColorSpace
    int nativeGetColorSpace(long nativeInstance);

    private static native byte[] nativeGetByteArrayCopy(long nativeInstance);

    private static native ByteBuffer nativeGetByteBufferCopy(long nativeInstance);

    private static native Bitmap nativeToBitmap(long nativeInstance);

    private static native Bitmap nativeToRGBABitmap(long nativeInstance);

    private static native Bitmap nativeToAlphaBitmap(long nativeInstance);

    private static native void nativeSetBitmap(long nativeInstance, Bitmap bitmap);

    private static native void nativeSetByteArray(long nativeInstance, byte[] bytes, int width, int height, @NativeBitmapColorSpace int colorSpace);

    private static native void nativeSetByteBuffer(long nativeInstance, ByteBuffer byteBuffer, int width, int height, @NativeBitmapColorSpace int colorSpace);
}
