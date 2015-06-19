package com.aug.elevator.main;

import com.aug.elevator.model.EdgeSeedFloor;
import com.aug.elevator.model.Elevator;
import com.aug.elevator.model.Seed;
import com.aug.elevator.model.Statistic;
import com.aug.elevator.model.collect.ElevatorCollect;
import com.aug.elevator.model.collect.SeedsOnFloorCollect;
import com.aug.elevator.tools.LogUtils;
import com.aug.elevator.tools.SeedsReader;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class ElevatorController extends TimerTask {
    
    private Timer globalTimer;
    private SeedsReader reader = null;
    
    private ElevatorCollect elevatorCollect;
    private SeedsOnFloorCollect seedsOnFloorCollect;
    
    public void init() {
        reader = new SeedsReader();
        reader.init();
        globalTimer = new Timer(true);
        
        elevatorCollect = new ElevatorCollect(Constants.elevatorCount);
        seedsOnFloorCollect = new SeedsOnFloorCollect(Constants.totalFloor);
    }
    
    public void start() {
        globalTimer.scheduleAtFixedRate(this, 0, Constants.timeSlot);
    }
    
    private boolean shouldContinue() {
        int waitingSeedCount = seedsOnFloorCollect.getWaitingSeedCount();
        return reader.getRemainSeedCount() > 0 || waitingSeedCount > 0 ||
        !isAllElevatorIdle();
    }
    
    @Override
    public void run() {
        int timeCount = Statistic.onTimeLapse();
        LogUtils.d("run time = " + timeCount);
        if (timeCount == 18) {
            int debug = timeCount;
            debug++;
        }

        if (shouldContinue()) {
            int waitingSeedCount = seedsOnFloorCollect.getWaitingSeedCount();
            LogUtils.d("   process elevatorCollect. wait count = " + waitingSeedCount);
            
            Seed seed = reader.getNext();
            if (seed != null) {
                onProcessSeed(seed);
            }
            onProcessElevatorNextStep();
            dumpCurrentStatus();
        } else {
            globalTimer.cancel();
            Statistic.showResule();
            synchronized (this) {
                this.notifyAll();
            }
        }
    }

    private void onProcessSeed(Seed seed) {
        Statistic.onShowNewSeed(seed);
        LogUtils.d("   [new seed] = " + seed.toDumpString());
        seedsOnFloorCollect.add(seed.getFloor() - 1, seed);
    }

    private void onProcessElevatorNextStep() {
        int elevatorCount = elevatorCollect.getSize();
        
        // 1. 清除电梯所在楼层seeds的stepCost
        seedsOnFloorCollect.clearAllStepCost();

        EdgeSeedFloor edgeFloor = new EdgeSeedFloor();
        seedsOnFloorCollect.getTopBottomSeedFloor(edgeFloor);
        
        // 2. 标记空载的电梯，如果没有目标seed，将要停下
        for (int i = 0; i < elevatorCount; i++) {
            Elevator elevator = elevatorCollect.get(i);
            elevator.preSetActive(edgeFloor);
        }
        
        // 3. 更新所有楼层seeds的stepCost
        for (int i = 0; i < elevatorCount; i++) {
            Elevator elevator = elevatorCollect.get(i);
            int totalFloorSize = seedsOnFloorCollect.getFloorSize();
            for (int floor = 0; floor < totalFloorSize; floor++) {
                elevator.preHandleSeeds(floor + 1, 
                        seedsOnFloorCollect.getSeedsListAt(floor),
                        edgeFloor.getTop(),
                        edgeFloor.getBottom());
            }
        }
        
        // 4. 便利所有seed，启动有相应标记的电梯
        int totalFloorSize = seedsOnFloorCollect.getFloorSize();
        for (int floor = 0; floor < totalFloorSize; floor++) {
            ArrayList<Seed> list = seedsOnFloorCollect.getSeedsListAt(floor);
            for (Seed seed : list) {
                int elevatorId = seed.getMarkElevatorId();
                elevatorId--;
                if (elevatorId < 0 || elevatorId >= elevatorCount) {
//                    LogUtils.e("!!! error !!! wrong elevatorId = " + elevatorId);
                } else {
                    Elevator elevator = elevatorCollect.get(elevatorId);
                    elevator.setActive(seed.getFloor(), seed.getToFloor());
                }
            }
        }

        // 5. 停下空载的电梯
        for (int i = 0; i < elevatorCount; i++) {
            Elevator elevator = elevatorCollect.get(i);
            elevator.setActiveIdle();
        }
        
        // 6. 电梯载人，走向下一个楼层
        for (int i = 0; i < elevatorCount; i++) {
            Elevator elevator = elevatorCollect.get(i);
            int floor = elevator.getCurrentFloor();
            int id = elevator.getId();
            ArrayList<Seed> newSeeds = seedsOnFloorCollect.takeSeeds(floor - 1, id, elevator.getMoveStatus());
            
            elevator.takeSeeds(newSeeds);
            elevator.gotoNextFloor();
        }

        // 7. 电梯停在某楼层
        for (int i = 0; i < elevatorCount; i++) {
            Elevator elevator = elevatorCollect.get(i);
            elevator.onStopAtFloor();
        }
    }
    
    private boolean isAllElevatorIdle() {
        int idleCnt = 0;
        int elevatorCount = elevatorCollect.getSize();
        for (int i = 0; i < elevatorCount; i++) {
            Elevator elevator = elevatorCollect.get(i);
            if (elevator.isIdle()) {
                idleCnt++;
            }
        }
        return idleCnt == elevatorCount;
    }
    
//    private int getTotalElevatorStep() {
//        int step = 0;
//        int elevatorCount = elevatorCollect.getSize();
//        for (int i = 0; i < elevatorCount; i++) {
//            Elevator elevator = elevatorCollect.get(i);
//            step += elevator.getTotalStep();
//        }
//        return step;
//    }
//
//    private int getTotalElevatorLoad() {
//        int load = 0;
//        int elevatorCount = elevatorCollect.getSize();
//        for (int i = 0; i < elevatorCount; i++) {
//            Elevator elevator = elevatorCollect.get(i);
//            load += elevator.getTotalLoad();
//        }
//        return load;
//    }
    
    private void dumpCurrentStatus() {
        
    }
}
