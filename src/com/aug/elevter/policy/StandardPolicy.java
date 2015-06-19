package com.aug.elevter.policy;

import com.aug.elevter.model.Elevter;
import com.aug.elevter.model.Elevter.MoveStatus;
import com.aug.elevter.model.Seed;

import java.util.ArrayList;

public class StandardPolicy extends ElevterPolicy {

    @Override
    public void takeSeed(Elevter elevter, int floor, ArrayList<Seed> seedsList) {
        if (seedsList.size() == 0) {
            return;
        }
        if (elevter.isOverLoad()) {
            return;
        }

        if (elevter.getMoveStatus() == MoveStatus.UP && elevter.getCurrentFloor() > floor) {
            return;
        }
        if (elevter.getMoveStatus() == MoveStatus.DOWN && elevter.getCurrentFloor() < floor) {
            return;
        }

        int stepCost = Math.abs(elevter.getCurrentFloor() - floor);
        int loadSpace = elevter.getLoadSpace();
        for (int i = 0; 0 < loadSpace && i < seedsList.size(); i++) {
            Seed seed = seedsList.get(i);
            if (elevter.getMoveStatus() == MoveStatus.IDLE) {
                elevter.setActive(floor);
                seed.setMarkElevterId(elevter.getId(), stepCost);
                loadSpace--;
            } else if (elevter.getMoveStatus() == MoveStatus.DOWN) {
                if (seed.isDown()) {
                    seed.setMarkElevterId(elevter.getId(), stepCost);
                    loadSpace--;
                }
            } else if (elevter.getMoveStatus() == MoveStatus.UP) {
                if (!seed.isDown()) {
                    seed.setMarkElevterId(elevter.getId(), stepCost);
                    loadSpace--;
                }
            }
        }
    }

}
