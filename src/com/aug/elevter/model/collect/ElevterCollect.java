package com.aug.elevter.model.collect;

import com.aug.elevter.model.Elevter;

public class ElevterCollect {
    
    private int collectSize = 0;
    private Elevter[] elevterList;
    
    public ElevterCollect(int elevtercount) {
        collectSize = elevtercount;
        elevterList = new Elevter[collectSize];
        for (int i = 0; i < collectSize; i++) {
            elevterList[i] = new Elevter(i + 1);
        }
    }
    
    public int getSize() {
        return collectSize;
    }
    
    public Elevter get(int index) {
        Elevter obj = null;
        if (0 <= index && index < collectSize) {
            obj = elevterList[index];
        }
        return obj;
    }

}
