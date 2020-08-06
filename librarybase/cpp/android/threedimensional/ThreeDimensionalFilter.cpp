//
// Created by rayyy on 2020/8/5.
//

#include "ThreeDimensionalFilter.h"

namespace threedimensional {

void ThreeDimensionalFilter::init(int width, int height) {
  dilateFilter.init(width, height);
  gaussianBlurFilter.init(width, height);
  depthFilter.init(width, height);
  perspectiveFilter.init(width, height);
}

void ThreeDimensionalFilter::release() {
  dilateFilter.release();
  gaussianBlurFilter.release();
  depthFilter.release();
  perspectiveFilter.release();
  if (processedDepthTextureID != 0) {
    glDeleteTextures(1, &processedDepthTextureID);
    processedDepthTextureID = 0;
  }
}

void ThreeDimensionalFilter::reset() {
  isNeedReset = true;
}

GLuint ThreeDimensionalFilter::render
    (GLuint inputTextureID, int originDepthTextureID,
     std::vector<std::pair<int, float> > materialTextureAndDepth,
     ThreeDimensionalOneFrameData threeDimensionalOneFrameData) {
  if (isNeedReset && processedDepthTextureID != 0) {
    glDeleteTextures(1, &processedDepthTextureID);
    processedDepthTextureID = 0;
    isNeedReset = false;
  }
  if (processedDepthTextureID == 0) {
    int dilateDepthTextureID = dilateFilter.render(originDepthTextureID);
    processedDepthTextureID = gaussianBlurFilter.render(dilateDepthTextureID);
  }
  int imageDepthTextureID = depthFilter
      .render(inputTextureID, originDepthTextureID, processedDepthTextureID,
              materialTextureAndDepth, threeDimensionalOneFrameData.xShift,
              threeDimensionalOneFrameData.yShift,
              threeDimensionalOneFrameData.depthScale);
  int perspectiveTextureID = perspectiveFilter
      .render(imageDepthTextureID, threeDimensionalOneFrameData.mvpMatrix);
  return perspectiveTextureID;
}

}