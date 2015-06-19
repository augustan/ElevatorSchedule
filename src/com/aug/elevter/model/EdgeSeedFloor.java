package com.aug.elevter.model;

/**
 * 楼层数从1开始
 *
 */
public class EdgeSeedFloor {
    private int top = 0;
    private int bottom = Integer.MAX_VALUE;
    
    public void setFloor(int floor) {
        top = Math.max(top, floor);
        bottom = Math.min(bottom, floor);
    }
    
    public int getBottom() {
        return bottom;
    }
    
    public int getTop() {
        return top;
    }
}
