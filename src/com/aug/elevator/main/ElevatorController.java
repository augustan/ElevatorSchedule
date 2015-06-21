package com.aug.elevator.main;

import com.aug.elevator.model.EdgeFloor;
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

public class ElevatorController {
    
    private boolean enableDumpAllStatus = true;

    private Timer globalTimer;
    private SeedsReader reader = null;

    private ElevatorCollect elevatorCollect;
    private SeedsOnFloorCollect seedsOnFloorCollect;
    
    private boolean runUntilFinish = true;
    private ElevatorRunCallback callback = null;

    private TimerTask stepTimerTask = new TimerTask() {
        @Override
        public void run() {
            runOneStep();
        }
    };

    public void init(boolean runUntilFinish, ElevatorRunCallback callback) {
        this.runUntilFinish = runUntilFinish;
        this.callback = callback;

        reader = new SeedsReader();
        reader.init();

        elevatorCollect = new ElevatorCollect(Constants.elevatorCount);
        seedsOnFloorCollect = new SeedsOnFloorCollect(Constants.totalFloor);
    }

    public void start() {
        if (runUntilFinish) {
            if (globalTimer == null) {
                globalTimer = new Timer(true);
                globalTimer.scheduleAtFixedRate(stepTimerTask, 0, Constants.timeSlot);
            }
        } else {
            runOneStep();
        }
    }

    private boolean runOneStep() {
        boolean finish = false;
        int timeCount = Statistic.onTimeLapse();
        LogUtils.d("run time = " + timeCount);
        if (timeCount == 216) {
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

            if (callback != null) {
                callback.onOneStep();
            }
        } else {
            finish = true;
            onFinish();
        }
        return finish;
    }

    private void onFinish() {
        Statistic.showResule();
        
        if (globalTimer != null) {
            globalTimer.cancel();
            globalTimer = null;
        }

        if (callback != null) {
            callback.onFinish();
        }
    }

    private boolean shouldContinue() {
        int waitingSeedCount = seedsOnFloorCollect.getWaitingSeedCount();
        return reader.getRemainSeedCount() > 0 || waitingSeedCount > 0 || !isAllElevatorIdle();
    }

    private void onProcessSeed(Seed seed) {
        Statistic.onShowNewSeed(seed);
        LogUtils.d("   [new seed] = " + seed.toDumpString());
        seedsOnFloorCollect.add(seed.getFloor() - 1, seed);
    }

    private void onProcessElevatorNextStep() {
        int elevatorCount = elevatorCollect.getSize();

        // 1. 清除电梯所在楼层seeds的stepCost
        LogUtils.d("   [CONTROL] clear all seeds' ElevatorId");
        seedsOnFloorCollect.clearAllStepCost();

        EdgeFloor seedsEdgeFloor = seedsOnFloorCollect.getTopBottomSeedFloor();

        // 2. 标记空载的电梯，如果没有目标seed，将要停下
        for (int i = 0; i < elevatorCount; i++) {
            Elevator elevator = elevatorCollect.get(i);
            elevator.preSetActive(seedsEdgeFloor);
        }

        // 3. 更新所有楼层seeds的stepCost
        for (int i = 0; i < elevatorCount; i++) {
            Elevator elevator = elevatorCollect.get(i);
            int totalFloorSize = seedsOnFloorCollect.getFloorSize();
            for (int floor = 0; floor < totalFloorSize; floor++) {
                elevator.preHandleSeeds(floor + 1, seedsOnFloorCollect.getSeedsListAt(floor), seedsEdgeFloor);
            }
        }

        // 4. 遍历所有seed，启动有相应标记的电梯
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
        
        dumpCurrentStatus();

        // 6. 电梯载人，走向下一个楼层
        for (int i = 0; i < elevatorCount; i++) {
            Elevator elevator = elevatorCollect.get(i);
            int floor = elevator.getCurrentFloor();
            int id = elevator.getId();
            ArrayList<Seed> newSeeds = seedsOnFloorCollect.takeSeeds(floor - 1, id, elevator.getMoveStatus());

            elevator.takeSeeds(newSeeds);
            elevator.gotoNextFloor();
        }

        Statistic.addTimeTick();
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
        if (enableDumpAllStatus) {
            LogUtils.e("[DUMP START] =================== [DUMP START]");
            
            int elevatorCount = elevatorCollect.getSize();
            for (int id = 0; id < elevatorCount; id++) {
                Elevator elevator = elevatorCollect.get(id);
                LogUtils.e("   [DUMP] [ELEVATOR] " + elevator.toDumpString());
            }
            
            int totalFloorSize = seedsOnFloorCollect.getFloorSize();
            for (int floor = 0; floor < totalFloorSize; floor++) {
                ArrayList<Seed> list = seedsOnFloorCollect.getSeedsListAt(floor);
                for (Seed seed : list) {
                    LogUtils.e("   [DUMP] [WAITING SEED] " + seed.toDumpString());
                }
            }

            LogUtils.e("[DUMP END] =================== [DUMP END]");
        }
    }
}
