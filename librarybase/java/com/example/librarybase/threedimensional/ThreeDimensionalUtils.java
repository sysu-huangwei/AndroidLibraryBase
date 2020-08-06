package com.example.librarybase.threedimensional;

import androidx.annotation.IntDef;
import androidx.annotation.StringDef;
import java.util.ArrayList;

/**
 * User: rayyyhuang
 * Date: 2020/7/31
 * Description: 3D效果数据预处理方法
 */
public class ThreeDimensionalUtils {

//    static {
//        System.loadLibrary("threedimensional");
//    }

    /* 左右 */
    public static final int DIRECTION_TYPE_LEFT_RIGHT = 1;
    /* 上下 */
    public static final int DIRECTION_TYPE_UP_DOWN = 2;
    /* 转圈 */
    public static final int DIRECTION_TYPE_CIRCLE = 3;

    /**
     * 计算3D效果所需要的数据
     *
     * @param directionType 运动方向类型
     * @param depthScale 景深效果程度，最小0，最大1，默认0.5
     * @param perspectiveScale 透视效果程度，最小0，最大1，默认0.3
     * @param speed 效果速度，最小0，最大1，默认0.5
     * @return 3D效果数据
     */
    public static ArrayList<ThreeDimensionalOneFrameData> calculateThreeDimensionalData(@DirectionType int directionType, float depthScale, float perspectiveScale, float speed) {
        ArrayList<ThreeDimensionalOneFrameData> result = new ArrayList<>();
        long[] threeDimensionalOneFrameDataNativeInstances = nativeCalculateThreeDimensionalData(directionType, depthScale, perspectiveScale, speed);
        if (threeDimensionalOneFrameDataNativeInstances != null) {
            for (long threeDimensionalOneFrameDataNativeInstance : threeDimensionalOneFrameDataNativeInstances) {
                result.add(new ThreeDimensionalOneFrameData(threeDimensionalOneFrameDataNativeInstance));
            }
        }
        return result;
    }

    private static native long[] nativeCalculateThreeDimensionalData(@DirectionType int directionType, float depthScale, float perspectiveScale, float speed);

    /**
     * 运动方向类型
     */
    @IntDef({DIRECTION_TYPE_LEFT_RIGHT, DIRECTION_TYPE_UP_DOWN, DIRECTION_TYPE_CIRCLE})
    public @interface DirectionType {

    }

}
