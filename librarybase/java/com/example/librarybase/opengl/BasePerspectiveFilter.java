package com.example.librarybase.opengl;

import android.opengl.GLES20;
import android.opengl.Matrix;
import java.nio.FloatBuffer;

/**
 * User: rayyyhuang
 * Date: 2020/7/28
 * Description: 透视效果
 */
public class BasePerspectiveFilter {

    private String vertexShaderString;
    private String fragmentShaderString;

    private int program = 0;

    private int positionAttribute = 0;
    private int textureCoordinateAttribute = 0;

    private int inputImageTextureUniform = 0;
    private int mvpMatrixUniform; // 变换矩阵位置

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

    // MVP矩阵
    protected final float[] mvpMatrix = {
            1f, 0f, 0f, 0f,
            0f, 1f, 0f, 0f,
            0f, 0f, 1f, 0f,
            0f, 0f, 0f, 1f,
    };

    // V矩阵
    protected final float[] viewMatrix = {
            1f, 0f, 0f, 0f,
            0f, 1f, 0f, 0f,
            0f, 0f, 1f, 0f,
            0f, 0f, 0f, 1f,
    };

    // P矩阵
    protected final float[] projMatrix = {
            1f, 0f, 0f, 0f,
            0f, 1f, 0f, 0f,
            0f, 0f, 1f, 0f,
            0f, 0f, 0f, 1f,
    };

    // float[]转FloatBuffer
    protected final FloatBuffer mImageVerticesBuffer = BaseGLUtils.floatArrayToFloatBuffer(mImageVertices);
    protected final FloatBuffer mTextureCoordinatesBuffer = BaseGLUtils.floatArrayToFloatBuffer(mTextureCoordinates);

    public BasePerspectiveFilter() {
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
            // 获取变换矩阵位置
            mvpMatrixUniform = GLES20.glGetUniformLocation(program, "mvpMatrix");
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

    public int render(int inputTexture,
            float eyeX, float eyeY, float eyeZ,
            float centerX, float centerY, float centerZ,
            float upX, float upY, float upZ) {
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
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, inputTexture);
        GLES20.glUniform1i(inputImageTextureUniform, 0);

        // 传入顶点位置
        GLES20.glEnableVertexAttribArray(positionAttribute);
        GLES20.glVertexAttribPointer(positionAttribute, 2, GLES20.GL_FLOAT, false, 8, mImageVerticesBuffer);

        // 传入纹理位置
        GLES20.glEnableVertexAttribArray(textureCoordinateAttribute);
        GLES20.glVertexAttribPointer(textureCoordinateAttribute, 2, GLES20.GL_FLOAT, false, 8, mTextureCoordinatesBuffer);

        // 计算和传入变换矩阵
        //相对于屏幕坐标系将摄像头固定在（0，0，-1）方向，看向屏幕正中点（0，0，0），以屏幕向上为正方向（0，1，0）
        Matrix.setLookAtM(viewMatrix, 0, eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ);
        //创建一个透视视景体
        //注意：near,far都必须大于0，且二者不能相等，left、right、bottom、top 是near的left和right坐标
        //但是由于相机视点为（0，0，-1）而屏幕应该显示的是正方向（0，0，x）x大于0的方向的画面，所有left对应的屏幕坐标应该为负，right对应的坐标系为正，呈左右翻转的效果。
        //将near设置为1，far设置为2。由此可以得到视图可见区域为由视点（0，0，-1）出发的距离为1-2的区域内的物体。（注意不是由0，0，0点出发距离1-2的区域）。


        double degreeX = Math.atan(Math.abs(eyeX) / Math.abs(eyeZ));
        double distanceX = Math.sqrt(eyeX * eyeX + eyeZ * eyeZ);
        double nearX = distanceX - Math.sin(degreeX);
        double farX = distanceX + Math.sin(degreeX);
        double left;
        double right;
        if (eyeX < 0) {
            left = Math.cos(degreeX);
            right = -1.0 * nearX / farX * Math.cos(degreeX);
        } else {
            left = nearX / farX * Math.cos(degreeX);
            right = -1.0 * Math.cos(degreeX);
        }

        double degreeY = Math.atan(Math.abs(eyeY) / Math.abs(eyeZ));
        double distanceY = Math.sqrt(eyeY * eyeY + eyeZ * eyeZ);
        double nearY = distanceY - Math.sin(degreeY);
        double farY = distanceY + Math.sin(degreeY);
        double top;
        double bottom;
        if (eyeY < 0) {
            top = nearY / farY * Math.cos(degreeY);
            bottom = -1.0 * Math.cos(degreeY);
        } else {
            top = Math.cos(degreeY);
            bottom = -1.0 * nearY / farY * Math.cos(degreeY);
        }


        double distanceToZero = Math.sqrt(eyeX * eyeX + eyeY * eyeY + eyeZ * eyeZ);
        double distanceInXY = Math.sqrt(2.0);
        double distanceToNearest = Math.sqrt((Math.abs(eyeX) - 1.0) * (Math.abs(eyeX) - 1.0) + (Math.abs(eyeY) - 1.0) * (Math.abs(eyeY) - 1.0) + eyeZ * eyeZ);
        double cos = (distanceToZero * distanceToZero + distanceToNearest * distanceToNearest - distanceInXY * distanceInXY) / (2.0 * distanceToZero * distanceToNearest);

        double near = distanceToNearest * cos;//Math.min(nearX, nearY);
        double far = Math.max(farX, farY) * 2;


        float cut = 0.95f;//(float)Math.sqrt(eyeX * eyeX + eyeY * eyeY);

//        Matrix.frustumM(projMatrix, 0, 1.0f, -1.0f, -1, 1, 1, 3);
        Matrix.frustumM(projMatrix, 0, (float)left * cut, (float)right * cut, (float)bottom * cut, (float)top * cut, (float)near, (float)far);
        // Calculate the projection and view transformation
        Matrix.multiplyMM(mvpMatrix, 0, projMatrix, 0, viewMatrix, 0);
        GLES20.glUniformMatrix4fv(mvpMatrixUniform, 1, false, mvpMatrix, 0);

        // 绘制
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glDisableVertexAttribArray(positionAttribute);
        GLES20.glDisableVertexAttribArray(textureCoordinateAttribute);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_NONE);

        return outputTextureID;
    }
}
