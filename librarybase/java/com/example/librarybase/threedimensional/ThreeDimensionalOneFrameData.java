package com.example.librarybase.threedimensional;


/**
 * User: rayyyhuang
 * Date: 2020/7/31
 * Description: 做3D效果一帧需要的数据
 */
public class ThreeDimensionalOneFrameData {

    /* 景深效果程度 */
    public float depthScale = 0.0f;

    /* 透视效果程度 */
    public float perspectiveScale = 0.0f;

    /* x方向的景深偏移值 */
    public float xShift = 0.0f;

    /* y方向的景深偏移值 */
    public float yShift = 0.0f;

    /* MVP矩阵，4*4 */
    public float[] mvpMatrix = new float[16];


}
