package com.example.librarybase.threedimensional;

import android.opengl.GLES20;
import android.util.Pair;
import com.example.librarybase.opengl.BaseGLUtils;
import java.nio.FloatBuffer;
import java.util.ArrayList;

/**
 * @author: rayyy
 * @date: 2020/7/23
 * @description: 画景深的类
 */
public class DepthFilter {

    private String vertexShaderString;
    private String fragmentShaderString;

    private int program = 0;

    private int positionAttribute = 0;
    private int textureCoordinateAttribute = 0;

    private int inputImageTextureUniform = 0;
    private int depthImageTextureProcessedUniform = 0;
    private int depthImageTextureOriginUniform = 0;
    private int[] materialImageTextureUniform = new int[5];
    private int[] materialDepthUniform = new int[5];
    private int xyOffsetUniform = 0;
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

    public DepthFilter() {
        vertexShaderString = ""
                + "attribute vec4 position;\n"
                + "attribute vec4 inputTextureCoordinate;\n"
                + "varying vec2 textureCoordinate;\n"
                + "const int MATERIAL_COUNT = 5;\n"
                + "varying vec2 materialCoordinateShift[MATERIAL_COUNT];\n"
                + "varying float materialDepth[MATERIAL_COUNT];\n"
                + "varying vec2 xyOffsetVarying;\n"
                + "varying float scaleVarying;\n"
                + "varying float focusVarying;\n"
                + "uniform vec2 xyOffset;\n"
                + "uniform float scale;\n"
                + "uniform float focus;\n"
                + "uniform float materialDepth0;\n"
                + "uniform float materialDepth1;\n"
                + "uniform float materialDepth2;\n"
                + "uniform float materialDepth3;\n"
                + "uniform float materialDepth4;\n"
                + "void main()\n"
                + "{\n"
                + "    gl_Position = position;\n"
                + "    textureCoordinate = inputTextureCoordinate.xy;\n"
                + "    //把素材图景深值映射到 -0.5 ~ 0.5 (把原点放到focus上)\n"
                + "    materialCoordinateShift[0] = textureCoordinate + xyOffset * (materialDepth0 - focus) * scale;\n"
                + "    materialCoordinateShift[1] = textureCoordinate + xyOffset * (materialDepth1 - focus) * scale;\n"
                + "    materialCoordinateShift[2] = textureCoordinate + xyOffset * (materialDepth2 - focus) * scale;\n"
                + "    materialCoordinateShift[3] = textureCoordinate + xyOffset * (materialDepth3 - focus) * scale;\n"
                + "    materialCoordinateShift[4] = textureCoordinate + xyOffset * (materialDepth4 - focus) * scale;\n"
                + "    materialDepth[0] = materialDepth0;\n"
                + "    materialDepth[1] = materialDepth1;\n"
                + "    materialDepth[2] = materialDepth2;\n"
                + "    materialDepth[3] = materialDepth3;\n"
                + "    materialDepth[4] = materialDepth4;\n"
                + "    xyOffsetVarying = xyOffset;\n"
                + "    scaleVarying = scale;\n"
                + "    focusVarying = focus;\n"
                + "}\n";

        fragmentShaderString = ""
                + "precision highp float;\n"
                + "varying vec2 textureCoordinate;\n"
                + "const int MATERIAL_COUNT = 5;\n"
                + "varying vec2 materialCoordinateShift[MATERIAL_COUNT];\n"
                + "varying float materialDepth[MATERIAL_COUNT];\n"
                + "varying vec2 xyOffsetVarying;\n"
                + "varying float scaleVarying;\n"
                + "varying float focusVarying;\n"
                + "uniform sampler2D inputImageTexture;\n"
                + "uniform sampler2D depthImageTextureProcessed;\n"
                + "uniform sampler2D depthImageTextureOrigin;\n"
                + "uniform sampler2D materialImageTexture0;\n"
                + "uniform sampler2D materialImageTexture1;\n"
                + "uniform sampler2D materialImageTexture2;\n"
                + "uniform sampler2D materialImageTexture3;\n"
                + "uniform sampler2D materialImageTexture4;\n"
                + "void main()\n"
                + "{\n"
                + "    //根据膨胀和模糊之后的景深图计算偏移后的坐标\n"
                + "    float depthProcessedAlpha = texture2D(depthImageTextureProcessed, textureCoordinate).r;\n"
                + "    float mapProcessed = depthProcessedAlpha - focusVarying;//把 0 ~ 1 映射到 -0.5 ~ 0.5 (把原点放到focus上) \n"
                + "    vec2 TexCoordinateShift = textureCoordinate + xyOffsetVarying * mapProcessed * scaleVarying;\n"
                + "\n"
                + "    //把原始的景深图也做一样的偏移效果处理，用于素材的遮盖\n"
                + "    vec4 depthOriginColorShift = texture2D(depthImageTextureOrigin, TexCoordinateShift);\n"
                + "    float depthOriginShiftAlpha = depthOriginColorShift.r;\n"
                + "\n"
                + "    //原图的颜色\n"
                + "    vec4 imageColor = texture2D(inputImageTexture, TexCoordinateShift);\n"
                + "    //当前最新的颜色\n"
                + "    vec4 currentColor = imageColor;\n"
                + "\n"
                + "    //计算素材图偏移之后的坐标，根据自身素材的深度进行偏移\n"
                + "    if (depthOriginShiftAlpha <= materialDepth[0]) {\n"
                + "        vec4 materialColorShift = texture2D(materialImageTexture0, materialCoordinateShift[0]);//得到偏移后的素材图\n"
                + "        currentColor = mix(currentColor, materialColorShift, materialColorShift.a);//素材和原图做融合\n"
                + "    }\n"
                + "    if (depthOriginShiftAlpha <= materialDepth[1]) {\n"
                + "        vec4 materialColorShift = texture2D(materialImageTexture1, materialCoordinateShift[1]);//得到偏移后的素材图\n"
                + "        currentColor = mix(currentColor, materialColorShift, materialColorShift.a);//素材和原图做融合\n"
                + "    }\n"
                + "    if (depthOriginShiftAlpha <= materialDepth[2]) {\n"
                + "        vec4 materialColorShift = texture2D(materialImageTexture2, materialCoordinateShift[2]);//得到偏移后的素材图\n"
                + "        currentColor = mix(currentColor, materialColorShift, materialColorShift.a);//素材和原图做融合\n"
                + "    }\n"
                + "    if (depthOriginShiftAlpha <= materialDepth[3]) {\n"
                + "        vec4 materialColorShift = texture2D(materialImageTexture3, materialCoordinateShift[3]);//得到偏移后的素材图\n"
                + "        currentColor = mix(currentColor, materialColorShift, materialColorShift.a);//素材和原图做融合\n"
                + "    }\n"
                + "    if (depthOriginShiftAlpha <= materialDepth[4]) {\n"
                + "        vec4 materialColorShift = texture2D(materialImageTexture4, materialCoordinateShift[4]);//得到偏移后的素材图\n"
                + "        currentColor = mix(currentColor, materialColorShift, materialColorShift.a);//素材和原图做融合\n"
                + "    }\n"
                + "\n"
                + "    gl_FragColor = currentColor;\n"
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
            depthImageTextureProcessedUniform = GLES20.glGetUniformLocation(program, "depthImageTextureProcessed");
            depthImageTextureOriginUniform = GLES20.glGetUniformLocation(program, "depthImageTextureOrigin");
            materialImageTextureUniform[0] = GLES20.glGetUniformLocation(program, "materialImageTexture0");
            materialDepthUniform[0] = GLES20.glGetUniformLocation(program, "materialDepth0");
            materialImageTextureUniform[1] = GLES20.glGetUniformLocation(program, "materialImageTexture1");
            materialDepthUniform[1] = GLES20.glGetUniformLocation(program, "materialDepth1");
            materialImageTextureUniform[2] = GLES20.glGetUniformLocation(program, "materialImageTexture2");
            materialDepthUniform[2] = GLES20.glGetUniformLocation(program, "materialDepth2");
            materialImageTextureUniform[3] = GLES20.glGetUniformLocation(program, "materialImageTexture3");
            materialDepthUniform[3] = GLES20.glGetUniformLocation(program, "materialDepth3");
            materialImageTextureUniform[4] = GLES20.glGetUniformLocation(program, "materialImageTexture4");
            materialDepthUniform[4] = GLES20.glGetUniformLocation(program, "materialDepth4");
            xyOffsetUniform = GLES20.glGetUniformLocation(program, "xyOffset");
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

    public int render(int inputTextureID, int depthTextureOriginID, int depthTextureProcessedID, ArrayList<Pair<Integer, Float>> materialTextureAndDepth, float xOffset, float yOffset, float depthScale) {
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
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, depthTextureProcessedID);
        GLES20.glUniform1i(depthImageTextureProcessedUniform, 1);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, depthTextureOriginID);
        GLES20.glUniform1i(depthImageTextureOriginUniform, 2);

        for (int i = 0; i < materialTextureAndDepth.size(); i++) {
            int materialTextureID = materialTextureAndDepth.get(i).first;
            float materialDepth = materialTextureAndDepth.get(i).second;
            GLES20.glUniform1f(materialDepthUniform[i], materialDepth);
            if (materialDepth >= 0.0f) {
                GLES20.glActiveTexture(GLES20.GL_TEXTURE3 + i);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, materialTextureID);
                GLES20.glUniform1i(materialImageTextureUniform[i], 3 + i);
            }
        }

        //传入其他参数
        GLES20.glUniform1f(focusUniform, 0.5f);
        GLES20.glUniform1f(scaleUniform, depthScale);
        GLES20.glUniform2f(xyOffsetUniform, xOffset, yOffset);

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
