//
// Created by rayyy on 2020/8/5.
//

#ifndef SMARTKIT_DEPTHFILTER_H
#define SMARTKIT_DEPTHFILTER_H

#include <vector>
#include <GLES2/gl2.h>

namespace threedimensional {

/**
 * Description: 画景深的类
 */
class DepthFilter {
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
   * @param depthTextureOriginID 原始景深图纹理ID
   * @param depthTextureProcessedID 经过膨胀和模糊处理之后的景深图纹理ID
   * @param xOffset x方向坐标偏移量
   * @param yOffset y方向坐标偏移量
   * @param depthScale 效果程度
   * @return 结果纹理ID
   */
  GLuint render(GLuint inputTextureID, GLuint depthTextureOriginID,
             GLuint depthTextureProcessedID,
             std::vector<std::pair<int, float> > materialTextureAndDepth,
             float xOffset, float yOffset, float depthScale);

 private:

  GLuint program;
  GLint positionAttribute;
  GLint textureCoordinateAttribute;
  GLint inputImageTextureUniform;
  GLint depthImageTextureProcessedUniform;
  GLint depthImageTextureOriginUniform;
  GLint materialImageTextureUniform[5];
  GLint materialDepthUniform[5];
  GLint xyOffsetUniform;
  GLint scaleUniform;
  GLint focusUniform;
  GLuint outputTextureID; // 用于离屏渲染内置的纹理
  GLuint outputFrameBufferID; // 用于离屏渲染内置的FBO，outputTextureID
  int width; // 用于离屏渲染内置的纹理的宽
  int height; // 用于离屏渲染内置的纹理的高
};

}

#endif //SMARTKIT_DEPTHFILTER_H
