package com.aug.elevter.model;

import com.aug.elevter.main.Constants;
import com.aug.elevter.policy.ElevterPolicy;
import com.aug.elevter.policy.StandardPolicy;
import com.aug.elevter.tools.LogUtils;

import java.util.ArrayList;

public class Elevter {

    public enum MoveStatus {
        UP, DOWN, IDLE, PRE_IDLE_UP, PRE_IDLE_DOWN,
    }

    private int totalStep = 0;
    private int totalLoad = 0;  // 总负载

    private int id = 0;
    private int maxLoad = Constants.elevterLoadCapacity;
    private int topFloor = Constants.totalFloor;
    private int bottomFloor = 1;

    private MoveStatus moveStatus = MoveStatus.IDLE;
    private ElevterPolicy elevterPolicy = new StandardPolicy();

    private ArrayList<Seed> loadSeeds = new ArrayList<Seed>();
    private int currentFloor = bottomFloor;

    public Elevter(int id) {
        this.id = id;
    }

    public int getCurrentLoad() {
        return loadSeeds != null ? loadSeeds.size() : 0;
    }

    public int getId() {
        return id;
    }

    public boolean isOverLoad() {
        return getCurrentLoad() >= maxLoad;
    }

    /**
     * 获取剩余空间
     */
    public int getLoadSpace() {
        return maxLoad - getCurrentLoad();
    }

    public int getCurrentFloor() {
        return currentFloor;
    }

    private boolean isGetTop() {
        return currentFloor == topFloor && moveStatus == MoveStatus.UP;
    }

    private boolean isGetBottom() {
        return currentFloor == bottomFloor && moveStatus == MoveStatus.DOWN;
    }

    public MoveStatus getMoveStatus() {
        return moveStatus;
    }

    public void onStopAtFloor() {
        releaseSeeds();
    }

    /**
     * 在seed结构中标记出最小消耗的电梯id
     * 
     * @param floor 实际楼层数。从1开始
     * @param seedsListAtFloor
     */
    public void preHandleSeeds(int floor, ArrayList<Seed> seedsListAtFloor) {
        elevterPolicy.takeSeed(this, floor, seedsListAtFloor);
    }

    private void releaseSeeds() {
        for (int i = loadSeeds.size() - 1; i >= 0; i--) {
            Seed seed = loadSeeds.get(i);
            if (seed.getToFloor() == currentFloor) {
                loadSeeds.remove(i);
                LogUtils.e(String.format("   [RELEASE] #%d# release %s. at floor = %d. load = %d", id,
                        seed.toDumpString(), currentFloor, getCurrentLoad()));
            }
        }
    }

    public void takeSeeds(ArrayList<Seed> newSeeds) {
        for (Seed seed : newSeeds) {
            loadSeeds.add(seed);
            LogUtils.e(String.format("   [TAKE] #%d# take %s. at floor = %d. load = %d", id, seed.toDumpString(),
                    currentFloor, getCurrentLoad()));
        }
    }

    public void gotoNextFloor() {
        if (moveStatus == MoveStatus.DOWN) {
            if (isGetBottom()) {
                moveStatus = MoveStatus.IDLE;
            } else {
                currentFloor--;
                totalStep++;
                totalLoad += getCurrentLoad();
                LogUtils.e(String.format("   [MOVE] #%d# move down at %d. total_step = %d, total_load = %d", id, currentFloor,
                        totalStep, totalLoad));
            }
        } else if (moveStatus == MoveStatus.UP) {
            if (isGetTop()) {
                moveStatus = MoveStatus.IDLE;
            } else {
                currentFloor++;
                totalStep++;
                totalLoad += getCurrentLoad();
                LogUtils.e(String.format("   [MOVE] #%d# move up at %d. total_step = %d, total_load = %d", id, currentFloor,
                        totalStep, totalLoad));
            }
        }
    }

    public void setActive(int gotoFloor) {
        boolean setted = false;
        if (moveStatus == MoveStatus.PRE_IDLE_DOWN && currentFloor > gotoFloor) {
            moveStatus = MoveStatus.DOWN;
            setted = true;
        } else if (moveStatus == MoveStatus.PRE_IDLE_UP && currentFloor < gotoFloor) {
            moveStatus = MoveStatus.UP;
            setted = true;
        } else if (moveStatus == MoveStatus.IDLE && currentFloor != gotoFloor) {
            moveStatus = currentFloor > gotoFloor ? MoveStatus.DOWN : MoveStatus.UP;
            setted = true;
        } else if (currentFloor == gotoFloor && 
                (moveStatus == MoveStatus.PRE_IDLE_DOWN || moveStatus == MoveStatus.PRE_IDLE_UP)) {
            moveStatus = moveStatus == MoveStatus.PRE_IDLE_DOWN ? MoveStatus.DOWN : MoveStatus.UP;
            setted = true;
        }

        if (setted) {
            LogUtils.d(String.format("   [ACTIVE] elevter #%d# from %d to %d", id, currentFloor, gotoFloor));
        }
    }

    public void checkActive(boolean beginCheck) {
        if (getCurrentLoad() == 0) {
            if (beginCheck) {
                if (moveStatus == MoveStatus.DOWN) {
                    moveStatus = MoveStatus.PRE_IDLE_DOWN;
                } else if (moveStatus == MoveStatus.UP) {
                    moveStatus = MoveStatus.PRE_IDLE_UP;
                }
            } else if (moveStatus == MoveStatus.PRE_IDLE_DOWN || moveStatus == MoveStatus.PRE_IDLE_UP) {
                moveStatus = MoveStatus.IDLE;
            }
        }
    }

    public boolean isIdle() {
        return moveStatus == MoveStatus.IDLE;
    }
    
    public int getTotalStep() {
        return totalStep;
    }
    
    public int getTotalLoad() {
        return totalLoad;
    }
}
