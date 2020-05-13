package com.example.librarybase.opengl;

import android.opengl.GLES20;

import java.nio.FloatBuffer;

/**
 * User: HW
 * Date: 2020/5/13
 * Description:
 */
public class BasePointPainter extends BasePainter {

    private FloatBuffer mPointsBuffer = null; // 需要画的点
    private int mPointCount = 0;
    private float[] mPointColor = new float[] {1.0f, 0.0f, 0.0f}; // 点的颜色
    private float mPointSize = 5.0f; // 点的大小

    private int pointSizeUniform = 0; // 点大小uniform位置
    private int pointColorUniform = 0; // 点颜色uniform位置

    private Base2DTexturePainter mBase2DTexturePainter = new Base2DTexturePainter(); // 用来画原图到输出上

    public BasePointPainter() {
        vertexShaderString = ""
                + "attribute vec4 position;\n"
                + "uniform mat4 mvpMatrix;\n"
                + "uniform float pointSize;\n"
                + "void main()\n"
                + "{\n"
                + "    gl_Position = mvpMatrix * position;\n"
                + "    gl_PointSize = pointSize;\n"
                + "}\n";

        fragmentShaderString = ""
                + "precision highp float;\n"
                + "uniform vec3 pointColor;\n"
                + "void main()\n"
                + "{\n"
                + "    gl_FragColor = vec4(pointColor, 1.0);\n"
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
            // 获取点大小uniform的位置
            pointSizeUniform = GLES20.glGetUniformLocation(mProgram, "pointSize");
            // 获取点颜色uniform的位置
            pointColorUniform = GLES20.glGetUniformLocation(mProgram, "pointColor");
        }
    }

    @Override
    public void render(int inputTexture, int inputTextureWidth, int inputTextureHeight, int outputWidth, int outputHeight, @BaseOrientationEnum int orientation) {
        // 先把原图画到输出上
        mBase2DTexturePainter.render(inputTexture, inputTextureWidth, inputTextureHeight, outputWidth, outputHeight, orientation);
        if (mPointsBuffer == null) {
            return;
        }

        // 使用着色器绘制程序
        GLES20.glUseProgram(mProgram);

        // 传入顶点位置
        GLES20.glEnableVertexAttribArray(mPositionAttribute);
        GLES20.glVertexAttribPointer(mPositionAttribute, mCoordinatesCountPerVertex, GLES20.GL_FLOAT, false, 8, mPointsBuffer);

        // 计算和传入变换矩阵
        getMvpMatrix(mMvpMatrix, (float) outputWidth / (float) outputHeight, inputTextureWidth, inputTextureHeight);
        GLES20.glUniformMatrix4fv(mMvpMatrixUniform, 1, false, mMvpMatrix, 0);

        // 传入点的大小
        GLES20.glUniform1f(pointSizeUniform, mPointSize);

        // 传入点的颜色
        GLES20.glUniform3fv(pointColorUniform, 1, mPointColor, 0);

        // 绘制
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, mPointCount);

        GLES20.glDisableVertexAttribArray(mPositionAttribute);
    }

    /**
     * 设置需要绘制的点
     * @param points 归一化后的点，{x0, y0, x1, y1, ...} 的形式
     */
    public void setPoints(float[] points) {
        if (points != null && points.length > 0) {
            mPointCount = points.length / 2;
            mPointsBuffer = BaseGLUtils.floatArrayToFloatBuffer(points);
        }
    }

    /**
     * 设置点的颜色
     * @param r 0 ~ 1
     * @param g 0 ~ 1
     * @param b 0 ~ 1
     */
    public void setPointColor(float r, float g, float b) {
        mPointColor[0] = r;
        mPointColor[1] = g;
        mPointColor[2] = b;
    }

    /**
     * 设置点的大小
     * @param pointSize 点的大小，单位是像素值
     */
    public void setPointSize(float pointSize) {
        mPointSize = pointSize;
    }
}
