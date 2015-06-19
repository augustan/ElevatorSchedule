package com.aug.elevter.policy;

import com.aug.elevter.model.Elevter;
import com.aug.elevter.model.Elevter.MoveStatus;
import com.aug.elevter.model.Seed;

import java.util.ArrayList;

public class StandardPolicy extends ElevterPolicy {

    @Override
    public void preHandleSeeds(Elevter elevter, int floor, 
            ArrayList<Seed> seedsList,
            int topFloor, int bottomFloor) {
        if (seedsList.size() == 0) {
            return;
        }
        if (elevter.isOverLoad()) {
            return;
        }

        
        int loadSpace = elevter.getLoadSpace();
        for (int i = 0; 0 < loadSpace && i < seedsList.size(); i++) {
            // stepCost 不能只计算绝对值，要计算方向。
            // 如果电梯向上走，要走到最上面有人的楼层，再向下
            int stepCost = 0;
            
            Seed seed = seedsList.get(i);
            elevter.setActive(floor, seed.getToFloor());
            
            boolean elevterIdle = elevter.getMoveStatus() == MoveStatus.IDLE;
            boolean elevterGoUp = elevter.getMoveStatus() == MoveStatus.UP || elevter.getMoveStatus() == MoveStatus.PRE_UP;
            boolean elevterGoDown = elevter.getMoveStatus() == MoveStatus.DOWN || elevter.getMoveStatus() == MoveStatus.PRE_DOWN;
            
            boolean sameDir = elevterIdle || (elevterGoUp && !seed.isDown()) || (elevterGoDown && seed.isDown());
            if (sameDir) {
                stepCost = Math.abs(elevter.getCurrentFloor() - floor);
            } else if (elevterGoUp){
                stepCost = Math.abs(elevter.getCurrentFloor() - topFloor);
                stepCost += Math.abs(topFloor - floor);
            } else if (elevterGoDown) {
                stepCost = Math.abs(elevter.getCurrentFloor() - bottomFloor);
                stepCost += Math.abs(bottomFloor - floor);
            }
            seed.setMarkElevterId(elevter.getId(), stepCost);
            loadSpace--;
        }
    }

}
