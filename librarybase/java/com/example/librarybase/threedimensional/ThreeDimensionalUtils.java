package com.example.librarybase.threedimensional;

import android.opengl.Matrix;
import android.util.Pair;
import androidx.annotation.StringDef;
import java.util.ArrayList;
import java.util.Collections;

/**
 * User: rayyyhuang
 * Date: 2020/7/31
 * Description: 3D效果数据预处理方法
 */
public class ThreeDimensionalUtils {

    /* 左右 */
    public static final String DIRECTION_TYPE_LEFT_RIGHT = "DIRECTION_TYPE_LEFT_RIGHT";
    /* 上下 */
    public static final String DIRECTION_TYPE_UP_DOWN = "DIRECTION_TYPE_UP_DOWN";
    /* 转圈 */
    public static final String DIRECTION_TYPE_CIRCLE = "DIRECTION_TYPE_CIRCLE";

    /**
     * 运动方向类型
     */
    @StringDef({DIRECTION_TYPE_LEFT_RIGHT, DIRECTION_TYPE_UP_DOWN, DIRECTION_TYPE_CIRCLE})
    public @interface DirectionType {
    }

    /* 默认的运动幅度的最大值（振幅） */
    private static final float DEFAULT_THRESHOLD = 0.5f;

    /* 默认的步长。【步长：1/4个周期的步长（周期，或者说是=1/频率）】 */
    private static final int DEFAULT_STEP = 20;

    /**
     * 计算3D效果所需要的数据
     *
     * @param directionType 运动方向类型
     * @param depthScale 景深效果程度，最小0，最大1，默认0.5
     * @param perspectiveScale 透视效果程度，最小0，最大1，默认0.3
     * @param speed 效果速度，最小0，最大1，默认0.5
     * @return 3D效果数据
     */
    public static ArrayList<ThreeDimensionalOneFrameData> calculateThreeDimensionalData(@DirectionType String directionType, float depthScale, float perspectiveScale, float speed) {
        ArrayList<ThreeDimensionalOneFrameData> result = new ArrayList<>();

        Pair<ArrayList<Float>, ArrayList<Float>> shift = getShift(directionType, DEFAULT_THRESHOLD, getStepBySpeed(speed));
        if (shift != null) {
            for (int i = 0; i < shift.first.size(); i++) {
                ThreeDimensionalOneFrameData threeDimensionalOneFrameData = new ThreeDimensionalOneFrameData();
                threeDimensionalOneFrameData.xShift = shift.first.get(i);
                threeDimensionalOneFrameData.yShift = shift.second.get(i);
                getMvpMatrix(threeDimensionalOneFrameData.mvpMatrix, threeDimensionalOneFrameData.xShift, threeDimensionalOneFrameData.yShift, DEFAULT_THRESHOLD, perspectiveScale);
                threeDimensionalOneFrameData.depthScale = depthScale;
                threeDimensionalOneFrameData.perspectiveScale = perspectiveScale;
                result.add(threeDimensionalOneFrameData);
            }
        }

        return result;
    }

    /**
     * 通过设置的速度计算步长（默认速度时步长是 DEFAULT_STEP，速度最小时步长是2倍默认值 2*DEFAULT_STEP）
     *
     * @param speed 效果速度，最小0，最大1，默认0.5
     * @return 1/4个周期的步长（周期，或者说是=1/频率）
     */

    private static int getStepBySpeed(float speed) {
        int step = (int)((-2.0f) * speed * DEFAULT_STEP) + 2 * DEFAULT_STEP;
        if (step <= 0) {
            step = 1;
        }
        return step;
    }

