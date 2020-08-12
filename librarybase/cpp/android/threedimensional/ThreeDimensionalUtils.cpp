//
// Created by rayyy on 2020/8/5.
//

#include "ThreeDimensionalUtils.h"
#include <cmath>

namespace threedimensional {

/* 默认的运动幅度的最大值（振幅） */
const static float DEFAULT_THRESHOLD = 0.5f;

/* 最大的步长 */
const static int MAX_STEP = 300;

std::vector<ThreeDimensionalOneFrameData>
ThreeDimensionalUtils::calculateThreeDimensionalData(int directionType,
                                                     float depthScale,
                                                     float perspectiveScale,
                                                     float speed,
                                                     int fps) {
  std::vector<ThreeDimensionalOneFrameData> result;
  std::pair<std::vector<float>, std::vector<float> > shift = getShift(directionType, DEFAULT_THRESHOLD,
                                                                      getStepBySpeedAndFPS(speed, fps));
  for (int i = 0; i < shift.first.size(); i++) {
    ThreeDimensionalOneFrameData threeDimensionalOneFrameData;
    threeDimensionalOneFrameData.xShift = shift.first.at(i);
    threeDimensionalOneFrameData.yShift = shift.second.at(i);
    getMvpMatrix(threeDimensionalOneFrameData.mvpMatrix,
                 threeDimensionalOneFrameData.xShift, threeDimensionalOneFrameData.yShift,
                 DEFAULT_THRESHOLD, perspectiveScale);
    threeDimensionalOneFrameData.depthScale = depthScale;
    threeDimensionalOneFrameData.perspectiveScale = perspectiveScale;
    result.push_back(threeDimensionalOneFrameData);
  }

  return result;
}

int ThreeDimensionalUtils::getStepBySpeedAndFPS(float speed, int fps) {
  if (speed > 0) {
    int step =  (int) ((float) fps / speed);
    return step > MAX_STEP ? MAX_STEP : step;
  }
  return MAX_STEP;
}

std::pair<std::vector<float>, std::vector<float> >
ThreeDimensionalUtils::getShift(int direction, float threshold, int step) {
  int quarterStep = step / 4;
  std::vector<float> xShift;
  std::vector<float> yShift;
  if (direction == DIRECTION_TYPE_LEFT_RIGHT || direction == DIRECTION_TYPE_UP_DOWN) {
    std::vector<float> positive;
    std::vector<float> negative;
    for (float f = 0.0f; f < threshold; f += (threshold / quarterStep)) {
      positive.push_back(f);
      negative.push_back(-f);
    }
    std::vector<float> animalFunction;
    animalFunction.insert(animalFunction.end(), positive.begin(), positive.end());
    animalFunction.insert(animalFunction.end(), positive.rbegin(), positive.rend());
    animalFunction.insert(animalFunction.end(), negative.begin(), negative.end());
    animalFunction.insert(animalFunction.end(), negative.rbegin(), negative.rend());


    if (direction == DIRECTION_TYPE_LEFT_RIGHT) {
      xShift.insert(xShift.end(), animalFunction.begin(), animalFunction.end());
      yShift = std::vector<float>(animalFunction.size(), 0);
    } else {
      xShift = std::vector<float>(animalFunction.size(), 0);
      yShift.insert(yShift.end(), animalFunction.begin(), animalFunction.end());
    }

  } else if (direction == DIRECTION_TYPE_CIRCLE) {
    float fullDeg = (float) M_PI * 2.0f;
    for (float f = 0.0f; f < fullDeg; f += fullDeg / quarterStep / 4.0f) {
      xShift.push_back(threshold * (float) std::cos(f));
      yShift.push_back(threshold * (float) std::sin(f));
    }
  }
  return std::pair<std::vector<float>, std::vector<float> >(xShift, yShift);
}

void ThreeDimensionalUtils::getMvpMatrix(float mvpMatrix[16],
                                         float xShift,
                                         float yShift,
                                         float threshold,
                                         float perspectiveScale) {
  float eyeX = xShift / threshold * perspectiveScale;
  float eyeY = yShift / threshold * perspectiveScale;
  float eyeZ = -2.0f;
  float centerX = 0.0f;
  float centerY = 0.0f;
  float centerZ = 0.0f;
  float upX = 0.0f;
  float upY = 1.0f;
  float upZ = 0.0f;

  // 计算和传入变换矩阵
  // 相对于屏幕坐标系将摄像头固定在（eyeX，eyeY，eyeZ）（0，0，-2）方向，
  // 看向屏幕正中点（centerX，centerY，centerZ）（0，0，0），
  // 以屏幕向上为正方向（upX，upY，upZ）（0，1，0）
  float viewMatrix[16];
  ThreeDimensionalUtils::setLookAtM(viewMatrix, eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ);

  double degreeX = std::atan(std::abs(eyeX) / std::abs(eyeZ));
  double distanceX = std::sqrt(eyeX * eyeX + eyeZ * eyeZ);
  double sinDegreeX = std::sin(degreeX);
  double cosDegreeX = std::cos(degreeX);
  double nearX = distanceX - sinDegreeX;
  double farX = distanceX + sinDegreeX;
  double left;
  double right;
  if (eyeX < 0) {
    left = cosDegreeX;
    right = -1.0 * nearX / farX * cosDegreeX;
  } else {
    left = nearX / farX * cosDegreeX;
    right = -1.0 * cosDegreeX;
  }

  double degreeY = std::atan(std::abs(eyeY) / std::abs(eyeZ));
  double distanceY = std::sqrt(eyeY * eyeY + eyeZ * eyeZ);
  double sinDegreeY = std::sin(degreeY);
  double cosDegreeY = std::cos(degreeY);
  double nearY = distanceY - sinDegreeY;
  double farY = distanceY + sinDegreeY;
  double top;
  double bottom;
  if (eyeY < 0) {
    top = nearY / farY * cosDegreeY;
    bottom = -1.0 * cosDegreeY;
  } else {
    top = cosDegreeY;
    bottom = -1.0 * nearY / farY * cosDegreeY;
  }

  double distanceToZero = std::sqrt(eyeX * eyeX + eyeY * eyeY + eyeZ * eyeZ);
  double distanceInXY = std::sqrt(2.0);
  double distanceToNearest = std::sqrt((std::abs(eyeX) - 1.0) * (std::abs(eyeX) - 1.0) + (std::abs(eyeY) - 1.0) * (
          std::abs(eyeY) - 1.0) + eyeZ * eyeZ);
  double cos = (distanceToZero * distanceToZero + distanceToNearest * distanceToNearest
      - distanceInXY * distanceInXY) / (2.0 * distanceToZero * distanceToNearest);

  double near = distanceToNearest * cos;
  double far = std::max(farX, farY) * 2;

  float cutMax = 1.0f;
  float cutMin = 0.98f;
  float d = (float) std::sqrt(eyeX * eyeX + eyeY * eyeY);
  float cut = (cutMax - cutMin) * d + cutMin;
  double cutX = nearY / farY * cosDegreeY;
  double cutY = nearX / farX * cosDegreeX;

  // 创建一个透视视景体
  // 注意：near,far都必须大于0，且二者不能相等，left、right、bottom、top 是near的平面的坐标
  // 但是由于相机视点为（0，0，-2）而屏幕应该显示的是正方向（0，0，x）x大于0的方向的画面，
  // 所有left对应的屏幕坐标应该为负，right对应的坐标系为正，呈左右翻转的效果。
  // 将near设置为2，far设置为3。由此可以得到视图可见区域为由视点（0，0，-2）出发的距离为2-4的区域内的物体
  // （注意不是由0，0，0点出发距离2-4的区域）。
  float projectMatrix[16];
  ThreeDimensionalUtils::frustumM(projectMatrix, (float) left * cut * (float) cutX,
                                 (float) right * cut * (float) cutX, (float) bottom * cut * (float) cutY,
                                 (float) top * cut * (float) cutY, (float) near, (float) far);

  ThreeDimensionalUtils::multiplyMM(mvpMatrix, viewMatrix, projectMatrix);
}

void ThreeDimensionalUtils::translateM(float m[16], float x, float y, float z) {
  for (int i = 0; i < 4; i++) {
    m[12 + i] += m[i] * x + m[4 + i] * y + m[8 + i] * z;
  }
}

void ThreeDimensionalUtils::setLookAtM(float rm[16],
                                       float eyeX,
                                       float eyeY,
                                       float eyeZ,
                                       float centerX,
                                       float centerY,
                                       float centerZ,
                                       float upX,
                                       float upY,
                                       float upZ) {
  // See the OpenGL GLUT documentation for gluLookAt for a description
  // of the algorithm. We implement it in a straightforward way:

  float fx = centerX - eyeX;
  float fy = centerY - eyeY;
  float fz = centerZ - eyeZ;

  // Normalize f
  const float rlf = 1.0f / std::sqrt(fx * fx + fy * fy + fz * fz);
  fx *= rlf;
  fy *= rlf;
  fz *= rlf;

  // compute s = f x up (x means "cross product")
  float sx = fy * upZ - fz * upY;
  float sy = fz * upX - fx * upZ;
  float sz = fx * upY - fy * upX;

  // and normalize s
  const float rls = 1.0f / std::sqrt(sx * sx + sy * sy + sz * sz);
  sx *= rls;
  sy *= rls;
  sz *= rls;

  // compute u = s x f
  const float ux = sy * fz - sz * fy;
  const float uy = sz * fx - sx * fz;
  const float uz = sx * fy - sy * fx;

  rm[0] = sx;
  rm[1] = ux;
  rm[2] = -fx;
  rm[3] = 0.0f;

  rm[4] = sy;
  rm[5] = uy;
  rm[6] = -fy;
  rm[7] = 0.0f;

  rm[8] = sz;
  rm[9] = uz;
  rm[10] = -fz;
  rm[11] = 0.0f;

  rm[12] = 0.0f;
  rm[13] = 0.0f;
  rm[14] = 0.0f;
  rm[15] = 1.0f;

  translateM(rm, -eyeX, -eyeY, -eyeZ);
}

void ThreeDimensionalUtils::frustumM(float m[16],
                                     float left,
                                     float right,
                                     float bottom,
                                     float top,
                                     float near,
                                     float far) {
  const float r_width = 1.0f / (right - left);
  const float r_height = 1.0f / (top - bottom);
  const float r_depth = 1.0f / (near - far);
  const float x = 2.0f * (near * r_width);
  const float y = 2.0f * (near * r_height);
  const float A = (right + left) * r_width;
  const float B = (top + bottom) * r_height;
  const float C = (far + near) * r_depth;
  const float D = 2.0f * (far * near * r_depth);
  m[0] = x;
  m[5] = y;
  m[8] = A;
  m[9] = B;
  m[10] = C;
  m[14] = D;
  m[11] = -1.0f;
  m[1] = 0.0f;
  m[2] = 0.0f;
  m[3] = 0.0f;
  m[4] = 0.0f;
  m[6] = 0.0f;
  m[7] = 0.0f;
  m[12] = 0.0f;
  m[13] = 0.0f;
  m[15] = 0.0f;
}

void ThreeDimensionalUtils::multiplyMM(float result[16], float lhs[16], float rhs[16]) {
  for (int i = 0; i < 4; i++) {
    for (int j = 0; j < 4; j++) {
      float sum = 0.0f;
      for (int k = 0; k < 4; k++) {
        sum += lhs[4 * i + k] * rhs[4 * k + j];
      }
      result[4 * i + j] = sum;
    }
  }
}

}