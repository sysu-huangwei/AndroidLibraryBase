package com.example.librarybase.threedimensional;

import android.util.Pair;
import androidx.annotation.NonNull;
import java.util.ArrayList;

/**
 * User: rayyyhuang
 * Date: 2020/8/3
 * Description: 3D效果组合滤镜
 */
public class ThreeDimensionalFilter {

//    static {
//        System.loadLibrary("threedimensional");
//    }

    /* 底层实例指针 */
    private long nativeInstance = 0L;

    public ThreeDimensionalFilter() {
        nativeInstance = nativeCreate();
    }

    @Override
    protected void finalize() {
        nativeDestroy(nativeInstance);
        nativeInstance = 0L;
    }

    /**
     * 初始化，必须在GL线程
     *
     * @param width 宽
     * @param height 高
     */
    public void init(int width, int height) {
        nativeInit(nativeInstance, width, height);
    }

    /**
     * 释放资源，必须在GL线程
     */
    public void release() {
        nativeRelease(nativeInstance);
    }

    /**
     * 清除之前的绘制信息，当输入新的[尺寸不变的]图片和景深的时候需要调用此接口
     * 如果新的图片尺寸变了，需要release之后重新init
     */
    public void reset() {
        nativeReset(nativeInstance);
    }

    /**
     * 绘制3D效果
     *
     * @param imageTextureID 原始图片的纹理ID
     * @param originDepthTextureID 原始景深图的纹理ID
     * @param materialTextureAndDepth 素材的纹理ID及其对应的景深值
     * @param threeDimensionalOneFrameData 3D效果其他参数
     * @return 结果纹理ID
     */
    public int render(int imageTextureID, int originDepthTextureID,
            @NonNull ArrayList<Pair<Integer, Float>> materialTextureAndDepth,
            ThreeDimensionalOneFrameData threeDimensionalOneFrameData) {
        int[] materialTextureIDs = new int[materialTextureAndDepth.size()];
        float[] materialDepths = new float[materialTextureAndDepth.size()];
        for (int i = 0; i < materialTextureAndDepth.size(); i++) {
            Pair<Integer, Float> pair = materialTextureAndDepth.get(i);
            materialTextureIDs[i] = pair.first;
            materialDepths[i] = pair.second;
        }
        return nativeRender(nativeInstance, imageTextureID, originDepthTextureID, materialTextureIDs, materialDepths, threeDimensionalOneFrameData.getNativeInstance());
    }

    private native long nativeCreate();

    private native void nativeDestroy(long nativeInstance);

    private native void nativeInit(long nativeInstance, int width, int height);

    private native void nativeRelease(long nativeInstance);

    private native void nativeReset(long nativeInstance);

    private native int nativeRender(long nativeInstance,
            int imageTextureID, int originDepthTextureID,
            int[] materialTextureIDs, float[] materialDepths,
            long threeDimensionalOneFrameDataPointer);
}
