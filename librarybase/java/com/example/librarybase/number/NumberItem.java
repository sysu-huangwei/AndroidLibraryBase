package com.example.librarybase.number;

import android.util.Log;

/**
 * User: rayyyhuang
 * Date: 2020/8/14
 * Description: 单个滚动数字的配置信息
 */
public class NumberItem {

    public float left = 0.0f;

    public float top = 0.0f;

    public float right = 0.0f;

    public float bottom = 0.0f;

    public float maxSpeed = 0.0f;

    public float speedUpTime = 0.0f;

    public float continueTime = 0.0f;

    public float stopTime = 0.0f;

    public int targetNumber = 0;

    float currentPosition = 0.0f;

    public void calculateCurrentPosition(float currentTime) {
        if (currentTime < speedUpTime) {
            //加速阶段
            currentPosition = maxSpeed * currentTime * currentTime / speedUpTime * 0.5f;
        } else if (currentTime < speedUpTime + continueTime) {
            //保持最大速度匀速阶段
            currentPosition = speedUpTime * maxSpeed * 0.5f + (currentTime - speedUpTime) * maxSpeed;
        } else {
            //减速阶段
            //当即将要开始减速时，已经跑过了多少距离 = 加速距离+匀速距离
            float totalPositionWhenBeginStop = speedUpTime * maxSpeed * 0.5f + continueTime * maxSpeed;
            //原本应该需要多长距离来减速（从最大速度开始，按照设定的减速时间计算）
            float totalStopPositionShouldBe = maxSpeed * stopTime * 0.5f;
            //原本应该跑完的整个距离 = 加速距离+匀速距离+减速距离
            float totalAllPositionShouldBe = totalStopPositionShouldBe + totalPositionWhenBeginStop;
            //原本应该最终停下来的位置（按照原本输入的最大速度、各种时间等参数计算得来的）
            float finalPositionShouldBe  = totalAllPositionShouldBe - (int)totalAllPositionShouldBe;
            //目标停留位置（预设是长条形的数字图片，一个数字占1/10，所以这里需要除以10）
            float targetPosition = (float)targetNumber / 10.0f;
            //实际上需要多长距离来减速，对照目标位置，不租一个周期就补足，超过一个周期就顺延到下个周期
            //例如：原本停留0.4，目标需要停留0.7，那么实际上需要 + 0.3 （0.7-0.4）
            //例如：原本停留0.8，目标需要停留0.2，那么实际上需要 + 0.4 （0.2-0.8+1）
            float totalStopPositionActual;
            if (targetPosition >= finalPositionShouldBe) {
                totalStopPositionActual = totalStopPositionShouldBe + (targetPosition - finalPositionShouldBe);
            } else {
                totalStopPositionActual = totalStopPositionShouldBe + (targetPosition - finalPositionShouldBe + 1.0f);
            }
            //总的减速时间里，仍然以最大速度匀速运动的时间
            float maxSpeedStopTime = 2.0f * totalStopPositionActual / maxSpeed - stopTime;
            //总的减速时间里，实际上真正的减速时间 = 总减速时间 - 最大速度匀速运动的时间
            float realStopTime = stopTime - maxSpeedStopTime;
            if (currentTime < speedUpTime + continueTime + maxSpeedStopTime) {
                //在总的减速阶段里仍以最大速度运行时
                currentPosition = speedUpTime * maxSpeed * 0.5f + continueTime * maxSpeed + (currentTime - speedUpTime - continueTime) * maxSpeed;
            } else if (currentTime < speedUpTime + continueTime + stopTime){
                //在总的减速阶段里减速时
                //当前已经经历的真正的减速时间
                float currentStopTime = currentTime - speedUpTime - continueTime - maxSpeedStopTime;
                //剩下的实际真正减速时间 = 实际真正减速时间 - 已经经历过的实际真正减速时间
                float leftTime = realStopTime - currentStopTime;
                currentPosition = speedUpTime * maxSpeed * 0.5f + continueTime * maxSpeed + maxSpeedStopTime * maxSpeed + (maxSpeed + maxSpeed * (leftTime / realStopTime)) * currentStopTime * 0.5f;
            } else {
                //已经减速完毕，最终停下来时
                currentPosition = speedUpTime * maxSpeed * 0.5f + continueTime * maxSpeed + maxSpeedStopTime * maxSpeed + realStopTime * maxSpeed * 0.5f;
            }
        }
        currentPosition -= (int)currentPosition;//取小数部分
    }

}
