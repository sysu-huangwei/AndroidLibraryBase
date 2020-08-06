//
// Created by rayyy on 2020/8/5.
//

#ifndef SMARTKIT_THREEDIMENSIONALUTILS_H
#define SMARTKIT_THREEDIMENSIONALUTILS_H

#include <vector>
#include "ThreeDimensionalOneFrameData.h"

namespace threedimensional {

/**
 * Description: 3D效果数据预处理方法
 */
class ThreeDimensionalUtils {
 public:

  /* 左右 */
  const static int DIRECTION_TYPE_LEFT_RIGHT = 1;
  /* 上下 */
  const static int DIRECTION_TYPE_UP_DOWN = 2;
  /* 转圈 */
  const static int DIRECTION_TYPE_CIRCLE = 3;

  /**
   * 计算3D效果所需要的数据
   *
   * @param directionType 运动方向类型
   * @param depthScale 景深效果程度，最小0，最大1，默认0.5
   * @param perspectiveScale 透视效果程度，最小0，最大1，默认0.3
   * @param speed 效果速度，最小0，最大1，默认0.5
   * @return 3D效果数据
   */
  static std::vector<ThreeDimensionalOneFrameData>
  calculateThreeDimensionalData(
      int directionType, float depthScale, float perspectiveScale, float speed);

  /**
   * 通过设置的速度计算步长（默认速度时步长是 DEFAULT_STEP，
   * 速度最小时步长是2倍默认值 2*DEFAULT_STEP）
   *
   * @param speed 效果速度，最小0，最大1，默认0.5
   * @return 1/4个周期的步长（周期，或者说是=1/频率）
   */

   static int getStepBySpeed(float speed);

  /**
   * 获取前景后景坐标偏移量
   *
   * @param direction 运动方向类型
   * @param threshold 最大偏移量
   * @param step 1/4周期的步长
   * @return first：x方向偏移量  second：y方向偏移量
   */
  static std::pair<std::vector<float>, std::vector<float > > getShift(
      int direction, float threshold, int step);

  /**
   * 计算透视矩阵
   *
   * @param mvpMatrix 4*4矩阵[输出]
   * @param xShift x方向的景深偏移值
   * @param yShift y方向的景深偏移值
   * @param threshold 最大偏移量
   * @param perspectiveScale 透视效果程度
   */
  static void getMvpMatrix(float mvpMatrix[16], float xShift, float yShift,
                           float threshold, float perspectiveScale);

  /**
   * 平移变换
   *
   * @param m 4*4矩阵[输出]
   * @param x x方向平移量
   * @param y y方向平移量
   * @param z z方向平移量
   */
   static void translateM(float m[16], float x, float y, float z);

  /**
   * 设置观察视角
   *
   * @param rm 4*4矩阵[输出]
   * @param eyeX 相机位置坐标x
   * @param eyeY 相机位置坐标y
   * @param eyeZ 相机位置坐标z
   * @param centerX 相机朝向的坐标x
   * @param centerY 相机朝向的坐标y
   * @param centerZ 相机朝向的坐标z
   * @param upX 相机旋转的坐标x
   * @param upY 相机旋转的坐标y
   * @param upZ 相机旋转的坐标z
   */
  static void setLookAtM(float rm[16],
                         float eyeX, float eyeY, float eyeZ,
                         float centerX, float centerY, float centerZ,
                         float upX, float upY, float upZ);

  /**
   * 设置透视参数
   *
   * @param m 4*4矩阵[输出]
   * @param left 近平面的左边界
   * @param right 近平面的右边界
   * @param bottom 近平面的下边界
   * @param top 近平面的上边界
   * @param near 相机到近平面的距离
   * @param far 相机到远平面的距离
   */
  static void frustumM(float m[16],
                       float left, float right, float bottom, float top,
                       float near, float far);

  /**
   * 两个4*4矩阵相乘
   *
   * @param result 4*4矩阵[输出]
   * @param lhs 4*4矩阵左
   * @param rhs 4*4矩阵右
   */
  static void multiplyMM(float result[16], float lhs[16], float rhs[16]);
};

}

#endif //SMARTKIT_THREEDIMENSIONALUTILS_H
