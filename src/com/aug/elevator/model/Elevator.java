package com.aug.elevator.model;

import com.aug.elevator.main.Constants;
import com.aug.elevator.policy.ElevatorPolicy;
import com.aug.elevator.policy.StandardPolicy;
import com.aug.elevator.tools.LogUtils;

import java.util.ArrayList;

/**
 * 楼层数从1开始
 *
 */
public class Elevator {

    public enum MoveStatus {
        UP, DOWN, IDLE, PRE_UP, PRE_DOWN,
    }

    private int totalStep = 0;  // 总共走了几步
    private int totalLoad = 0;  // 总负载

    private int id = 0;
    private int maxLoad = Constants.elevatorLoadCapacity;
    private int topFloor = Constants.totalFloor;
    private int bottomFloor = 1;

    private MoveStatus moveStatus = MoveStatus.IDLE;
    private ElevatorPolicy elevatorPolicy = new StandardPolicy();

    private ArrayList<Seed> loadSeeds = new ArrayList<Seed>();
    private int currentFloor = bottomFloor;

    public Elevator(int id) {
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
     * @param seedAtFloor seed所在的实际楼层数。从1开始
     * @param seedsListAtFloor
     */
    public void preHandleSeeds(int seedAtFloor, ArrayList<Seed> seedsListAtFloor,
            EdgeFloor seedsEdgeFloor) {
        elevatorPolicy.preHandleSeeds(this, seedAtFloor, seedsListAtFloor, seedsEdgeFloor, getTakingEdgeFloor());
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
    
    private EdgeFloor getTakingEdgeFloor() {
        EdgeFloor edge = new EdgeFloor();
        for (int i = loadSeeds.size() - 1; i >= 0; i--) {
            Seed seed = loadSeeds.get(i);
            edge.setFloor(seed.getToFloor());
        }
        return edge;
    }
    
    public void takeSeeds(ArrayList<Seed> newSeeds) {
        for (Seed seed : newSeeds) {
            if (moveStatus == MoveStatus.IDLE && loadSeeds.size() == 0) {
                setMoveStatus(seed.isDown() ? MoveStatus.DOWN : MoveStatus.UP);
            }
            loadSeeds.add(seed);
            Statistic.onTakeSeed(seed);
            LogUtils.e(String.format("   [TAKE] #%d# take %s. at floor = %d. load = %d", id, seed.toDumpString(),
                    currentFloor, getCurrentLoad()));
        }
    }
    
    private void setMoveStatus(MoveStatus status) {
        if (moveStatus != status) {
            LogUtils.d(String.format("   [STATUS] #%d# from %s to %s, at floor = %d", id, moveStatus, status, currentFloor));
            moveStatus = status;
        }
    }

    public void gotoNextFloor() {
        if (moveStatus == MoveStatus.DOWN) {
            if (isReachBottom()) {
                setMoveStatus(MoveStatus.IDLE);
            } else {
                currentFloor--;
                totalStep++;
                totalLoad += getCurrentLoad();
                LogUtils.e(String.format("   [MOVE] #%d# move down arrive floor = %d. total_step = %d, total_load = %d. load = %d", id, currentFloor,
                        totalStep, totalLoad, getCurrentLoad()));
                Statistic.onMove(this);
            }
        } else if (moveStatus == MoveStatus.UP) {
            if (isReachTop()) {
                setMoveStatus(MoveStatus.IDLE);
            } else {
                currentFloor++;
                totalStep++;
                totalLoad += getCurrentLoad();
                LogUtils.e(String.format("   [MOVE] #%d# move up arrive floor = %d. total_step = %d, total_load = %d. load = %d", id, currentFloor,
                        totalStep, totalLoad, getCurrentLoad()));
                Statistic.onMove(this);
            }
        }
    }

    public void preSetActive(EdgeFloor seedsEdgeFloor) {
        if (getCurrentLoad() == 0) {
            if (currentFloor >= seedsEdgeFloor.getTop() || 
                    currentFloor <= seedsEdgeFloor.getBottom()) {
                setMoveStatus(MoveStatus.IDLE);
            } else if (moveStatus == MoveStatus.DOWN) {
                setMoveStatus(MoveStatus.PRE_DOWN);
            } else if (moveStatus == MoveStatus.UP) {
                setMoveStatus(MoveStatus.PRE_UP);
            }
        }
    }
    
    /**
     * 
     * @param seedAtFloor  seed 所在楼层
     * @param seedGotoFloor   seed 要去的楼层
     */
    public void setActive(int seedAtFloor, int seedGotoFloor) {
      boolean setted = false;
      if (moveStatus == MoveStatus.IDLE) {
          if (currentFloor != seedAtFloor) {
              setMoveStatus(currentFloor > seedAtFloor ? MoveStatus.DOWN : MoveStatus.UP);
              setted = true;
          } else {
              setMoveStatus(currentFloor > seedGotoFloor ? MoveStatus.DOWN : MoveStatus.UP);
              setted = true;
          }
      } else if (moveStatus == MoveStatus.PRE_DOWN) {
          if (currentFloor > seedAtFloor || 
                  (currentFloor == seedAtFloor && currentFloor > seedGotoFloor)) {
              setMoveStatus(MoveStatus.DOWN);
              setted = true;
          }
      } else if (moveStatus == MoveStatus.PRE_UP) {
          if (currentFloor < seedAtFloor || 
                  (currentFloor == seedAtFloor && currentFloor < seedGotoFloor)) {
              setMoveStatus(MoveStatus.UP);
              setted = true;
          }
      }

      if (setted) {
          LogUtils.d(String.format("   [ACTIVE] elevator #%d# from floor %d to %d", id, currentFloor, seedAtFloor));
      }
    }
    
    public void setActiveIdle() {
        if (getCurrentLoad() == 0 &&
                (moveStatus == MoveStatus.PRE_DOWN || moveStatus == MoveStatus.PRE_UP)) {
            setMoveStatus(MoveStatus.IDLE);
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

    public String toDumpString() {
        StringBuilder sb = new StringBuilder();
        String record = String.format("#%d#:[at floor %d %s, load = %d. totalStep = %d, totalLoad = %d]", id, currentFloor,
                moveStatus, getCurrentLoad(), totalStep, totalLoad);
        sb.append(record);
        sb.append("\n");
        sb.append(String.format("      [DUMP] [TAKEING] %d SEEDS: \n", getCurrentLoad()));
        for (Seed seed : loadSeeds) {
            sb.append("      [DUMP] [TAKEING] ");
            sb.append(seed.toDumpString());
            sb.append("\n");
        }
        return sb.toString();
    }

}
