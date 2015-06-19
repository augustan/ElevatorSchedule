package com.aug.elevtor.model.collect;

import com.aug.elevtor.model.EdgeSeedFloor;
import com.aug.elevtor.model.Seed;
import com.aug.elevtor.model.Elevtor.MoveStatus;

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
    
    public ArrayList<Seed> takeSeeds(int floor, int elevtorId, MoveStatus moveStatus) {

        boolean elevtorIdle = moveStatus == MoveStatus.IDLE;
        boolean elevtorGoUp = moveStatus == MoveStatus.UP;
        boolean elevtorGoDown = moveStatus == MoveStatus.DOWN;
        
        ArrayList<Seed> taken = new ArrayList<Seed>();
        ArrayList<Seed> list = getSeedsListAt(floor);
        for (int i = list.size() - 1; i >= 0; i--) {
            Seed seed = list.get(i);
            boolean sameDir = elevtorIdle || (elevtorGoUp && !seed.isDown()) || (elevtorGoDown && seed.isDown());
            if (seed.getMarkElevtorId() == elevtorId && sameDir) {
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
            seed.clearMarkElevtorId();
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
