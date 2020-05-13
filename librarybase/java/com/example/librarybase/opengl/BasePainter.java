package com.example.librarybase.opengl;


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

import android.opengl.GLES20;

import java.nio.FloatBuffer;

/**
 * User: HW
 * Date: 2020/5/13
 * Description: OpenGL 绘制基类
 */
public abstract class BasePainter {

    // 顶点坐标
    protected final float[] mImageVertices1 = {
            -1f, -1f, // bottom left
            1f, -1f, // bottom right
            -1f, 1f, // top left
            1f, 1f,  // top right
    };
    protected final float[] mImageVertices2 = {
            1f, -1f, // bottom right
            -1f, -1f, // bottom left
            1f, 1f,  // top right
            -1f, 1f, // top left
    };
    protected final float[] mImageVertices3 = {
            1f, 1f,  // top right
            -1f, 1f, // top left
            1f, -1f, // bottom right
            -1f, -1f, // bottom left
    };
    protected final float[] mImageVertices4 = {
            -1f, 1f, // top left
            1f, 1f,  // top right
            -1f, -1f, // bottom left
            1f, -1f, // bottom right
    };
    protected final float[] mImageVertices5 = {
            1f, 1f,  // top right
            1f, -1f, // bottom right
            -1f, 1f, // top left
            -1f, -1f, // bottom left
    };
    protected final float[] mImageVertices6 = {
            -1f, 1f, // top left
            -1f, -1f, // bottom left
            1f, 1f,  // top right
            1f, -1f, // bottom right
    };
    protected final float[] mImageVertices7 = {
            -1f, -1f, // bottom left
            -1f, 1f, // top left
            1f, -1f, // bottom right
            1f, 1f,  // top right
    };
    protected final float[] mImageVertices8 = {
            1f, -1f, // bottom right
            1f, 1f,  // top right
            -1f, -1f, // bottom left
            -1f, 1f, // top left
    };

    // 纹理坐标
    protected final float[] mTextureCoordinates = {
            0f, 1f, // bottom left
            1f, 1f, // bottom right
            0f, 0f, // top left
            1f, 0f, // top right
    };

    // MVP矩阵
    protected final float[] mMvpMatrix = {
            1f, 0f, 0f, 0f,
            0f, 1f, 0f, 0f,
            0f, 0f, 1f, 0f,
            0f, 0f, 0f, 1f,
    };

    // float[]转FloatBuffer
    protected final FloatBuffer mImageVertices1Buffer = BaseGLUtils.floatArrayToFloatBuffer(mImageVertices1);
    protected final FloatBuffer mImageVertices2Buffer = BaseGLUtils.floatArrayToFloatBuffer(mImageVertices2);
    protected final FloatBuffer mImageVertices3Buffer = BaseGLUtils.floatArrayToFloatBuffer(mImageVertices3);
    protected final FloatBuffer mImageVertices4Buffer = BaseGLUtils.floatArrayToFloatBuffer(mImageVertices4);
    protected final FloatBuffer mImageVertices5Buffer = BaseGLUtils.floatArrayToFloatBuffer(mImageVertices5);
    protected final FloatBuffer mImageVertices6Buffer = BaseGLUtils.floatArrayToFloatBuffer(mImageVertices6);
    protected final FloatBuffer mImageVertices7Buffer = BaseGLUtils.floatArrayToFloatBuffer(mImageVertices7);
    protected final FloatBuffer mImageVertices8Buffer = BaseGLUtils.floatArrayToFloatBuffer(mImageVertices8);
    protected final FloatBuffer[] mImageVerticesBuffers = {mImageVertices1Buffer, mImageVertices2Buffer, mImageVertices3Buffer, mImageVertices4Buffer, mImageVertices5Buffer, mImageVertices6Buffer, mImageVertices7Buffer, mImageVertices8Buffer};
    protected final FloatBuffer mTextureCoordinatesBuffer = BaseGLUtils.floatArrayToFloatBuffer(mTextureCoordinates);

    protected final int mCoordinatesCountPerVertex = 2; // 每个顶点的坐标数
    protected final int mVertexCount = mImageVertices1.length / mCoordinatesCountPerVertex; // 顶点数量
    protected final int vertexStride = mCoordinatesCountPerVertex * 4; // 每个坐标字节长度

