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

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.FloatBuffer;

/**
 * User: HW
 * Date: 2020/5/13
 * Description: OpenGL 绘制基类
 */
public abstract class BasePainter {

    /**
     * 相机预览比例
     */
    public final static int BASE_ORIENTATION_1 = 1;
    public final static int BASE_ORIENTATION_2 = 2;
    public final static int BASE_ORIENTATION_3 = 3;
    public final static int BASE_ORIENTATION_4 = 4;
    public final static int BASE_ORIENTATION_5 = 5;
    public final static int BASE_ORIENTATION_6 = 6;
    public final static int BASE_ORIENTATION_7 = 7;
    public final static int BASE_ORIENTATION_8 = 8;
    @IntDef({BASE_ORIENTATION_1, BASE_ORIENTATION_2, BASE_ORIENTATION_3, BASE_ORIENTATION_4, BASE_ORIENTATION_5, BASE_ORIENTATION_6, BASE_ORIENTATION_7, BASE_ORIENTATION_8})
    @Retention(RetentionPolicy.SOURCE)
    public @interface BaseOrientationEnum {
    }

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

    private int mOutputTextureID = 0; // 用于离屏渲染内置的纹理
    private int mOutputFrameBufferID = 0; // 用于离屏渲染内置的FBO，和mOutputTextureID绑定
    private int mOutputTextureWidth = 0; // 用于离屏渲染内置的纹理的宽
    private int mOutputTextureHeight = 0; // 用于离屏渲染内置的纹理的高

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
        GLES20.glDeleteTextures(1, new int[]{mOutputTextureID}, 0);
        mOutputTextureID = 0;
        GLES20.glDeleteFramebuffers(1, new int[]{mOutputFrameBufferID}, 0);
        mOutputFrameBufferID = 0;
        mOutputTextureWidth = 0;
        mOutputTextureHeight = 0;
    }

    /**
     * 绘制到屏幕（默认的FBO）
     *
     * @param inputTextureID     输入纹理
     * @param inputTextureWidth  输入纹理的宽
     * @param inputTextureHeight 输入纹理的高
     * @param outputWidth        输出的纹理宽
     * @param outputHeight       输出的纹理高
     */
    public void render(int inputTextureID, int inputTextureWidth, int inputTextureHeight, int outputWidth, int outputHeight) {
        render(inputTextureID, inputTextureWidth, inputTextureHeight, outputWidth, outputHeight, BASE_ORIENTATION_1);
    }

    /**
     * 绘制到屏幕（默认的FBO）
     *
     * @param inputTextureID     输入纹理
     * @param inputTextureWidth  输入纹理的宽
     * @param inputTextureHeight 输入纹理的高
     * @param outputWidth        输出的纹理宽
     * @param outputHeight       输出的纹理高
     * @param orientation        方向
     */
    public abstract void render(int inputTextureID, int inputTextureWidth, int inputTextureHeight, int outputWidth, int outputHeight, @BaseOrientationEnum int orientation);

    /**
     * 绘制到指定的FBO
     *
     * @param inputTextureID      输入纹理
     * @param inputTextureWidth   输入纹理的宽
     * @param inputTextureHeight  输入纹理的高
     * @param outputTextureID     输出的纹理
     * @param outputFrameBufferID 输出的FBO
     * @param outputWidth         输出的纹理宽
     * @param outputHeight        输出的纹理高
     * @return 结果纹理
     */
    public int renderToOuterFBO(int inputTextureID, int inputTextureWidth, int inputTextureHeight, int outputTextureID, int outputFrameBufferID, int outputWidth, int outputHeight) {
        return renderToOuterFBO(inputTextureID, inputTextureWidth, inputTextureHeight, outputTextureID, outputFrameBufferID, outputWidth, outputHeight, BASE_ORIENTATION_1);
    }

    /**
     * 绘制到指定的FBO
     *
     * @param inputTextureID      输入纹理
     * @param inputTextureWidth   输入纹理的宽
     * @param inputTextureHeight  输入纹理的高
     * @param outputTextureID     输出的纹理
     * @param outputFrameBufferID 输出的FBO
     * @param outputWidth         输出的纹理宽
     * @param outputHeight        输出的纹理高
     * @param orientation         方向
     * @return 结果纹理
     */
    public int renderToOuterFBO(int inputTextureID, int inputTextureWidth, int inputTextureHeight, int outputTextureID, int outputFrameBufferID, int outputWidth, int outputHeight, @BaseOrientationEnum int orientation) {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, outputFrameBufferID);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, outputTextureID, 0);

        render(inputTextureID, inputTextureWidth, inputTextureHeight, outputWidth, outputHeight, orientation);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_NONE);

        return outputTextureID;
    }

    /**
     * 绘制到内置的FBO
     *
     * @param inputTextureID     输入纹理
     * @param inputTextureWidth  输入纹理的宽
     * @param inputTextureHeight 输入纹理的高
     * @return 结果纹理
     */
    public int renderToInnerFBO(int inputTextureID, int inputTextureWidth, int inputTextureHeight) {
        return renderToInnerFBO(inputTextureID, inputTextureWidth, inputTextureHeight, BASE_ORIENTATION_1);
    }

    /**
     * 绘制到内置的FBO
     *
     * @param inputTextureID     输入纹理
     * @param inputTextureWidth  输入纹理的宽
     * @param inputTextureHeight 输入纹理的高
     * @param orientation        方向
     * @return 结果纹理
     */
    public int renderToInnerFBO(int inputTextureID, int inputTextureWidth, int inputTextureHeight, @BaseOrientationEnum int orientation) {
        if (mOutputTextureID <= 0) {
            mOutputTextureID = BaseGLUtils.createTextures2D();
        }

        if (inputTextureWidth != mOutputTextureWidth || inputTextureHeight != mOutputTextureHeight) {
            mOutputTextureWidth = inputTextureWidth;
            mOutputTextureHeight = inputTextureHeight;
            GLES20.glDeleteFramebuffers(1, new int[]{mOutputFrameBufferID}, 0);
            mOutputFrameBufferID = BaseGLUtils.createFBO(mOutputTextureID, mOutputTextureWidth, mOutputTextureHeight);
        }

        return renderToOuterFBO(inputTextureID, inputTextureWidth, inputTextureHeight, mOutputTextureID, mOutputFrameBufferID, mOutputTextureWidth, mOutputTextureHeight, orientation);
    }

    /**
     * 获取结果纹理的宽
     *
     * @return 结果纹理的宽
     */
    public int getOutputTextureWidth() {
        return mOutputTextureWidth;
    }

    /**
     * 获取结果纹理的高
     * @return 结果纹理的高
     */
    public int getOutputTextureHeight() {
        return mOutputTextureHeight;
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
