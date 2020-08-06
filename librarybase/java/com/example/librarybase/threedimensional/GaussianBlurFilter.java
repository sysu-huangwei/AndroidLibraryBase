package com.example.librarybase.threedimensional;

import android.opengl.GLES20;
import java.nio.FloatBuffer;

/**
 * User: rayyyhuang
 * Date: 2020/7/27
 * Description: 高斯模糊算法
 */
public class GaussianBlurFilter {

    // 顶点坐标
    protected final float[] imageVertices = {
            -1f, 1f, // top left
            1f, 1f,  // top right
            -1f, -1f, // bottom left
            1f, -1f, // bottom right
    };
    // 纹理坐标
    protected final float[] textureCoordinates = {
            0f, 1f, // bottom left
            1f, 1f, // bottom right
            0f, 0f, // top left
            1f, 0f, // top right
    };
    // float[]转FloatBuffer
    protected final FloatBuffer imageVerticesBuffer = BaseGLUtils
            .floatArrayToFloatBuffer(imageVertices);
    protected final FloatBuffer textureCoordinatesBuffer = BaseGLUtils
            .floatArrayToFloatBuffer(textureCoordinates);
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

    /**
     * 高斯模糊
     */
    public GaussianBlurFilter() {
        vertexShaderString = ""
                + "attribute vec4 position;\n"
                + "attribute vec4 inputTextureCoordinate;\n"
                + "const int GAUSSIAN_SAMPLES = 13;\n"
                + "varying vec2 textureCoordinate;\n"
                + "varying vec2 blurCoordinates[GAUSSIAN_SAMPLES];\n"
                + "uniform float texelWidthOffset;\n"
                + "uniform float texelHeightOffset;\n"
                + "void main()\n"
                + "{\n"
                + "    gl_Position = position;\n"
                + "    int multiplier = 0;\n"
                + "    vec2 blurStep;\n"
                + "    vec2 singleStepOffset = vec2(texelWidthOffset, texelHeightOffset);\n"
                + "    for (int i = 0; i < GAUSSIAN_SAMPLES; i++)\n"
                + "    {\n"
                + "        multiplier = (i - ((GAUSSIAN_SAMPLES - 1) / 2));\n"
                + "        blurStep = float(multiplier) * singleStepOffset;\n"
                + "        blurCoordinates[i] = inputTextureCoordinate.xy + blurStep;\n"
                + "    }\n"
                + "}\n";

        fragmentShaderString = ""
                + "precision highp float;\n"
                + "const int GAUSSIAN_SAMPLES = 13;\n"
                + "varying vec2 textureCoordinate;\n"
                + "varying vec2 blurCoordinates[GAUSSIAN_SAMPLES];\n"
                + "uniform sampler2D inputImageTexture;\n"
                + "void main()\n"
                + "{\n"
                + "    highp vec4 sum = vec4(0.0);\n"
                + "    sum += texture2D(inputImageTexture, blurCoordinates[0]) * 0.046118;\n"
                + "    sum += texture2D(inputImageTexture, blurCoordinates[1]) * 0.058552;\n"
                + "    sum += texture2D(inputImageTexture, blurCoordinates[2]) * 0.071181;\n"
                + "    sum += texture2D(inputImageTexture, blurCoordinates[3]) * 0.082860;\n"
                + "    sum += texture2D(inputImageTexture, blurCoordinates[4]) * 0.092356;\n"
                + "    sum += texture2D(inputImageTexture, blurCoordinates[5]) * 0.098568;\n"
                + "    sum += texture2D(inputImageTexture, blurCoordinates[6]) * 0.100731;\n"
                + "    sum += texture2D(inputImageTexture, blurCoordinates[7]) * 0.098568;\n"
                + "    sum += texture2D(inputImageTexture, blurCoordinates[8]) * 0.092356;\n"
                + "    sum += texture2D(inputImageTexture, blurCoordinates[9]) * 0.082860;\n"
                + "    sum += texture2D(inputImageTexture, blurCoordinates[10]) * 0.071181;\n"
                + "    sum += texture2D(inputImageTexture, blurCoordinates[11]) * 0.058552;\n"
                + "    sum += texture2D(inputImageTexture, blurCoordinates[12]) * 0.046118;\n"
                + "    gl_FragColor = sum;\n"
                + "}\n";
    }

    /**
     * 初始化，必须在GL线程
     *
     * @param width 宽
     * @param height 高
     */
    public void init(int width, int height) {
        program = BaseGLUtils.createProgram(vertexShaderString, fragmentShaderString);
        if (program > 0) {
            // 获取顶点坐标位置
            positionAttribute = GLES20.glGetAttribLocation(program, "position");
            // 获取纹理坐标位置
            textureCoordinateAttribute = GLES20
                    .glGetAttribLocation(program, "inputTextureCoordinate");
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

    /**
     * 释放资源，必须在GL线程
     */
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

    /**
     * 渲染绘制效果
     *
     * @param inputTextureID 输入的原图的纹理ID
     * @return 结果纹理ID
     */
    public int render(int inputTextureID) {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, outputFrameBufferID);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D, outputTextureID, 0);

        GLES20.glViewport(0, 0, width, height);

        // 清屏
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        // 使用着色器绘制程序
        GLES20.glUseProgram(program);

        // 传入纹理
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, inputTextureID);
        GLES20.glUniform1i(inputImageTextureUniform, 0);

        //传入其他参数
        GLES20.glUniform1f(texelWidthOffsetUniform, 0.002f);
        GLES20.glUniform1f(texelHeightOffsetUniform, 0.002f);

        // 传入顶点位置
        GLES20.glEnableVertexAttribArray(positionAttribute);
        GLES20.glVertexAttribPointer(positionAttribute, 2, GLES20.GL_FLOAT, false, 8,
                imageVerticesBuffer);

        // 传入纹理位置
        GLES20.glEnableVertexAttribArray(textureCoordinateAttribute);
        GLES20.glVertexAttribPointer(textureCoordinateAttribute, 2, GLES20.GL_FLOAT, false, 8,
                textureCoordinatesBuffer);

        // 绘制
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glDisableVertexAttribArray(positionAttribute);
        GLES20.glDisableVertexAttribArray(textureCoordinateAttribute);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_NONE);

        return outputTextureID;
    }
}
