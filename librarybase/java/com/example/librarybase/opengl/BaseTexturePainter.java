package com.example.librarybase.opengl;


/**
 * User: HW
 * Date: 2020/5/13
 * Description: OpenGL 纹理绘制基类
 */
public abstract class BaseTexturePainter extends BasePainter {

    protected int mTextureCoordinateAttribute; // 纹理坐标位置
    protected int mTextureUniform; // 纹理采样器位置

}
