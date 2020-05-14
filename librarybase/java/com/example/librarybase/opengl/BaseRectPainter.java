package com.example.librarybase.opengl;

import android.opengl.GLES20;

import java.nio.FloatBuffer;
import java.util.ArrayList;

/**
 * User: HW
 * Date: 2020/5/13
 * Description: OpenGL 在原纹理上画矩形
 */
public class BaseRectPainter extends BasePainter {

    private ArrayList<FloatBuffer> mRectPointsBuffers = new ArrayList<>(); // 需要画的框的(left, top, right, bottom)点集合
    private int mRectCount = 0; // 框的数量
    private float[] mRectLineColor = new float[] {1.0f, 0.0f, 0.0f}; // 线的颜色
    private float mRectLineWidth = 5.0f; // 线的宽度

    private int mRectColorUniform = 0; // 线颜色uniform的位置

    private Base2DTexturePainter mBase2DTexturePainter = new Base2DTexturePainter(); // 用来画原图到输出上

    public BaseRectPainter() {
        vertexShaderString = ""
                + "attribute vec4 position;\n"
                + "uniform mat4 mvpMatrix;\n"
                + "void main()\n"
                + "{\n"
                + "    gl_Position = mvpMatrix * position;\n"
                + "}\n";

        fragmentShaderString = ""
                + "precision highp float;\n"
                + "uniform vec3 rectColor;\n"
                + "void main()\n"
                + "{\n"
                + "    gl_FragColor = vec4(rectColor, 1.0);\n"
                + "}\n";
    }

    @Override
    public void init() {
        mBase2DTexturePainter.init();
        mProgram = BaseGLUtils.createProgram(vertexShaderString, fragmentShaderString);
        if (mProgram > 0) {
            // 获取顶点坐标位置
            mPositionAttribute = GLES20.glGetAttribLocation(mProgram, "position");
            // 获取变换矩阵位置
            mMvpMatrixUniform = GLES20.glGetUniformLocation(mProgram, "mvpMatrix");
            // 获取线颜色uniform的位置
            mRectColorUniform = GLES20.glGetUniformLocation(mProgram, "rectColor");
        }
    }

    @Override
    public void render(int inputTexture, int inputTextureWidth, int inputTextureHeight, int outputWidth, int outputHeight, int orientation) {
        // 先把原图画到输出上
        mBase2DTexturePainter.render(inputTexture, inputTextureWidth, inputTextureHeight, outputWidth, outputHeight, orientation);
        if (mRectPointsBuffers.isEmpty()) {
            return;
        }

        // 使用着色器绘制程序
        GLES20.glUseProgram(mProgram);

        // 计算和传入变换矩阵
        getMvpMatrix(mMvpMatrix, (float) outputWidth / (float) outputHeight, inputTextureWidth, inputTextureHeight);
        GLES20.glUniformMatrix4fv(mMvpMatrixUniform, 1, false, mMvpMatrix, 0);

        // 传入线的宽度
        GLES20.glLineWidth(mRectLineWidth);

        // 传入线的颜色
        GLES20.glUniform3fv(mRectColorUniform, 1, mRectLineColor, 0);

        GLES20.glEnableVertexAttribArray(mPositionAttribute);
        for (int i = 0; i < mRectCount; i++) {
            // 传入顶点位置
            GLES20.glVertexAttribPointer(mPositionAttribute, mCoordinatesCountPerVertex, GLES20.GL_FLOAT, false, 8, mRectPointsBuffers.get(i));

            // 绘制
            GLES20.glDrawArrays(GLES20.GL_LINE_LOOP, 0, 4);
        }
        GLES20.glDisableVertexAttribArray(mPositionAttribute);
    }

    /**
     * 设置需要绘制的点
     * @param rectPoints 归一化后的点，{left1, top1, right1, bottom1, left2, ...} 的形式
     */
    public void setRectPoints(float[] rectPoints) {
        mRectPointsBuffers.clear();
        if (rectPoints != null && rectPoints.length >= 4) {
            mRectCount = rectPoints.length / 4;
            for (int i = 0; i < mRectCount; i++) {
                mRectPointsBuffers.add(BaseGLUtils.floatArrayToFloatBuffer(new float[] {rectPoints[4 * i], rectPoints[4 * i + 1], rectPoints[4 * i + 2], rectPoints[4 * i + 1], rectPoints[4 * i + 2], rectPoints[4 * i + 3], rectPoints[4 * i], rectPoints[4 * i + 3]}));
            }
        }
    }

    /**
     * 设置线的颜色
     * @param r 0 ~ 1
     * @param g 0 ~ 1
     * @param b 0 ~ 1
     */
    public void setRectLineColor(float r, float g, float b) {
        mRectLineColor[0] = r;
        mRectLineColor[1] = g;
        mRectLineColor[2] = b;
    }

    /**
     * 设置线的宽度
     * @param rectLineWidth 线的宽度，单位是像素值
     */
    public void setRectLineWidth(float rectLineWidth) {
        mRectLineWidth = rectLineWidth;
    }
}
