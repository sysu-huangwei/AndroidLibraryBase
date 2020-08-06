package com.example.librarybase.threedimensional;

import android.graphics.Bitmap;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Log;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * User: HW
 * Date: 2020/4/29
 * Description: OpenGLES2.0 常用方法
 */
public class BaseGLUtils {

    private static final String TAG = "BaseGLUtils";

    /**
     * 根据指定的着色器类型和脚本生成着色器。
     *
     * @param shaderType 着色器类型
     * @param source 着色器脚本
     * @return 着色器
     */
    public static int loadShader(@GLShaderType int shaderType, @NonNull String source) {
        int shader = GLES20.glCreateShader(shaderType);
        GLES20.glShaderSource(shader, source);
        GLES20.glCompileShader(shader);
        int[] compiled = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            Log.e(TAG,
                    "Error: Could not compile shader, shaderType = " + shaderType + " shader = ");
            Log.e(TAG, " " + GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader);
            shader = 0;
        }
        return shader;
    }

    /**
     * 根据指定的着色器脚本创建着色器程序。
     *
     * @param vertexSource 顶点着色器脚本
     * @param fragmentSource 片段着色器脚本
     * @return 着色器程序
     */
    public static int createProgram(@NonNull String vertexSource, @NonNull String fragmentSource) {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        if (vertexShader == 0) {
            return 0;
        }
        int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        if (pixelShader == 0) {
            GLES20.glDeleteShader(vertexShader);
            return 0;
        }

        int program = GLES20.glCreateProgram();
        if (program == 0) {
            Log.e(TAG, "Error: Could not create program");
        }

        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, pixelShader);
        GLES20.glLinkProgram(program);
        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] != GLES20.GL_TRUE) {
            Log.e(TAG, "Error: Could not link program: ");
            Log.e(TAG, GLES20.glGetProgramInfoLog(program));
            GLES20.glDeleteProgram(program);
            program = 0;
        }

        GLES20.glDeleteShader(vertexShader);
        GLES20.glDeleteShader(pixelShader);
        return program;
    }

    /**
     * 根据指定的参数创建纹理。
     *
     * @param target 纹理类型
     * @param wrapMode 纹理环绕方式
     * @param filterMode 纹理过滤方式
     * @return 要创建的纹理
     */
    public static int createTextures(@GLTarget int target, @GLWrapMode int wrapMode,
            @GLFilterMode int filterMode) {
        int[] textures = new int[1];
        GLES20.glGenTextures(textures.length, textures, 0);
        GLES20.glBindTexture(target, textures[0]);
        GLES20.glTexParameteri(target, GLES20.GL_TEXTURE_WRAP_S, wrapMode);
        GLES20.glTexParameteri(target, GLES20.GL_TEXTURE_WRAP_T, wrapMode);
        GLES20.glTexParameteri(target, GLES20.GL_TEXTURE_MIN_FILTER, filterMode);
        GLES20.glTexParameteri(target, GLES20.GL_TEXTURE_MAG_FILTER, filterMode);
        return textures[0];
    }

    /**
     * 创建 2D 纹理
     */
    public static int createTextures2D() {
        return createTextures(GLES20.GL_TEXTURE_2D, GLES20.GL_CLAMP_TO_EDGE, GLES20.GL_LINEAR);
    }

    public static int createFBO(int texture, int width, int height) {
        int[] frameBuffers = new int[1];
        GLES20.glGenFramebuffers(1, frameBuffers, 0);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0,
                GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffers[0]);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D, texture, 0);

        int fboStatus = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
        if (fboStatus != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            Log.e(TAG, "initFBO failed, status: " + fboStatus);
        }

        return frameBuffers[0];
    }

    /**
     * float[] 转 FloatBuffer
     *
     * @param floats float数组
     * @return FloatBuffer
     */
    public static FloatBuffer floatArrayToFloatBuffer(float[] floats) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(floats.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();
        floatBuffer.put(floats);
        floatBuffer.position(0);
        return floatBuffer;
    }

    /**
     * 读取纹理内容转成ByteBuffer
     *
     * @param texture 纹理对象
     * @param width 纹理 宽
     * @param height 纹理 高
     * @return 返回ByteBuffer
     */
    public static ByteBuffer readTextureToByteBuffer(int texture, int width, int height) {
        if (width <= 0 || height <= 0) {
            return null;
        }

        GLES20.glFinish();
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(width * height * 4);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.rewind();
        byteBuffer.position(0);

        int[] offscreenFBO = new int[1];
        GLES20.glGenFramebuffers(1, offscreenFBO, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, offscreenFBO[0]);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D, texture, 0);
        GLES20.glReadPixels(0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE,
                byteBuffer);
        GLES20.glDeleteFramebuffers(1, offscreenFBO, 0);

        return byteBuffer;
    }

    /**
     * 读取纹理内容转成Bitmap
     *
     * @param texture 纹理对象
     * @param width 纹理 宽
     * @param height 纹理 高
     * @return 返回Bitmap图像
     */
    public static Bitmap readTextureToBitmap(int texture, int width, int height) {
        ByteBuffer byteBuffer = readTextureToByteBuffer(texture, width, height);
        if (byteBuffer != null) {
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.copyPixelsFromBuffer(byteBuffer);
            byteBuffer.clear();
            return bitmap;
        }
        return null;
    }

    /**
     * 着色器类型。
     */
    @IntDef({GLES20.GL_VERTEX_SHADER, GLES20.GL_FRAGMENT_SHADER})
    private @interface GLShaderType {

    }

    /**
     * 纹理类型
     */
    @IntDef({GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_CUBE_MAP, GLES11Ext.GL_TEXTURE_EXTERNAL_OES})
    private @interface GLTarget {

    }

    /**
     * 纹理环绕方式
     */
    @IntDef({GLES20.GL_CLAMP_TO_EDGE, GLES20.GL_MIRRORED_REPEAT, GLES20.GL_REPEAT})
    private @interface GLWrapMode {

    }

    /**
     * 纹理过滤方式
     */
    @IntDef({GLES20.GL_NEAREST, GLES20.GL_LINEAR, GLES20.GL_NEAREST_MIPMAP_NEAREST,
            GLES20.GL_NEAREST_MIPMAP_LINEAR, GLES20.GL_LINEAR_MIPMAP_LINEAR,
            GLES20.GL_LINEAR_MIPMAP_NEAREST})
    private @interface GLFilterMode {

    }

    /**
     * 像素格式
     */
    @IntDef({GLES20.GL_ALPHA, GLES20.GL_LUMINANCE, GLES20.GL_LUMINANCE_ALPHA, GLES20.GL_RGB,
            GLES20.GL_RGBA})
    private @interface GLFormat {

    }
}