    /**
     * 获取前景后景坐标偏移量
     *
     * @param direction 运动方向类型
     * @param threshold 最大偏移量
     * @param step 1/4周期的步长
     * @return first：x方向偏移量  second：y方向偏移量
     */
    private static Pair<ArrayList<Float>, ArrayList<Float>> getShift(@DirectionType String direction, float threshold, int step) {
        ArrayList<Float> xShift = new ArrayList<>();
        ArrayList<Float> yShift = new ArrayList<>();
        if (direction.equals(DIRECTION_TYPE_LEFT_RIGHT) || direction.equals(DIRECTION_TYPE_UP_DOWN)) {
            ArrayList<Float> positive = new ArrayList<>();
            ArrayList<Float> negative = new ArrayList<>();
            for (float f = 0.0f; f < threshold; f += (threshold / step)) {
                positive.add(f);
                negative.add(-f);
            }
            ArrayList<Float> animalFunction = new ArrayList<>();
            animalFunction.addAll(positive);
            Collections.reverse(positive);
            animalFunction.addAll(positive);
            animalFunction.addAll(negative);
            Collections.reverse(negative);
            animalFunction.addAll(negative);

            xShift.addAll(animalFunction);
            yShift.addAll(animalFunction);

            if (direction.equals(DIRECTION_TYPE_LEFT_RIGHT)) {
                Collections.fill(yShift, 0.0f);
            } else {
                Collections.fill(xShift, 0.0f);
            }

        } else if (direction.equals(DIRECTION_TYPE_CIRCLE)) {
            float fullDeg = (float) Math.PI * 2.0f;
            for (float f = 0.0f; f < fullDeg; f += fullDeg / step / 4.0f) {
                xShift.add(threshold * (float) Math.cos(f));
                yShift.add(threshold * (float) Math.sin(f));
            }
        }
        return Pair.create(xShift, yShift);
    }

    /**
     * 计算透视矩阵
     *
     * @param mvpMatrix 4*4矩阵[输出]
     * @param xShift x方向的景深偏移值
     * @param yShift y方向的景深偏移值
     * @param threshold 最大偏移量
     * @param perspectiveScale 透视效果程度
     */
    private static void getMvpMatrix(float[] mvpMatrix, float xShift, float yShift, float threshold, float perspectiveScale) {

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
        //相对于屏幕坐标系将摄像头固定在（eyeX，eyeY，eyeZ）（0，0，-2）方向，看向屏幕正中点（centerX，centerY，centerZ）（0，0，0），以屏幕向上为正方向（upX，upY，upZ）（0，1，0）
        float[] viewMatrix = new float[16];
        ThreeDimensionalUtils.setLookAtM(viewMatrix, eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ);

        double degreeX = Math.atan(Math.abs(eyeX) / Math.abs(eyeZ));
        double distanceX = Math.sqrt(eyeX * eyeX + eyeZ * eyeZ);
        double sinDegreeX = Math.sin(degreeX);
        double cosDegreeX = Math.cos(degreeX);
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

        double degreeY = Math.atan(Math.abs(eyeY) / Math.abs(eyeZ));
        double distanceY = Math.sqrt(eyeY * eyeY + eyeZ * eyeZ);
        double sinDegreeY = Math.sin(degreeY);
        double cosDegreeY = Math.cos(degreeY);
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

        double distanceToZero = Math.sqrt(eyeX * eyeX + eyeY * eyeY + eyeZ * eyeZ);
        double distanceInXY = Math.sqrt(2.0);
        double distanceToNearest = Math.sqrt((Math.abs(eyeX) - 1.0) * (Math.abs(eyeX) - 1.0) + (Math.abs(eyeY) - 1.0) * (Math.abs(eyeY) - 1.0) + eyeZ * eyeZ);
        double cos = (distanceToZero * distanceToZero + distanceToNearest * distanceToNearest - distanceInXY * distanceInXY) / (2.0 * distanceToZero * distanceToNearest);

        double near = distanceToNearest * cos;
        double far = Math.max(farX, farY) * 2;

        float cutMax = 1.0f;
        float cutMin = 0.98f;
        float d = (float)Math.sqrt(eyeX * eyeX + eyeY * eyeY);
        float cut = (cutMax - cutMin) * d + cutMin;
        double cutX = nearY / farY * cosDegreeY;
        double cutY = nearX / farX * cosDegreeX;

        //创建一个透视视景体
        //注意：near,far都必须大于0，且二者不能相等，left、right、bottom、top 是near的平面的坐标
        //但是由于相机视点为（0，0，-2）而屏幕应该显示的是正方向（0，0，x）x大于0的方向的画面，所有left对应的屏幕坐标应该为负，right对应的坐标系为正，呈左右翻转的效果。
        //将near设置为2，far设置为3。由此可以得到视图可见区域为由视点（0，0，-2）出发的距离为2-4的区域内的物体。（注意不是由0，0，0点出发距离2-4的区域）。
        float[] projectMatrix = new float[16];
        ThreeDimensionalUtils.frustumM(projectMatrix,  (float)left * cut * (float)cutX, (float)right * cut * (float)cutX, (float)bottom * cut * (float)cutY, (float)top * cut * (float)cutY, (float)near, (float)far);

        ThreeDimensionalUtils.multiplyMM(mvpMatrix, viewMatrix, projectMatrix);
    }

