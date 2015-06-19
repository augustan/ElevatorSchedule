package com.aug.elevtor.policy;

import com.aug.elevtor.model.Elevtor;
import com.aug.elevtor.model.Seed;

import java.util.ArrayList;

public abstract class ElevtorPolicy {
    abstract public void preHandleSeeds(Elevtor elevtor, int floor,
            ArrayList<Seed> seedsListAt,
            int topFloor, int bottomFloor);
}
