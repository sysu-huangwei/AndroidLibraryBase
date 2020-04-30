package com.example.librarybase.opengl;

import android.opengl.GLES20;

import java.nio.FloatBuffer;


/**
 * User: HW
 * Date: 2020/4/29
 * Description: 2D纹理绘制基础类
 */
public class Base2DTexturePainter {

    // 顶点坐标
    private final float mImageVertices[] = {
            -1f, -1f, // bottom left
            1f, -1f, // bottom right
            -1f, 1f, // top left
            1f, 1f,  // top right
    };

    // 纹理坐标
    private final float[] mTextureCoordinates = {
            0f, 1f, // bottom left
            1f, 1f, // bottom right
            0f, 0f, // top left
            1f, 0f, // top right
    };

    // MVP矩阵
    private final float[] mMvpMatrix = {
            1f, 0f, 0f, 0f,
            0f, 1f, 0f, 0f,
            0f, 0f, 1f, 0f,
            0f, 0f, 0f, 1f,
    };

    // 正交矩阵，把 OpenGL 的 [-1, 1] 坐标，映射到真实的屏幕尺寸上
    private final float[] mViewportOrthogonalMatrix = {
            1f, 0f, 0f, 0f,
            0f, 1f, 0f, 0f,
            0f, 0f, 1f, 0f,
            0f, 0f, 0f, 1f,
    };

    // 正交矩阵，把纹理的尺寸，映射到 OpenGL 的 [-1, 1] 坐标上
    private final float[] mTextureOrthogonalMatrix = {
            1f, 0f, 0f, 0f,
            0f, 1f, 0f, 0f,
            0f, 0f, 1f, 0f,
            0f, 0f, 0f, 1f,
    };

    // float[]转FloatBuffer
    private final FloatBuffer mImageVerticesBuffer = BaseGLUtils.floatArrayToFloatBuffer(mImageVertices);
    private final FloatBuffer mTextureCoordinatesBuffer = BaseGLUtils.floatArrayToFloatBuffer(mTextureCoordinates);

    private final int mCoordinatesCountPerVertex = 2; // 每个顶点的坐标数
    private final int mVertexCount = mImageVertices.length / mCoordinatesCountPerVertex; // 顶点数量
    private final int vertexStride = mCoordinatesCountPerVertex * 4; // 每个坐标字节长度

    private int mProgram; // 2D纹理绘制着色器程序
    private int mPositionAttribute; // 顶点坐标位置
    private int mTextureCoordinateAttribute; // 纹理坐标位置
    private int mMvpMatrixUniform; // 变换矩阵位置
    private int mTextureUniform; // 纹理采样器位置

    private final String base2DVertexShaderString = ""
            + "attribute vec4 position;\n"
            + "attribute vec4 inputTextureCoordinate;\n"
            + "uniform mat4 mvpMatrix;\n"
            + "varying vec2 textureCoordinate;\n"
            + "void main()\n"
            + "{\n"
            + "    gl_Position = mvpMatrix * position;\n"
            + "    textureCoordinate = inputTextureCoordinate.xy;\n"
            + "}\n";

    private final String base2DFragmentShaderString = ""
            + "precision highp float;\n"
            + "varying vec2 textureCoordinate;\n"
            + "uniform sampler2D inputImageTexture;\n"
            + "void main()\n"
            + "{\n"
            + "    gl_FragColor = texture2D(inputImageTexture, textureCoordinate);\n"
            + "}\n";


    /**
     * 初始化，必须在GL线程
     */
    public void init() {
        // 创建着色器程序
        mProgram = BaseGLUtils.createProgram(base2DVertexShaderString, base2DFragmentShaderString);
        if (mProgram > 0) {
            // 获取顶点坐标位置
            mPositionAttribute = GLES20.glGetAttribLocation(mProgram, "position");
            // 获取纹理坐标位置
            mTextureCoordinateAttribute = GLES20.glGetAttribLocation(mProgram, "inputTextureCoordinate");
            // 获取变换矩阵位置
            mMvpMatrixUniform = GLES20.glGetUniformLocation(mProgram, "mvpMatrix");
            // 获取纹理采样器位置
            mTextureUniform = GLES20.glGetUniformLocation(mProgram, "inputImageTexture");

        }
    }

