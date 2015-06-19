package com.aug.elevtor.model;

import com.aug.elevtor.tools.LogUtils;

public class Statistic {
    
    private static int timeTickTack = 0;
    private static int totalStep = 0;  // 总移动步数
    private static int totalLoad = 0;  // 总负载
    
    private static int totalWaitTime = 0;  // 乘客等待上电梯的时间
    private static int totalSpendTime = 0;  // 乘客总共等待时间
    
    public static int onTimeLapse() {
        timeTickTack++;
        return timeTickTack;
    }
    
    public static void onMove(Elevtor elevtor) {
        totalStep++;
        totalLoad += elevtor.getCurrentLoad();
    }

    /**
     * 首次出现
     */
    public static void onShowNewSeed(Seed seed) {
        seed.setBeginTime(timeTickTack);
    }
    
    /**
     * 上电梯
     */
    public static void onTakeSeed(Seed seed) {
        seed.setTakeTime(timeTickTack);
        totalWaitTime += (timeTickTack - seed.getBeginTime());
    }

    /**
     * 下电梯
     */
    public static void onReleaseSeed(Seed seed) {
        seed.setReleaseTime(timeTickTack);
        totalSpendTime += (timeTickTack - seed.getBeginTime());
    }
    
    public static void showResule() {
      LogUtils.d("! [Statistic] timeTick = " + timeTickTack);
      LogUtils.d("! [Statistic] totalStep = " + totalStep);
      LogUtils.d("! [Statistic] totalLoad = " + totalLoad);
      LogUtils.d("! [Statistic] totalWaitTime = " + totalWaitTime);
      LogUtils.d("! [Statistic] totalSpendTime = " + totalSpendTime);
    }
}
