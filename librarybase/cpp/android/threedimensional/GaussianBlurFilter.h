//
// Created by rayyy on 2020/8/5.
//

#ifndef SMARTKIT_GAUSSIANBLURFILTER_H
#define SMARTKIT_GAUSSIANBLURFILTER_H

#include <GLES2/gl2.h>

namespace threedimensional {

/**
 * Description: 高斯模糊算法
 */
class GaussianBlurFilter {
 public:

  /**
   * 初始化，必须在GL线程
   *
   * @param width 宽
   * @param height 高
   */
  void init(int width, int height);

  /**
   * 释放资源，必须在GL线程
   */
  void release();

  /**
   * 渲染绘制效果
   *
   * @param inputTextureID 输入的原图的纹理ID
   * @return 结果纹理ID
   */
  GLuint render(GLuint inputTextureID);

 private:

  GLuint program;
  GLint positionAttribute;
  GLint textureCoordinateAttribute;
  GLint inputImageTextureUniform;
  GLint texelWidthOffsetUniform;
  GLint texelHeightOffsetUniform;
  GLuint outputTextureID; // 用于离屏渲染内置的纹理
  GLuint outputFrameBufferID; // 用于离屏渲染内置的FBO，outputTextureID
  int width; // 用于离屏渲染内置的纹理的宽
  int height; // 用于离屏渲染内置的纹理的高
};

}

#endif //SMARTKIT_GAUSSIANBLURFILTER_H
