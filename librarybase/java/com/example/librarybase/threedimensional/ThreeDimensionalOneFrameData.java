package com.example.librarybase.threedimensional;


/**
 * User: rayyyhuang
 * Date: 2020/7/31
 * Description: 做3D效果一帧需要的数据
 */
public class ThreeDimensionalOneFrameData {

//    static {
//        System.loadLibrary("threedimensional");
//    }

    /* 底层实例指针 */
    private long nativeInstance = 0L;

    private ThreeDimensionalOneFrameData() {
        nativeInstance = nativeCreate();
    }

    ThreeDimensionalOneFrameData (long nativeInstance) {
        this.nativeInstance = nativeInstance;
    }

    @Override
    protected void finalize() {
        nativeDestroy(nativeInstance);
        nativeInstance = 0L;
    }

    /**
     * 获取底层实例指针
     */
    long getNativeInstance() {
        return nativeInstance;
    }

    /**
     * 景深效果程度
     */
    public float getDepthScale() {
        return nativeGetDepthScale(nativeInstance);
    }

    /**
     * 透视效果程度
     */
    public float getPerspectiveScale() {
        return nativeGetPerspectiveScale(nativeInstance);
    }

    /**
     * x方向的景深偏移值
     */
    public float getXShift() {
        return nativeGetXShift(nativeInstance);
    }

    /**
     * y方向的景深偏移值
     */
    public float getYShift() {
        return nativeGetYShift(nativeInstance);
    }

    /**
     * MVP矩阵，4*4
     */
    public float[] getMvpMatrix() {
        return nativeGetMvpMatrix(nativeInstance);
    }

    private native long nativeCreate();

    private native void nativeDestroy(long nativeInstance);

    private native float nativeGetDepthScale(long nativeInstance);

    private native float nativeGetPerspectiveScale(long nativeInstance);

    private native float nativeGetXShift(long nativeInstance);

    private native float nativeGetYShift(long nativeInstance);

    private native float[] nativeGetMvpMatrix(long nativeInstance);

}
