//
// Created by rayyy on 2020/8/5.
//

#ifndef SMARTKIT_THREEDIMENSIONALONEFRAMEDATA_H
#define SMARTKIT_THREEDIMENSIONALONEFRAMEDATA_H

namespace threedimensional {

class ThreeDimensionalOneFrameData {
 public:

  /* 景深效果程度 */
  float depthScale = 0.0f;

  /* 透视效果程度 */
  float perspectiveScale = 0.0f;

  /* x方向的景深偏移值 */
  float xShift = 0.0f;

  /* y方向的景深偏移值 */
  float yShift = 0.0f;

  /* MVP矩阵，4*4 */
  float mvpMatrix[16] = {1, 0, 0, 0,
                         0, 1, 0, 0,
                         0, 0, 1, 0,
                         0, 0, 0, 1};
};

}

#endif //SMARTKIT_THREEDIMENSIONALONEFRAMEDATA_H
