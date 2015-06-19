package com.aug.elevtor.policy;

import com.aug.elevtor.model.Elevtor;
import com.aug.elevtor.model.Seed;
import com.aug.elevtor.model.Elevtor.MoveStatus;

import java.util.ArrayList;

public class StandardPolicy extends ElevtorPolicy {

    @Override
    public void preHandleSeeds(Elevtor elevtor, int floor, 
            ArrayList<Seed> seedsList,
            int topFloor, int bottomFloor) {
        if (seedsList.size() == 0) {
            return;
        }
        if (elevtor.isOverLoad()) {
            return;
        }

        
        int loadSpace = elevtor.getLoadSpace();
        for (int i = 0; 0 < loadSpace && i < seedsList.size(); i++) {
            // stepCost 不能只计算绝对值，要计算方向。
            // 如果电梯向上走，要走到最上面有人的楼层，再向下
            int stepCost = 0;
            
            Seed seed = seedsList.get(i);
            elevtor.setActive(floor, seed.getToFloor());
            
            boolean elevtorIdle = elevtor.getMoveStatus() == MoveStatus.IDLE;
            boolean elevtorGoUp = elevtor.getMoveStatus() == MoveStatus.UP || elevtor.getMoveStatus() == MoveStatus.PRE_UP;
            boolean elevtorGoDown = elevtor.getMoveStatus() == MoveStatus.DOWN || elevtor.getMoveStatus() == MoveStatus.PRE_DOWN;
            
            boolean sameDir = elevtorIdle || (elevtorGoUp && !seed.isDown()) || (elevtorGoDown && seed.isDown());
            if (sameDir) {
                stepCost = Math.abs(elevtor.getCurrentFloor() - floor);
            } else if (elevtorGoUp){
                stepCost = Math.abs(elevtor.getCurrentFloor() - topFloor);
                stepCost += Math.abs(topFloor - floor);
            } else if (elevtorGoDown) {
                stepCost = Math.abs(elevtor.getCurrentFloor() - bottomFloor);
                stepCost += Math.abs(bottomFloor - floor);
            }
            seed.setMarkElevtorId(elevtor.getId(), stepCost);
            loadSpace--;
        }
    }

}
