package com.example.librarybase.threedimensional;

import android.opengl.GLES20;
import android.util.Pair;
import java.util.ArrayList;

/**
 * User: rayyyhuang
 * Date: 2020/8/3
 * Description: 3D效果组合滤镜
 */
public class ThreeDimensionalFilter {

    /* 膨胀 */
    private DilateFilter dilateFilter = new DilateFilter();

    /* 高斯模糊 */
    private GaussianBlurFilter gaussianBlurFilter = new GaussianBlurFilter();

    /* 景深（加上穿插素材） */
    private DepthFilter depthFilter = new DepthFilter();

    /* 透视 */
    private PerspectiveFilter perspectiveFilter = new PerspectiveFilter();

    /* 膨胀和模糊之后的景深图纹理ID */
    private int processedDepthTextureID = 0;

    /* 是否需要重置 */
    private boolean isNeedReset = false;

    /**
     * 初始化，必须在GL线程
     *
     * @param width 宽
     * @param height 高
     */
    public void init(int width, int height) {
        dilateFilter.init(width, height);
        gaussianBlurFilter.init(width, height);
        depthFilter.init(width, height);
        perspectiveFilter.init(width, height);
    }

    /**
     * 释放资源，必须在GL线程
     */
    public void release() {
        dilateFilter.release();
        gaussianBlurFilter.release();
        depthFilter.release();
        perspectiveFilter.release();
        if (processedDepthTextureID != 0) {
            GLES20.glDeleteTextures(1, new int[]{processedDepthTextureID}, 0);
            processedDepthTextureID = 0;
        }
    }

    /**
     * 清除之前的绘制信息，当输入新的图片和景深的时候需要调用此接口
     */
    public void reset() {
        isNeedReset = true;
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
    public int render(int imageTextureID, int originDepthTextureID, ArrayList<Pair<Integer, Float>> materialTextureAndDepth, ThreeDimensionalOneFrameData threeDimensionalOneFrameData) {
        if (isNeedReset && processedDepthTextureID != 0) {
            GLES20.glDeleteTextures(1, new int[]{processedDepthTextureID}, 0);
            processedDepthTextureID = 0;
            isNeedReset = false;
        }
        if (processedDepthTextureID == 0) {
            int dilateDepthTextureID = dilateFilter.render(originDepthTextureID);
            processedDepthTextureID = gaussianBlurFilter.render(dilateDepthTextureID);
        }
        int imageDepthTextureID = depthFilter.render(imageTextureID, originDepthTextureID, processedDepthTextureID, materialTextureAndDepth, threeDimensionalOneFrameData.xShift, threeDimensionalOneFrameData.yShift, threeDimensionalOneFrameData.depthScale);
        int perspectiveTextureID = perspectiveFilter.render(imageDepthTextureID, threeDimensionalOneFrameData.mvpMatrix);
        return perspectiveTextureID;
    }
}
