//
// Created by rayyy on 2020/8/5.
//

#ifndef SMARTKIT_THREEDIMENSIONALFILTER_H
#define SMARTKIT_THREEDIMENSIONALFILTER_H

#include <vector>
#include <GLES2/gl2.h>
#include "DilateFilter.h"
#include "GaussianBlurFilter.h"
#include "DepthFilter.h"
#include "PerspectiveFilter.h"
#include "ThreeDimensionalOneFrameData.h"

namespace threedimensional {

/**
 * Description: 3D效果组合滤镜
 */
class ThreeDimensionalFilter {
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
   * 清除之前的绘制信息，当输入新的[尺寸不变的]图片和景深的时候需要调用此接口
   * 如果新的图片尺寸变了，需要release之后重新init
   */
  void reset();

  /**
   * 绘制3D效果
   *
   * @param inputTextureID 原始图片的纹理ID
   * @param originDepthTextureID 原始景深图的纹理ID
   * @param materialTextureAndDepth 素材的纹理ID及其对应的景深值
   * @param threeDimensionalOneFrameData 3D效果其他参数
   * @return 结果纹理ID
   */
  GLuint render(GLuint inputTextureID, int originDepthTextureID,
                std::vector<std::pair<int, float> > materialTextureAndDepth,
                ThreeDimensionalOneFrameData threeDimensionalOneFrameData);

 private:

  /* 膨胀 */
  DilateFilter dilateFilter;

  /* 高斯模糊 */
  GaussianBlurFilter gaussianBlurFilter;

  /* 景深（加上穿插素材） */
  DepthFilter depthFilter;

  /* 透视 */
  PerspectiveFilter perspectiveFilter;

  /* 膨胀和模糊之后的景深图纹理ID */
  GLuint processedDepthTextureID;

  /* 是否需要重置 */
  bool isNeedReset = false;
};

}

#endif //SMARTKIT_THREEDIMENSIONALFILTER_H
