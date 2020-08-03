package com.example.librarybase.threedimensional;

import android.opengl.GLES20;
import com.example.librarybase.opengl.BaseGLUtils;
import java.nio.FloatBuffer;

/**
 * User: rayyyhuang
 * Date: 2020/7/27
 * Description: 膨胀滤算法
 */
public class DilateFilter {

    private String vertexShaderString;
    private String fragmentShaderString;

    private int program = 0;

    private int positionAttribute = 0;
    private int textureCoordinateAttribute = 0;

    private int inputImageTextureUniform = 0;
    private int texelWidthOffsetUniform = 0;
    private int texelHeightOffsetUniform = 0;

    private int outputTextureID = 0; // 用于离屏渲染内置的纹理
    private int outputFrameBufferID = 0; // 用于离屏渲染内置的FBO，outputTextureID
    private int width = 0; // 用于离屏渲染内置的纹理的宽
    private int height = 0; // 用于离屏渲染内置的纹理的高

    // 顶点坐标
    protected final float[] mImageVertices = {
            -1f, 1f, // top left
            1f, 1f,  // top right
            -1f, -1f, // bottom left
            1f, -1f, // bottom right
    };

    // 纹理坐标
    protected final float[] mTextureCoordinates = {
            0f, 1f, // bottom left
            1f, 1f, // bottom right
            0f, 0f, // top left
            1f, 0f, // top right
    };

    // float[]转FloatBuffer
    protected final FloatBuffer mImageVerticesBuffer = BaseGLUtils.floatArrayToFloatBuffer(mImageVertices);
    protected final FloatBuffer mTextureCoordinatesBuffer = BaseGLUtils.floatArrayToFloatBuffer(mTextureCoordinates);

    public DilateFilter() {
        vertexShaderString = ""
                + "attribute vec4 position;\n"
                + "attribute vec4 inputTextureCoordinate;\n"
                + "varying vec2 textureCoordinate;\n"
                + "varying vec2 textureCoordinateOneStepPositive;\n"
                + "varying vec2 textureCoordinateOneStepNegative;\n"
                + "uniform float texelWidthOffset;\n"
                + "uniform float texelHeightOffset;\n"
                + "void main()\n"
                + "{\n"
                + "    vec2 offset = vec2(texelWidthOffset, texelHeightOffset);\n"
                + "    gl_Position = position;\n"
                + "    textureCoordinate = inputTextureCoordinate.xy;\n"
                + "    textureCoordinateOneStepPositive = inputTextureCoordinate.xy + offset;\n"
                + "    textureCoordinateOneStepNegative = inputTextureCoordinate.xy - offset;\n"
                + "}\n";

        fragmentShaderString = ""
                + "precision highp float;\n"
                + "varying vec2 textureCoordinate;\n"
                + "varying vec2 textureCoordinateOneStepPositive;\n"
                + "varying vec2 textureCoordinateOneStepNegative;\n"
                + "uniform sampler2D inputImageTexture;\n"
                + "void main()\n"
                + "{\n"
                + "    vec4 centerColor = texture2D(inputImageTexture, textureCoordinate);\n"
                + "    vec4 oneStepPositiveColor = texture2D(inputImageTexture, textureCoordinateOneStepPositive);\n"
                + "    vec4 oneStepNegativeColor = texture2D(inputImageTexture, textureCoordinateOneStepNegative);\n"
                + "    float maxR = max(centerColor.r, oneStepPositiveColor.r);\n"
                + "    maxR = max(maxR, oneStepNegativeColor.r);\n"
                + "    float maxG = max(centerColor.g, oneStepPositiveColor.g);\n"
                + "    maxG = max(maxG, oneStepNegativeColor.g);\n"
                + "    float maxB = max(centerColor.b, oneStepPositiveColor.b);\n"
                + "    maxB = max(maxB, oneStepNegativeColor.b);\n"
                + "    gl_FragColor = vec4(maxR, maxG, maxB, centerColor.a);\n"
                + "}\n";
    }

    public void init(int width, int height) {
        program = BaseGLUtils.createProgram(vertexShaderString, fragmentShaderString);
        if (program > 0) {
            // 获取顶点坐标位置
            positionAttribute = GLES20.glGetAttribLocation(program, "position");
            // 获取纹理坐标位置
            textureCoordinateAttribute = GLES20.glGetAttribLocation(program, "inputTextureCoordinate");
            // 获取纹理采样器位置
            inputImageTextureUniform = GLES20.glGetUniformLocation(program, "inputImageTexture");
            texelWidthOffsetUniform = GLES20.glGetUniformLocation(program, "texelWidthOffset");
            texelHeightOffsetUniform = GLES20.glGetUniformLocation(program, "texelHeightOffset");
        }

        this.width = width;
        this.height = height;
        outputTextureID = BaseGLUtils.createTextures2D();
        outputFrameBufferID = BaseGLUtils.createFBO(outputTextureID, width, height);
    }

    public void release() {
        GLES20.glDeleteProgram(program);
        program = 0;
        GLES20.glDeleteTextures(1, new int[]{outputTextureID}, 0);
        outputTextureID = 0;
        GLES20.glDeleteFramebuffers(1, new int[]{outputFrameBufferID}, 0);
        outputFrameBufferID = 0;
        width = 0;
        height = 0;
    }

    public int render(int inputTextureID) {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, outputFrameBufferID);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, outputTextureID, 0);

        GLES20.glViewport(0, 0, width, height);

        // 清屏
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glClearColor(0.0f,0.0f,0.0f,1.0f);

        // 使用着色器绘制程序
        GLES20.glUseProgram(program);

        // 传入纹理
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, inputTextureID);
        GLES20.glUniform1i(inputImageTextureUniform, 0);

        //传入其他参数
        GLES20.glUniform1f(texelWidthOffsetUniform, 0.02f);
        GLES20.glUniform1f(texelHeightOffsetUniform, 0.02f);

        // 传入顶点位置
        GLES20.glEnableVertexAttribArray(positionAttribute);
        GLES20.glVertexAttribPointer(positionAttribute, 2, GLES20.GL_FLOAT, false, 8, mImageVerticesBuffer);

        // 传入纹理位置
        GLES20.glEnableVertexAttribArray(textureCoordinateAttribute);
        GLES20.glVertexAttribPointer(textureCoordinateAttribute, 2, GLES20.GL_FLOAT, false, 8, mTextureCoordinatesBuffer);

        // 绘制
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glDisableVertexAttribArray(positionAttribute);
        GLES20.glDisableVertexAttribArray(textureCoordinateAttribute);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_NONE);

        return outputTextureID;
    }
}
