//
// Created by rayyy on 2020/8/5.
//

#ifndef SMARTKIT_PERSPECTIVEFILTER_H
#define SMARTKIT_PERSPECTIVEFILTER_H

#include <GLES2/gl2.h>

namespace threedimensional {

/**
 * Description: 透视效果
 */
class PerspectiveFilter {
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
   * @param mvpMatrix 透视矩阵
   * @return 结果纹理ID
   */
  GLuint render(GLuint inputTextureID, GLfloat mvpMatrix[16]);

 private:

  GLuint program;
  GLint positionAttribute;
  GLint textureCoordinateAttribute;
  GLint inputImageTextureUniform;
  GLint mvpMatrixUniform; // 变换矩阵位置
  GLuint outputTextureID; // 用于离屏渲染内置的纹理
  GLuint outputFrameBufferID; // 用于离屏渲染内置的FBO，outputTextureID
  int width; // 用于离屏渲染内置的纹理的宽
  int height; // 用于离屏渲染内置的纹理的高
};

}

#endif //SMARTKIT_PERSPECTIVEFILTER_H
