package com.example.librarybase.opengl;

import android.opengl.GLES20;
import java.nio.FloatBuffer;

/**
 * @author: rayyy
 * @date: 2020/7/23
 * @description: 画3D的类
 */
public class Base3DPainter  {

    private String vertexShaderString;
    private String fragmentShaderString;

    private int program = 0;

    private int positionAttribute = 0;
    private int textureCoordinateAttribute = 0;

    private int inputImageTextureUniform = 0;
    private int depthImageTextureUniform = 0;
    private int materialImageTextureUniform = 0;
    private int scaleUniform = 0;
    private int focusUniform = 0;
    
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

    public Base3DPainter() {
        vertexShaderString = ""
                + "attribute vec4 position;\n"
                + "attribute vec4 inputTextureCoordinate;\n"
                + "varying vec2 textureCoordinate;\n"
                + "void main()\n"
                + "{\n"
                + "    gl_Position = position;\n"
                + "    textureCoordinate = inputTextureCoordinate.xy;\n"
                + "}\n";

        fragmentShaderString = ""
                + "precision highp float;\n"
                + "varying vec2 textureCoordinate;\n"
                + "uniform sampler2D inputImageTexture;\n"
                + "uniform sampler2D depthImageTexture;\n"
                + "uniform sampler2D materialImageTexture;\n"
                + "uniform vec2 uv_offset;\n"
                + "uniform float scale;\n"
                + "uniform float focus;\n"
                + "void main()\n"
                + "{\n"
                + "    float map = texture2D(depthImageTexture, textureCoordinate).r;\n"
                + "    map = map * (- 1.0) + focus;\n"
                + "    vec2 TexCoordinateShift = textureCoordinate + uv_offset * map * scale;\n"
                + "    gl_FragColor = texture2D(inputImageTexture, TexCoordinateShift);\n"
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
            depthImageTextureUniform = GLES20.glGetUniformLocation(program, "depthImageTexture");
            materialImageTextureUniform = GLES20.glGetUniformLocation(program, "materialImageTexture");
            scaleUniform = GLES20.glGetUniformLocation(program, "scale");
            focusUniform = GLES20.glGetUniformLocation(program, "focus");
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

    public int render(int inputTextureID, int depthTextureID, int materialTextureID) {
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

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, depthTextureID);
        GLES20.glUniform1i(depthImageTextureUniform, 1);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, materialTextureID);
        GLES20.glUniform1i(materialImageTextureUniform, 2);

        //传入其他参数
        GLES20.glUniform1f(focusUniform, 0.5f);
        GLES20.glUniform1f(scaleUniform, 0.05f);

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
