package com.aug.elevtor.model;

import com.aug.elevtor.main.Constants;
import com.aug.elevtor.policy.ElevtorPolicy;
import com.aug.elevtor.policy.StandardPolicy;
import com.aug.elevtor.tools.LogUtils;

import java.util.ArrayList;

/**
 * 楼层数从1开始
 *
 */
public class Elevtor {

    public enum MoveStatus {
        UP, DOWN, IDLE, PRE_UP, PRE_DOWN,
    }

    private int totalStep = 0;  // 总共走了几步
    private int totalLoad = 0;  // 总负载

    private int id = 0;
    private int maxLoad = Constants.elevtorLoadCapacity;
    private int topFloor = Constants.totalFloor;
    private int bottomFloor = 1;

    private MoveStatus moveStatus = MoveStatus.IDLE;
    private ElevtorPolicy elevtorPolicy = new StandardPolicy();

    private ArrayList<Seed> loadSeeds = new ArrayList<Seed>();
    private int currentFloor = bottomFloor;

    public Elevtor(int id) {
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

    private boolean isReachTop() {
        return currentFloor == topFloor && moveStatus == MoveStatus.UP;
    }

    private boolean isReachBottom() {
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
    public void preHandleSeeds(int floor, ArrayList<Seed> seedsListAtFloor,
            int topFloor, int bottomFloor) {
        elevtorPolicy.preHandleSeeds(this, floor, seedsListAtFloor, topFloor, bottomFloor);
    }

    private void releaseSeeds() {
        for (int i = loadSeeds.size() - 1; i >= 0; i--) {
            Seed seed = loadSeeds.get(i);
            if (seed.getToFloor() == currentFloor) {
                loadSeeds.remove(i);
                Statistic.onReleaseSeed(seed);
                LogUtils.e(String.format("   [RELEASE] #%d# release %s. at floor = %d. load = %d", id,
                        seed.toDumpString(), currentFloor, getCurrentLoad()));
            }
        }
    }

    public void takeSeeds(ArrayList<Seed> newSeeds) {
        for (Seed seed : newSeeds) {
            loadSeeds.add(seed);
            Statistic.onTakeSeed(seed);
            LogUtils.e(String.format("   [TAKE] #%d# take %s. at floor = %d. load = %d", id, seed.toDumpString(),
                    currentFloor, getCurrentLoad()));
        }
    }

    public void gotoNextFloor() {
        if (moveStatus == MoveStatus.DOWN) {
            if (isReachBottom()) {
                moveStatus = MoveStatus.IDLE;
            } else {
                currentFloor--;
                totalStep++;
                totalLoad += getCurrentLoad();
                Statistic.onMove(this);
                LogUtils.e(String.format("   [MOVE] #%d# move down at %d. total_step = %d, total_load = %d. load = %d", id, currentFloor,
                        totalStep, totalLoad, getCurrentLoad()));
            }
        } else if (moveStatus == MoveStatus.UP) {
            if (isReachTop()) {
                moveStatus = MoveStatus.IDLE;
            } else {
                currentFloor++;
                totalStep++;
                totalLoad += getCurrentLoad();
                Statistic.onMove(this);
                LogUtils.e(String.format("   [MOVE] #%d# move up at %d. total_step = %d, total_load = %d. load = %d", id, currentFloor,
                        totalStep, totalLoad, getCurrentLoad()));
            }
        }
    }

    public void preSetActive(EdgeSeedFloor edgeFloor) {
        if (getCurrentLoad() == 0) {
            if (currentFloor >= edgeFloor.getTop() || 
                    currentFloor <= edgeFloor.getBottom()) {
                moveStatus = MoveStatus.IDLE;
            } else if (moveStatus == MoveStatus.DOWN) {
                moveStatus = MoveStatus.PRE_DOWN;
            } else if (moveStatus == MoveStatus.UP) {
                moveStatus = MoveStatus.PRE_UP;
            }
        }
    }
    
    public void setActive(int atFloor, int gotoFloor) {
      boolean setted = false;
      if (moveStatus == MoveStatus.IDLE) {
          if (currentFloor != atFloor) {
              moveStatus = currentFloor > atFloor ? MoveStatus.DOWN : MoveStatus.UP;
              setted = true;
          } else {
              moveStatus = currentFloor > gotoFloor ? MoveStatus.DOWN : MoveStatus.UP;
              setted = true;
          }
      } else if (moveStatus == MoveStatus.PRE_DOWN && currentFloor > atFloor) {
          moveStatus = MoveStatus.DOWN;
          setted = true;
      } else if (moveStatus == MoveStatus.PRE_UP && currentFloor < atFloor) {
          moveStatus = MoveStatus.UP;
          setted = true;
      }

      if (setted) {
          LogUtils.d(String.format("   [ACTIVE] elevtor #%d# from %d to %d", id, currentFloor, atFloor));
      }
    }
    
    public void setActiveIdle() {
        if (getCurrentLoad() == 0 &&
                (moveStatus == MoveStatus.PRE_DOWN || moveStatus == MoveStatus.PRE_UP)) {
            moveStatus = MoveStatus.IDLE;
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
