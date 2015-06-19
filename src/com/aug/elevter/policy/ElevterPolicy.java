package com.aug.elevter.policy;

import com.aug.elevter.model.Elevter;
import com.aug.elevter.model.Seed;

import java.util.ArrayList;

public abstract class ElevterPolicy {
    abstract public void preHandleSeeds(Elevter elevter, int floor,
            ArrayList<Seed> seedsListAt,
            int topFloor, int bottomFloor);
}