    /**
     * 平移变换
     *
     * @param m 4*4矩阵[输出]
     * @param x x方向平移量
     * @param y y方向平移量
     * @param z z方向平移量
     */
    private static void translateM(float[] m, float x, float y, float z) {
        for (int i = 0; i < 4; i++) {
            m[12 + i] += m[i] * x + m[4 + i] * y + m[8 + i] * z;
        }
    }

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
    public static void setLookAtM(float[] rm,
            float eyeX, float eyeY, float eyeZ,
            float centerX, float centerY, float centerZ,
            float upX, float upY, float upZ) {

        // See the OpenGL GLUT documentation for gluLookAt for a description
        // of the algorithm. We implement it in a straightforward way:

        float fx = centerX - eyeX;
        float fy = centerY - eyeY;
        float fz = centerZ - eyeZ;

        // Normalize f
        float rlf = 1.0f / Matrix.length(fx, fy, fz);
        fx *= rlf;
        fy *= rlf;
        fz *= rlf;

        // compute s = f x up (x means "cross product")
        float sx = fy * upZ - fz * upY;
        float sy = fz * upX - fx * upZ;
        float sz = fx * upY - fy * upX;

        // and normalize s
        float rls = 1.0f / Matrix.length(sx, sy, sz);
        sx *= rls;
        sy *= rls;
        sz *= rls;

        // compute u = s x f
        float ux = sy * fz - sz * fy;
        float uy = sz * fx - sx * fz;
        float uz = sx * fy - sy * fx;

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
    public static void frustumM(float[] m,
            float left, float right, float bottom, float top,
            float near, float far) {
        if (left == right) {
            throw new IllegalArgumentException("left == right");
        }
        if (top == bottom) {
            throw new IllegalArgumentException("top == bottom");
        }
        if (near == far) {
            throw new IllegalArgumentException("near == far");
        }
        if (near <= 0.0f) {
            throw new IllegalArgumentException("near <= 0.0f");
        }
        if (far <= 0.0f) {
            throw new IllegalArgumentException("far <= 0.0f");
        }
        final float r_width  = 1.0f / (right - left);
        final float r_height = 1.0f / (top - bottom);
        final float r_depth  = 1.0f / (near - far);
        final float x = 2.0f * (near * r_width);
        final float y = 2.0f * (near * r_height);
        final float A = (right + left) * r_width;
        final float B = (top + bottom) * r_height;
        final float C = (far + near) * r_depth;
        final float D = 2.0f * (far * near * r_depth);
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

    /**
     * 两个4*4矩阵相乘
     *
     * @param result 4*4矩阵[输出]
     * @param lhs 4*4矩阵左
     * @param rhs 4*4矩阵右
     */
    public static void multiplyMM(float[] result, float[] lhs, float[] rhs) {
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
