package com.aug.elevtor.model.collect;

import com.aug.elevtor.model.Elevtor;

public class ElevtorCollect {
    
    private int collectSize = 0;
    private Elevtor[] elevtorList;
    
    public ElevtorCollect(int elevtorcount) {
        collectSize = elevtorcount;
        elevtorList = new Elevtor[collectSize];
        for (int i = 0; i < collectSize; i++) {
            elevtorList[i] = new Elevtor(i + 1);
        }
    }
    
    public int getSize() {
        return collectSize;
    }
    
    public Elevtor get(int index) {
        Elevtor obj = null;
        if (0 <= index && index < collectSize) {
            obj = elevtorList[index];
        }
        return obj;
    }

}