    /**
     * 使用完必须释放，否则会有GL内存泄露，必须在GL线程
     */
    public void release() {
        GLES20.glDeleteProgram(mProgram);
        mProgram = 0;
    }

    /**
     * 设置视图尺寸
     */
    public void viewPort(int x, int y, int width, int height) {
        GLES20.glViewport(x, y, width, height);
        // 把 OpenGL 的 [-1, 1] 坐标，映射到真实的屏幕尺寸上，这样绘制不会被拉伸变形
        float aspectRatio = width > height ? (float) width / (float) height : (float) height / (float) width;
        if (width > height) {
            android.opengl.Matrix.orthoM(mViewportOrthogonalMatrix, 0, -aspectRatio, aspectRatio, -1f, 1f, -1f, 1f);
        } else {
            android.opengl.Matrix.orthoM(mViewportOrthogonalMatrix, 0, -1f, 1f, -aspectRatio, aspectRatio, -1f, 1f);
        }
    }

    /**
     * 绘制到屏幕
     * @param inputTexture 输入纹理
     * @param inputTextureWidth 输入纹理的宽
     * @param inputTextureHeight 输入纹理的高
     */
    public void render(int inputTexture, int inputTextureWidth, int inputTextureHeight) {
        // 清屏
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glClearColor(0.0f,0.0f,0.0f,1.0f);

        // 使用着色器绘制程序
        GLES20.glUseProgram(mProgram);

        // 传入纹理
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, inputTexture);
        GLES20.glUniform1i(mTextureUniform, 0);

        // 传入顶点位置
        GLES20.glEnableVertexAttribArray(mPositionAttribute);
        GLES20.glVertexAttribPointer(mPositionAttribute, mCoordinatesCountPerVertex, GLES20.GL_FLOAT, false, vertexStride, mImageVerticesBuffer);

        // 传入纹理位置
        GLES20.glEnableVertexAttribArray(mTextureCoordinateAttribute);
        GLES20.glVertexAttribPointer(mTextureCoordinateAttribute, mCoordinatesCountPerVertex, GLES20.GL_FLOAT, false, vertexStride, mTextureCoordinatesBuffer);

        // 计算和传入变换矩阵
        getTextureMatric(mMvpMatrix, mViewportOrthogonalMatrix, mTextureOrthogonalMatrix, inputTextureWidth, inputTextureHeight);
        GLES20.glUniformMatrix4fv(mMvpMatrixUniform, 1, false, mMvpMatrix, 0);

        // 绘制
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, mVertexCount);

        GLES20.glDisableVertexAttribArray(mPositionAttribute);
        GLES20.glDisableVertexAttribArray(mTextureCoordinateAttribute);
    }

    /**
     * 由viewport矩阵和纹理尺寸，计算最终的mvp矩阵
     * @param mvpMatrix out 最终的mvp矩阵结果
     * @param viewportOrthogonalMatrix in viewport正交矩阵
     * @param textureOrthogonalMatrix in 纹理正交矩阵
     * @param textureWidth in 纹理宽
     * @param textureHeight in 纹理高
     */
    private static void getTextureMatric(float[] mvpMatrix, float[] viewportOrthogonalMatrix, float[] textureOrthogonalMatrix, int textureWidth, int textureHeight) {
        float textureAspectRatio = textureWidth > textureHeight ? (float) textureWidth / (float) textureHeight : (float) textureHeight / (float) textureWidth;
        if (textureWidth < textureHeight) {
            android.opengl.Matrix.orthoM(textureOrthogonalMatrix, 0, -textureAspectRatio, textureAspectRatio, -1f, 1f, -1f, 1f);
        } else {
            android.opengl.Matrix.orthoM(textureOrthogonalMatrix, 0, -1f, 1f, -textureAspectRatio, textureAspectRatio, -1f, 1f);
        }
        android.opengl.Matrix.multiplyMM(mvpMatrix, 0, textureOrthogonalMatrix, 0, viewportOrthogonalMatrix, 0);
    }

}
