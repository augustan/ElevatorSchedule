package com.aug.elevator.policy;

import com.aug.elevator.model.EdgeFloor;
import com.aug.elevator.model.Elevator;
import com.aug.elevator.model.Elevator.MoveStatus;
import com.aug.elevator.model.Seed;

import java.util.ArrayList;

public class StandardPolicy extends ElevatorPolicy {

    @Override
    public void preHandleSeeds(Elevator elevator, int seedAtFloor, 
            ArrayList<Seed> seedsList,
            EdgeFloor seedsEdgeFloor, EdgeFloor elevatorEdgeFloor) {
        if (seedsList.size() == 0) {
            return;
        }
        if (elevator.isOverLoad()) {
            return;
        }

        int loadSpace = elevator.getLoadSpace();
        int topFloor = Math.max(seedsEdgeFloor.getTop(), elevatorEdgeFloor.getTop());
        int bottomFloor = Math.min(seedsEdgeFloor.getBottom(), elevatorEdgeFloor.getBottom());
        
        for (int i = 0; 0 < loadSpace && i < seedsList.size(); i++) {
            // stepCost 不能只计算绝对值，要计算方向。
            // 如果电梯向上走，要走到最上面有人的楼层，再向下
            int stepCost = 0;
            
            Seed seed = seedsList.get(i);
            
            boolean elevatorIdle = elevator.getMoveStatus() == MoveStatus.IDLE;
            boolean elevatorGoUp = elevator.getMoveStatus() == MoveStatus.UP || elevator.getMoveStatus() == MoveStatus.PRE_UP;
            boolean elevatorGoDown = elevator.getMoveStatus() == MoveStatus.DOWN || elevator.getMoveStatus() == MoveStatus.PRE_DOWN;
            
            boolean sameDir = elevatorIdle || (elevatorGoUp && !seed.isDown()) || (elevatorGoDown && seed.isDown());
            if (sameDir) {
                stepCost = Math.abs(elevator.getCurrentFloor() - seedAtFloor);
            } else if (elevatorGoUp) {
                stepCost = Math.abs(elevator.getCurrentFloor() - topFloor);
                stepCost += Math.abs(topFloor - seedAtFloor);
            } else if (elevatorGoDown) {
                stepCost = Math.abs(elevator.getCurrentFloor() - bottomFloor);
                stepCost += Math.abs(bottomFloor - seedAtFloor);
            }
            
            seed.setMarkElevatorId(elevator.getId(), stepCost);
//            loadSpace--;
        }
    }

}
