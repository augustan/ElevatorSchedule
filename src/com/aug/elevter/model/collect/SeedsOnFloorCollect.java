package com.aug.elevter.model.collect;

import com.aug.elevter.model.Elevter;
import com.aug.elevter.model.Seed;

import java.util.ArrayList;

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
    
    public ArrayList<Seed> takeSeeds(int floor, int elevterId) {
        ArrayList<Seed> taken = new ArrayList<Seed>();
        ArrayList<Seed> list = getSeedsListAt(floor);
        for (int i = list.size() - 1; i >= 0; i--) {
            Seed seed = list.get(i);
            if (seed.getMarkElevterId() == elevterId) {
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
            seed.clearMarkElevterId();
        }
    }
}
