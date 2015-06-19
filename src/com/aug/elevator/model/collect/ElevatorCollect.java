package com.aug.elevator.model.collect;

import com.aug.elevator.model.Elevator;

public class ElevatorCollect {
    
    private int collectSize = 0;
    private Elevator[] elevatorList;
    
    public ElevatorCollect(int elevatorcount) {
        collectSize = elevatorcount;
        elevatorList = new Elevator[collectSize];
        for (int i = 0; i < collectSize; i++) {
            elevatorList[i] = new Elevator(i + 1);
        }
    }
    
    public int getSize() {
        return collectSize;
    }
    
    public Elevator get(int index) {
        Elevator obj = null;
        if (0 <= index && index < collectSize) {
            obj = elevatorList[index];
        }
        return obj;
    }

}
