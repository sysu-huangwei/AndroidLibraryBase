package com.example.librarybase.opengl;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import java.nio.FloatBuffer;


////////////////////////////////////////////////////////////////////////////////////////
//								orientation方向示例
//     1        2       3      4         5            6           7          8
//
//    888888  888888      88  88      8888888888  88                  88  8888888888
//    88          88      88  88      88  88      88  88          88  88      88  88
//    8888      8888    8888  8888    88          8888888888  8888888888          88
//    88          88      88  88
//    88          88  888888  888888
////////////////////////////////////////////////////////////////////////////////////////


/**
 * User: HW
 * Date: 2020/4/29
 * Description: 2D纹理绘制基础类
 */
public class Base2DTexturePainter {

    // 顶点坐标
    private final float mImageVertices1[] = {
            -1f, -1f, // bottom left
            1f, -1f, // bottom right
            -1f, 1f, // top left
            1f, 1f,  // top right
    };
    private final float mImageVertices2[] = {
            1f, -1f, // bottom right
            -1f, -1f, // bottom left
            1f, 1f,  // top right
            -1f, 1f, // top left
    };
    private final float mImageVertices3[] = {
            1f, 1f,  // top right
            -1f, 1f, // top left
            1f, -1f, // bottom right
            -1f, -1f, // bottom left
    };
    private final float mImageVertices4[] = {
            -1f, 1f, // top left
            1f, 1f,  // top right
            -1f, -1f, // bottom left
            1f, -1f, // bottom right
    };
    private final float mImageVertices5[] = {
            1f, 1f,  // top right
            1f, -1f, // bottom right
            -1f, 1f, // top left
            -1f, -1f, // bottom left
    };
    private final float mImageVertices6[] = {
            -1f, 1f, // top left
            -1f, -1f, // bottom left
            1f, 1f,  // top right
            1f, -1f, // bottom right
    };
    private final float mImageVertices7[] = {
            -1f, -1f, // bottom left
            -1f, 1f, // top left
            1f, 1f,  // top right
            1f, -1f, // bottom right
    };
    private final float mImageVertices8[] = {
            1f, -1f, // bottom right
            1f, 1f,  // top right
            -1f, -1f, // bottom left
            -1f, 1f, // top left
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

    // float[]转FloatBuffer
    private final FloatBuffer mImageVertices1Buffer = BaseGLUtils.floatArrayToFloatBuffer(mImageVertices1);
    private final FloatBuffer mImageVertices2Buffer = BaseGLUtils.floatArrayToFloatBuffer(mImageVertices2);
    private final FloatBuffer mImageVertices3Buffer = BaseGLUtils.floatArrayToFloatBuffer(mImageVertices3);
    private final FloatBuffer mImageVertices4Buffer = BaseGLUtils.floatArrayToFloatBuffer(mImageVertices4);
    private final FloatBuffer mImageVertices5Buffer = BaseGLUtils.floatArrayToFloatBuffer(mImageVertices5);
    private final FloatBuffer mImageVertices6Buffer = BaseGLUtils.floatArrayToFloatBuffer(mImageVertices6);
    private final FloatBuffer mImageVertices7Buffer = BaseGLUtils.floatArrayToFloatBuffer(mImageVertices7);
    private final FloatBuffer mImageVertices8Buffer = BaseGLUtils.floatArrayToFloatBuffer(mImageVertices8);
    private final FloatBuffer[] mImageVerticesBuffers = {mImageVertices1Buffer, mImageVertices2Buffer, mImageVertices3Buffer, mImageVertices4Buffer, mImageVertices5Buffer, mImageVertices6Buffer, mImageVertices7Buffer, mImageVertices8Buffer};
    private final FloatBuffer mTextureCoordinatesBuffer = BaseGLUtils.floatArrayToFloatBuffer(mTextureCoordinates);

    private final int mCoordinatesCountPerVertex = 2; // 每个顶点的坐标数
    private final int mVertexCount = mImageVertices1.length / mCoordinatesCountPerVertex; // 顶点数量
    private final int vertexStride = mCoordinatesCountPerVertex * 4; // 每个坐标字节长度

    private int mProgram; // 2D纹理绘制着色器程序
    private int mPositionAttribute; // 顶点坐标位置
    private int mTextureCoordinateAttribute; // 纹理坐标位置
    private int mMvpMatrixUniform; // 变换矩阵位置
    private int mTextureUniform; // 纹理采样器位置

    private boolean mIsOES = false; // 是否是OES纹理

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

    private final String baseOES2DFragmentShaderString = ""
            + "#extension GL_OES_EGL_image_external : require\n"
            + "precision highp float;\n"
            + "varying vec2 textureCoordinate;\n"
            + "uniform samplerExternalOES inputImageTexture;\n"
            + "void main()\n"
            + "{\n"
            + "    gl_FragColor = texture2D(inputImageTexture, textureCoordinate);\n"
            + "}\n";


    /**
     * 初始化，必须在GL线程，默认画2D纹理
     */
    public void init() {
        init(false);
    }

    /**
     * 初始化，必须在GL线程
     *
     * @param isOES 是否画OES纹理，否则画2D纹理
     */
    public void init(boolean isOES) {
        mIsOES = isOES;
        // 创建着色器程序
        if (isOES) {
            mProgram = BaseGLUtils.createProgram(base2DVertexShaderString, baseOES2DFragmentShaderString);
        } else {
            mProgram = BaseGLUtils.createProgram(base2DVertexShaderString, base2DFragmentShaderString);
        }
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
     * 绘制到屏幕
     * @param inputTexture 输入纹理
     * @param inputTextureWidth 输入纹理的宽
     * @param inputTextureHeight 输入纹理的高
     * @param outputWidth 输出的纹理宽
     * @param outputHeight 输出的纹理高
     */
    public void render(int inputTexture, int inputTextureWidth, int inputTextureHeight, int outputWidth, int outputHeight) {
        render(inputTexture, inputTextureWidth, inputTextureHeight, outputWidth, outputHeight, 1);
    }

    /**
     * 绘制到屏幕
     * @param inputTexture 输入纹理
     * @param inputTextureWidth 输入纹理的宽
     * @param inputTextureHeight 输入纹理的高
     * @param outputWidth 输出的纹理宽
     * @param outputHeight 输出的纹理高
     * @param orientation 方向
     */
    public void render(int inputTexture, int inputTextureWidth, int inputTextureHeight, int outputWidth, int outputHeight, int orientation) {

        GLES20.glViewport(0, 0, outputWidth, outputHeight);

        // 清屏
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glClearColor(0.0f,0.0f,0.0f,1.0f);

        // 使用着色器绘制程序
        GLES20.glUseProgram(mProgram);

        // 传入纹理
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        if (mIsOES) {
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, inputTexture);
        } else {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, inputTexture);
        }
        GLES20.glUniform1i(mTextureUniform, 0);

        // 传入顶点位置
        GLES20.glEnableVertexAttribArray(mPositionAttribute);
        GLES20.glVertexAttribPointer(mPositionAttribute, mCoordinatesCountPerVertex, GLES20.GL_FLOAT, false, vertexStride, mImageVerticesBuffers[orientation - 1]);

        // 传入纹理位置
        GLES20.glEnableVertexAttribArray(mTextureCoordinateAttribute);
        GLES20.glVertexAttribPointer(mTextureCoordinateAttribute, mCoordinatesCountPerVertex, GLES20.GL_FLOAT, false, vertexStride, mTextureCoordinatesBuffer);

        // 计算和传入变换矩阵
        getMvpMatrix(mMvpMatrix, (float) outputWidth / (float) outputHeight, inputTextureWidth, inputTextureHeight);
        GLES20.glUniformMatrix4fv(mMvpMatrixUniform, 1, false, mMvpMatrix, 0);

        // 绘制
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, mVertexCount);