    protected int mProgram; // 2D纹理绘制着色器程序
    protected int mPositionAttribute; // 顶点坐标位置
    protected int mMvpMatrixUniform; // 变换矩阵位置

    protected String vertexShaderString = null; // 顶点着色器脚本
    protected String fragmentShaderString = null; // 片段着色器脚本

    /**
     * 初始化GL资源，必须在GL线程
     */
    public abstract void init();

    /**
     * 使用完必须释放，否则会有GL内存泄露，必须在GL线程
     */
    public void release() {
        GLES20.glDeleteProgram(mProgram);
        mProgram = 0;
    }

    /**
     * 绘制到屏幕
     *
     * @param inputTexture       输入纹理
     * @param inputTextureWidth  输入纹理的宽
     * @param inputTextureHeight 输入纹理的高
     * @param outputWidth        输出的纹理宽
     * @param outputHeight       输出的纹理高
     */
    public void render(int inputTexture, int inputTextureWidth, int inputTextureHeight, int outputWidth, int outputHeight) {
        render(inputTexture, inputTextureWidth, inputTextureHeight, outputWidth, outputHeight, 1);
    }

    /**
     * 绘制到屏幕
     *
     * @param inputTexture       输入纹理
     * @param inputTextureWidth  输入纹理的宽
     * @param inputTextureHeight 输入纹理的高
     * @param outputWidth        输出的纹理宽
     * @param outputHeight       输出的纹理高
     * @param orientation        方向
     */
    public abstract void render(int inputTexture, int inputTextureWidth, int inputTextureHeight, int outputWidth, int outputHeight, int orientation);

    /**
     * 绘制到FBO
     *
     * @param inputTexture       输入纹理
     * @param inputTextureWidth  输入纹理的宽
     * @param inputTextureHeight 输入纹理的高
     * @param outputTexture      输出的纹理
     * @param outputFrameBuffer  输出的FBO
     * @param outputWidth        输出的纹理宽
     * @param outputHeight       输出的纹理高
     */
    public void renderToFBO(int inputTexture, int inputTextureWidth, int inputTextureHeight, int outputTexture, int outputFrameBuffer, int outputWidth, int outputHeight) {
        renderToFBO(inputTexture, inputTextureWidth, inputTextureHeight, outputTexture, outputFrameBuffer, outputWidth, outputHeight, 1);
    }

    /**
     * 绘制到FBO
     *
     * @param inputTexture       输入纹理
     * @param inputTextureWidth  输入纹理的宽
     * @param inputTextureHeight 输入纹理的高
     * @param outputTexture      输出的纹理
     * @param outputFrameBuffer  输出的FBO
     * @param outputWidth        输出的纹理宽
     * @param outputHeight       输出的纹理高
     * @param orientation        方向
     */
    public void renderToFBO(int inputTexture, int inputTextureWidth, int inputTextureHeight, int outputTexture, int outputFrameBuffer, int outputWidth, int outputHeight, int orientation) {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, outputFrameBuffer);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, outputTexture, 0);

        render(inputTexture, inputTextureWidth, inputTextureHeight, outputWidth, outputHeight, orientation);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_NONE);
    }

    /**
     * 把纹理尺寸映射到viewport区域内，使画出来的纹理不被拉伸变形
     *
     * @param mvpMatrix                out 最终的mvp矩阵结果
     * @param viewportWidthHeightRatio in viewport的宽高比
     * @param textureWidth             in 纹理宽
     * @param textureHeight            in 纹理高
     */
    protected static void getMvpMatrix(float[] mvpMatrix, float viewportWidthHeightRatio, int textureWidth, int textureHeight) {
        float textureWidthHeightRatio = (float) textureWidth / (float) textureHeight;
        float ratio = viewportWidthHeightRatio / textureWidthHeightRatio;
        if (ratio > 1f) {
            android.opengl.Matrix.orthoM(mvpMatrix, 0, -ratio, ratio, -1f, 1f, -1f, 1f);
        } else {
            android.opengl.Matrix.orthoM(mvpMatrix, 0, -1f, 1f, -1.0f / ratio, 1.0f / ratio, -1f, 1f);
        }
    }

}
