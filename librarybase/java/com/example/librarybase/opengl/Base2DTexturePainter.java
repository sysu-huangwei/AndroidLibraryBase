package com.example.librarybase.opengl;


import android.opengl.GLES20;

/**
 * User: HW
 * Date: 2020/4/29
 * Description: OpenGL 2D纹理绘制类
 */
public class Base2DTexturePainter extends BaseTexturePainter {

    public Base2DTexturePainter() {
        vertexShaderString = ""
                + "attribute vec4 position;\n"
                + "attribute vec4 inputTextureCoordinate;\n"
                + "uniform mat4 mvpMatrix;\n"
                + "varying vec2 textureCoordinate;\n"
                + "void main()\n"
                + "{\n"
                + "    gl_Position = mvpMatrix * position;\n"
                + "    textureCoordinate = inputTextureCoordinate.xy;\n"
                + "}\n";

        fragmentShaderString = ""
                + "precision highp float;\n"
                + "varying vec2 textureCoordinate;\n"
                + "uniform sampler2D inputImageTexture;\n"
                + "void main()\n"
                + "{\n"
                + "    gl_FragColor = texture2D(inputImageTexture, textureCoordinate);\n"
                + "}\n";
    }

    @Override
    public void init() {
        mProgram = BaseGLUtils.createProgram(vertexShaderString, fragmentShaderString);
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

    @Override
    public void render(int inputTexture, int inputTextureWidth, int inputTextureHeight, int outputWidth, int outputHeight, @BaseOrientationEnum int orientation) {

        GLES20.glViewport(0, 0, outputWidth, outputHeight);

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
}