        GLES20.glDisableVertexAttribArray(mPositionAttribute);
        GLES20.glDisableVertexAttribArray(mTextureCoordinateAttribute);
    }

    /**
     * 绘制到FBO
     * @param inputTexture 输入纹理
     * @param inputTextureWidth 输入纹理的宽
     * @param inputTextureHeight 输入纹理的高
     * @param outputTexture 输出的纹理
     * @param outputFrameBuffer 输出的FBO
     * @param outputWidth 输出的纹理宽
     * @param outputHeight 输出的纹理高
     */
    public void renderToFBO(int inputTexture, int inputTextureWidth, int inputTextureHeight, int outputTexture, int outputFrameBuffer, int outputWidth, int outputHeight) {
        renderToFBO(inputTexture, inputTextureWidth, inputTextureHeight, outputTexture, outputFrameBuffer, outputWidth, outputHeight, 1);
    }

    /**
     * 绘制到FBO
     * @param inputTexture 输入纹理
     * @param inputTextureWidth 输入纹理的宽
     * @param inputTextureHeight 输入纹理的高
     * @param outputTexture 输出的纹理
     * @param outputFrameBuffer 输出的FBO
     * @param outputWidth 输出的纹理宽
     * @param outputHeight 输出的纹理高
     * @param orientation 方向
     */
    public void renderToFBO(int inputTexture, int inputTextureWidth, int inputTextureHeight, int outputTexture, int outputFrameBuffer, int outputWidth, int outputHeight, int orientation) {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, outputFrameBuffer);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, outputTexture, 0);

        render(inputTexture, inputTextureWidth, inputTextureHeight, outputWidth, outputHeight, orientation);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_NONE);
    }

    /**
     * 把纹理尺寸映射到viewport区域内，使画出来的纹理不被拉伸变形
     * @param mvpMatrix out 最终的mvp矩阵结果
     * @param viewportWidthHeightRatio in viewport的宽高比
     * @param textureWidth in 纹理宽
     * @param textureHeight in 纹理高
     */
    private static void getMvpMatrix(float[] mvpMatrix, float viewportWidthHeightRatio, int textureWidth, int textureHeight) {
        float textureWidthHeightRatio = (float) textureWidth / (float) textureHeight;
        float ratio = viewportWidthHeightRatio / textureWidthHeightRatio;
        if (ratio > 1f) {
            android.opengl.Matrix.orthoM(mvpMatrix, 0, -ratio, ratio, -1f, 1f, -1f, 1f);
        } else {
            android.opengl.Matrix.orthoM(mvpMatrix, 0, -1f, 1f, -1.0f / ratio, 1.0f / ratio, -1f, 1f);
        }
    }

}
