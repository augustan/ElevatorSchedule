package com.aug.elevator.model.collect;

import com.aug.elevator.model.EdgeSeedFloor;
import com.aug.elevator.model.Seed;
import com.aug.elevator.model.Elevator.MoveStatus;

import java.util.ArrayList;

/**
 * 楼层数从0开始
 *
 */
public class SeedsOnFloorCollect {

    private int floorSize = 0;
    private ArrayList<Seed> [] seedsList;

    @SuppressWarnings("unchecked")
    public SeedsOnFloorCollect(int totalfloor) {
        floorSize = totalfloor;
        seedsList = new ArrayList[floorSize];
        for (int i = 0; i < floorSize; i++) {
            seedsList[i] = new ArrayList<Seed>();
        }
    }
    
    public void add(int floor, Seed seed) {
        ArrayList<Seed> list = getSeedsListAt(floor);
        list.add(seed);
    }
    
    public ArrayList<Seed> takeSeeds(int floor, int elevatorId, MoveStatus moveStatus) {

        boolean elevatorIdle = moveStatus == MoveStatus.IDLE;
        boolean elevatorGoUp = moveStatus == MoveStatus.UP;
        boolean elevatorGoDown = moveStatus == MoveStatus.DOWN;
        
        ArrayList<Seed> taken = new ArrayList<Seed>();
        ArrayList<Seed> list = getSeedsListAt(floor);
        for (int i = list.size() - 1; i >= 0; i--) {
            Seed seed = list.get(i);
            boolean sameDir = elevatorIdle || (elevatorGoUp && !seed.isDown()) || (elevatorGoDown && seed.isDown());
            if (seed.getMarkElevatorId() == elevatorId && sameDir) {
                taken.add(list.remove(i));
            }
        }
        return taken;
    }
    
    public ArrayList<Seed> getSeedsListAt(int floor) {
        ArrayList<Seed> list = null;
        if (0 <= floor && floor < floorSize) {
            list = seedsList[floor];
        }
        return list;
    }
    
    public void clearAllStepCost() {
        for (int i = 0; i < floorSize; i++) {
            resetStepCost(getSeedsListAt(i));
        }
    }
    
    public int getFloorSize() {
        return floorSize;
    }
    
    public int getWaitingSeedCount() {
        int cnt = 0;
        for (int i = 0; i < seedsList.length; i++) {
            cnt += getSeedsListAt(i).size();
        }
        return cnt;
    }

    private void resetStepCost(ArrayList<Seed> list) {
        for (Seed seed : list) {
            seed.clearMarkElevatorId();
        }
    }
    
    public void getTopBottomSeedFloor(EdgeSeedFloor edgeFloor) {
        for (int i = 0; i < floorSize; i++) {
            if (getSeedsListAt(i).size() > 0) {
                edgeFloor.setFloor(i + 1);
            }
        }
    }
}
