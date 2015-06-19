package com.aug.elevter.model;

import com.aug.elevter.main.Constants;
import com.aug.elevter.tools.LogUtils;

import java.util.Random;

/**
 * 楼层数从1开始
 *
 */
public class Seed {

    private static final String DIR_UP = "UP";
    private static final String DIR_DOWN = "DOWN";
    
    // ID. 楼层   上/下按钮   要去几层    读取下一条数据间隔时间  
    private static final String seedLineFormat = "%d,%d,%s,%d,%d";
    
    private static Random random = new Random(System.nanoTime());
    
    private int id = 0;
    private int floor = 0;
    private int toFloor = 0;
    private boolean isDown = false;
    private int waitTime = 0;
    private int curWaitTime = 0;
    
    private int markElevterId = -1;
    private int minStepCost = Integer.MAX_VALUE;
    
    private int beginTime = 0;    // 开始等待的时间。统计用
    private int takeTime = 0;     // 上电梯的时间。统计用
    private int releaseTime = 0;  // 下电梯的时间。统计用

    private Seed() {
    }
    
    public Seed(int id) {
        this.id = id;
        int maxWaitTime = Constants.totalFloor / Constants.elevterCount;
        if (maxWaitTime <= 0) {
            maxWaitTime = 1;
        }
        
        floor = random.nextInt(Constants.totalFloor) + 1;
        toFloor = floor;
        while (toFloor == floor) {
            toFloor = random.nextInt(Constants.totalFloor) + 1;
        }
        isDown = toFloor < floor;
        waitTime = random.nextInt(maxWaitTime) + 1;
        curWaitTime = waitTime;
    }
    
    public static Seed parse(String record) {
        Seed seed = null;
        try {
            String [] input = record.split(",");
            if (input != null && input.length == 5) {
                seed = new Seed();
                seed.id = Integer.valueOf(input[0]);
                seed.floor = Integer.valueOf(input[1]);
                seed.toFloor = Integer.valueOf(input[3]);
                seed.waitTime = Integer.valueOf(input[4]);
                seed.curWaitTime = seed.waitTime;
                seed.isDown = seed.toFloor < seed.floor;
            }
        } catch (Exception e) {
            seed = null;
        }
        return seed;
    }
    
    public String toString() {
        String record = String.format(seedLineFormat, id, floor,
                isDown ? DIR_DOWN : DIR_UP, toFloor, waitTime);
        return record;
    }
    
    public String toDumpString() {
        String record = String.format("[%d]: from %d %s to %d, after %d", id, floor,
                isDown ? DIR_DOWN : DIR_UP, toFloor, waitTime);
        return record;
    }

    public int getId() {
        return id;
    }

    public int getFloor() {
        return floor;
    }

    public int getToFloor() {
        return toFloor;
    }

    public boolean isDown() {
        return isDown;
    }

    public int getWaitTime() {
        return curWaitTime;
    }
    
    public void setWaitTime(int waitTime) {
        this.curWaitTime = waitTime;
    }
    
    public int getMarkElevterId() {
        return markElevterId;
    }
    
    public void clearMarkElevterId() {
        minStepCost = Integer.MAX_VALUE;
        markElevterId = -1;
        LogUtils.d("   [SEED] clear ElevterId");
    }
    
    public void setMarkElevterId(int markElevterId, int stepCost) {
        if (stepCost < minStepCost) {
            minStepCost = stepCost;
            this.markElevterId = markElevterId;
            LogUtils.d("   [SEED] " + this.toDumpString() + " set ElevterId = " + markElevterId);
        }
    }
    
    public int getBeginTime() {
        return beginTime;
    }
    
    public void setBeginTime(int beginTime) {
        this.beginTime = beginTime;
    }
    
    public int getTakeTime() {
        return takeTime;
    }
    
    public void setTakeTime(int takeTime) {
        this.takeTime = takeTime;
    }
    
    public int getReleaseTime() {
        return releaseTime;
    }
    
    public void setReleaseTime(int releaseTime) {
        this.releaseTime = releaseTime;
    }
}
