package com.example.librarybase.number;

import android.opengl.GLES20;
import com.example.librarybase.opengl.BaseGLUtils;
import java.nio.FloatBuffer;
import java.util.ArrayList;

/**
 * User: rayyyhuang
 * Date: 2020/8/14
 * Description: 数字滚动滤镜
 */
public class NumberRollFilter {

    private String vertexShaderString;
    private String fragmentShaderString;

    private int program = 0;

    private int positionAttribute = 0;
    private int textureCoordinateAttribute = 0;

    private int inputImageTextureUniform = 0;
    private int boxCountUniform = 0;

    private int numberTextureUniform = 0;
    private int box1Uniform = 0;
    private int currentY1Uniform = 0;
    private int box2Uniform = 0;
    private int currentY2Uniform = 0;
    private int box3Uniform = 0;
    private int currentY3Uniform = 0;

    private int outputTextureID = 0; // 用于离屏渲染内置的纹理
    private int outputFrameBufferID = 0; // 用于离屏渲染内置的FBO，outputTextureID
    private int width = 0; // 用于离屏渲染内置的纹理的宽
    private int height = 0; // 用于离屏渲染内置的纹理的高

    /* 是否需要重置动画 */
    private boolean isNeedReset = true;
    /* 动画开始的时间 */
    private long startTime = 0L;
    /* 当前已经经过的时间 */
    private long currentTime = 0L;

    /* 数字的配置信息 */
    private ArrayList<NumberItem> numberItemArrayList = new ArrayList<>();

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
    protected final FloatBuffer imageVerticesBuffer = BaseGLUtils.floatArrayToFloatBuffer(imageVertices);
    protected final FloatBuffer textureCoordinatesBuffer = BaseGLUtils.floatArrayToFloatBuffer(textureCoordinates);

