package com.aug.elevator.policy;

import com.aug.elevator.model.EdgeFloor;
import com.aug.elevator.model.Elevator;
import com.aug.elevator.model.Seed;

import java.util.ArrayList;

public abstract class ElevatorPolicy {
    abstract public void preHandleSeeds(Elevator elevator, int seedAtFloor, 
            ArrayList<Seed> seedsListAt,
            EdgeFloor seedsEdgeFloor, EdgeFloor elevatorEdgeFloor);
}