    /**
     * Description: 数字滚动滤镜
     */
    public NumberRollFilter() {
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
                + "#define ABC 1\n"
                + "precision highp float;\n"
                + "varying vec2 textureCoordinate;\n"
                + "uniform sampler2D inputImageTexture;\n"
                + "uniform sampler2D numberTexture;\n"
                + "uniform int boxCount;\n"
                + "uniform vec4 box1;\n"
                + "uniform float currentY1;\n"
                + "uniform vec4 box2;\n"
                + "uniform float currentY2;\n"
                + "uniform vec4 box3;\n"
                + "uniform float currentY3;\n"
                + "void main()\n"
                + "{\n"
                + "    if (boxCount > 0 && textureCoordinate.x > box1.x && textureCoordinate.x < box1.z && textureCoordinate.y > box1.y && textureCoordinate.y < box1.w) {\n"
                + "        float width = box1.z - box1.x;\n"
                + "        float height = box1.w - box1.y;\n"
                + "        float numberTextureCoordinateX = (textureCoordinate.x - box1.x) / width;\n"
                + "        float numberTextureCoordinateY = 0.1 * (textureCoordinate.y - box1.y) / height + currentY1;\n"
                + "        if (numberTextureCoordinateY > 1.0) {\n"
                + "            numberTextureCoordinateY -= 1.0;\n"
                + "        }\n"
                + "        vec2 numberTextureCoordinate = vec2(numberTextureCoordinateX, numberTextureCoordinateY);\n"
                + "        gl_FragColor = texture2D(numberTexture, numberTextureCoordinate);\n"
                + "    } else if (boxCount > 1 && textureCoordinate.x > box2.x && textureCoordinate.x < box2.z && textureCoordinate.y > box2.y && textureCoordinate.y < box2.w) {\n"
                + "        float width = box2.z - box2.x;\n"
                + "        float height = box2.w - box2.y;\n"
                + "        float numberTextureCoordinateX = (textureCoordinate.x - box2.x) / width;\n"
                + "        float numberTextureCoordinateY = 0.1 * (textureCoordinate.y - box2.y) / height + currentY2;\n"
                + "        if (numberTextureCoordinateY > 1.0) {\n"
                + "            numberTextureCoordinateY -= 1.0;\n"
                + "        }\n"
                + "        vec2 numberTextureCoordinate = vec2(numberTextureCoordinateX, numberTextureCoordinateY);\n"
                + "        gl_FragColor = texture2D(numberTexture, numberTextureCoordinate);\n"
                + "    } else if (boxCount > 2 && textureCoordinate.x > box3.x && textureCoordinate.x < box3.z && textureCoordinate.y > box3.y && textureCoordinate.y < box3.w) {\n"
                + "        float width = box3.z - box3.x;\n"
                + "        float height = box3.w - box3.y;\n"
                + "        float numberTextureCoordinateX = (textureCoordinate.x - box3.x) / width;\n"
                + "        float numberTextureCoordinateY = 0.1 * (textureCoordinate.y - box3.y) / height + currentY3;\n"
                + "        if (numberTextureCoordinateY > 1.0) {\n"
                + "            numberTextureCoordinateY -= 1.0;\n"
                + "        }\n"
                + "        vec2 numberTextureCoordinate = vec2(numberTextureCoordinateX, numberTextureCoordinateY);\n"
                + "        gl_FragColor = texture2D(numberTexture, numberTextureCoordinate);\n"
                + "    } else {\n"
                + "        gl_FragColor = texture2D(inputImageTexture, textureCoordinate);\n"
                + "    }\n"
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
            textureCoordinateAttribute = GLES20.glGetAttribLocation(program, "inputTextureCoordinate");
            // 获取纹理采样器位置
            inputImageTextureUniform = GLES20.glGetUniformLocation(program, "inputImageTexture");
            boxCountUniform = GLES20.glGetUniformLocation(program, "boxCount");
            numberTextureUniform = GLES20.glGetUniformLocation(program, "numberTexture");
            box1Uniform = GLES20.glGetUniformLocation(program, "box1");
            currentY1Uniform = GLES20.glGetUniformLocation(program, "currentY1");
            box2Uniform = GLES20.glGetUniformLocation(program, "box2");
            currentY2Uniform = GLES20.glGetUniformLocation(program, "currentY2");
            box3Uniform = GLES20.glGetUniformLocation(program, "box3");
            currentY3Uniform = GLES20.glGetUniformLocation(program, "currentY3");
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
     * 设置需要显示的数字信息
     *
     * @param numberItemArrayList 数字信息
     */
    public void setNumberItemArrayList(ArrayList<NumberItem> numberItemArrayList) {
        this.numberItemArrayList = numberItemArrayList;
    }

    /**
     * 设置需要显示的目标数字
     *
     * @param targetNumber 目标数字
     */
    public void setTargetNumber(int targetNumber) {
        if (numberItemArrayList != null) {
            for (int i = numberItemArrayList.size() - 1; i >= 0; --i) {
                numberItemArrayList.get(i).targetNumber = targetNumber % 10;
                targetNumber /= 10;
            }
        }
    }

    /**
     * 重置动画，重置后会重新播放滚动动画
     */
    public void reset() {
        isNeedReset = true;
        currentTime = 0;
    }

    /**
     * 渲染
     *
     * @param inputTextureID 输入的原图纹理ID
     * @param numberTextureID 数字图片的纹理ID
     * @param time 当前的时间戳
     * @return 结果纹理
     */
    public int render(int inputTextureID, int numberTextureID, long time) {

        if (isNeedReset) {
            startTime = time;
            isNeedReset = false;
        } else {
            currentTime = time - startTime;
        }

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
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, numberTextureID);
        GLES20.glUniform1i(numberTextureUniform, 1);

        GLES20.glUniform1i(boxCountUniform, numberItemArrayList.size());

        if (numberItemArrayList.size() > 0) {
            GLES20.glUniform4f(box1Uniform, numberItemArrayList.get(0).left, numberItemArrayList.get(0).top, numberItemArrayList.get(0).right, numberItemArrayList.get(0).bottom);
            numberItemArrayList.get(0).calculateCurrentPosition(currentTime / 1000.0f);
            GLES20.glUniform1f(currentY1Uniform, numberItemArrayList.get(0).currentPosition);
            if (numberItemArrayList.size() > 1) {
                GLES20.glUniform4f(box2Uniform, numberItemArrayList.get(1).left, numberItemArrayList.get(1).top, numberItemArrayList.get(1).right, numberItemArrayList.get(1).bottom);
                numberItemArrayList.get(1).calculateCurrentPosition(currentTime / 1000.0f);
                GLES20.glUniform1f(currentY2Uniform, numberItemArrayList.get(1).currentPosition);
                if (numberItemArrayList.size() > 2) {
                    GLES20.glUniform4f(box3Uniform, numberItemArrayList.get(2).left, numberItemArrayList.get(2).top, numberItemArrayList.get(2).right, numberItemArrayList.get(2).bottom);
                    numberItemArrayList.get(2).calculateCurrentPosition(currentTime / 1000.0f);
                    GLES20.glUniform1f(currentY3Uniform, numberItemArrayList.get(2).currentPosition);
                }
            }
        }

        // 传入顶点位置
        GLES20.glEnableVertexAttribArray(positionAttribute);
        GLES20.glVertexAttribPointer(positionAttribute, 2, GLES20.GL_FLOAT, false, 8, imageVerticesBuffer);

        // 传入纹理位置
        GLES20.glEnableVertexAttribArray(textureCoordinateAttribute);
        GLES20.glVertexAttribPointer(textureCoordinateAttribute, 2, GLES20.GL_FLOAT, false, 8, textureCoordinatesBuffer);

        // 绘制
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glDisableVertexAttribArray(positionAttribute);
        GLES20.glDisableVertexAttribArray(textureCoordinateAttribute);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_NONE);

        return outputTextureID;
    }
}
