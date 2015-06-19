package com.aug.elevter.main;

import com.aug.elevter.model.EdgeSeedFloor;
import com.aug.elevter.model.Elevter;
import com.aug.elevter.model.Seed;
import com.aug.elevter.model.Statistic;
import com.aug.elevter.model.collect.ElevterCollect;
import com.aug.elevter.model.collect.SeedsOnFloorCollect;
import com.aug.elevter.tools.LogUtils;
import com.aug.elevter.tools.SeedsReader;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class ElevterController extends TimerTask {
    
    private Timer globalTimer;
    private SeedsReader reader = null;
    
    private ElevterCollect elevterCollect;
    private SeedsOnFloorCollect seedsOnFloorCollect;
    
    public void init() {
        reader = new SeedsReader();
        reader.init();
        globalTimer = new Timer(true);
        
        elevterCollect = new ElevterCollect(Constants.elevterCount);
        seedsOnFloorCollect = new SeedsOnFloorCollect(Constants.totalFloor);
    }
    
    public void start() {
        globalTimer.scheduleAtFixedRate(this, 0, Constants.timeSlot);
    }
    
    private boolean shouldContinue() {
        int waitingSeedCount = seedsOnFloorCollect.getWaitingSeedCount();
        return reader.getRemainSeedCount() > 0 || waitingSeedCount > 0 ||
        !isAllElevterIdle();
    }
    
    @Override
    public void run() {
        int timeCount = Statistic.onTimeLapse();
        LogUtils.d("run time = " + timeCount);
        if (timeCount == 40) {
            int debug = timeCount;
            debug++;
        }

        if (shouldContinue()) {
            int waitingSeedCount = seedsOnFloorCollect.getWaitingSeedCount();
            LogUtils.d("   process elevterCollect. wait count = " + waitingSeedCount);
            
            Seed seed = reader.getNext();
            if (seed != null) {
                onProcessSeed(seed);
            }
            onProcessElevterNextStep();
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

    private void onProcessElevterNextStep() {
        int elevterCount = elevterCollect.getSize();
        
        // 1. 清除电梯所在楼层seeds的stepCost
        seedsOnFloorCollect.clearAllStepCost();

        EdgeSeedFloor edgeFloor = new EdgeSeedFloor();
        seedsOnFloorCollect.getTopBottomSeedFloor(edgeFloor);
        
        // 2. 标记空载的电梯，如果没有目标seed，将要停下
        for (int i = 0; i < elevterCount; i++) {
            Elevter elevter = elevterCollect.get(i);
            elevter.preSetActive(edgeFloor);
        }
        
        // 3. 更新所有楼层seeds的stepCost
        for (int i = 0; i < elevterCount; i++) {
            Elevter elevter = elevterCollect.get(i);
            int totalFloorSize = seedsOnFloorCollect.getFloorSize();
            for (int floor = 0; floor < totalFloorSize; floor++) {
                elevter.preHandleSeeds(floor + 1, 
                        seedsOnFloorCollect.getSeedsListAt(floor),
                        edgeFloor.getTop(),
                        edgeFloor.getBottom());
            }
        }
        
//        int totalFloorSize = seedsOnFloorCollect.getFloorSize();
//        for (int floor = 0; floor < totalFloorSize; floor++) {
//            ArrayList<Seed> list = seedsOnFloorCollect.getSeedsListAt(floor);
//            for (Seed seed : list) {
//                int seedAtFloor = seed.getFloor();
//                int elevterId = seed.getMarkElevterId();
//                elevterId--;
//                if (elevterId < 0 || elevterId >= elevterCount) {
////                    LogUtils.e("!!! error !!! wrong elevterId = " + elevterId);
//                } else {
//                    Elevter elevter = elevterCollect.get(elevterId);
//                    elevter.setActive(seedAtFloor);
//                }
//            }
//        }
//        for (int i = 0; i < elevterCount; i++) {
//            Elevter elevter = elevterCollect.get(i);
//            elevter.checkActive(false);
//        }

        // 4. 停下空载的电梯
        for (int i = 0; i < elevterCount; i++) {
            Elevter elevter = elevterCollect.get(i);
            elevter.setActiveIdle();
        }
        
        // 5. 电梯载人，走向下一个楼层
        for (int i = 0; i < elevterCount; i++) {
            Elevter elevter = elevterCollect.get(i);
            int floor = elevter.getCurrentFloor();
            int id = elevter.getId();
            ArrayList<Seed> newSeeds = seedsOnFloorCollect.takeSeeds(floor - 1, id, elevter.getMoveStatus());
            
            elevter.takeSeeds(newSeeds);
            elevter.gotoNextFloor();
        }

        // 6. 电梯停在某楼层
        for (int i = 0; i < elevterCount; i++) {
            Elevter elevter = elevterCollect.get(i);
            elevter.onStopAtFloor();
        }
    }
    
    private boolean isAllElevterIdle() {
        int idleCnt = 0;
        int elevterCount = elevterCollect.getSize();
        for (int i = 0; i < elevterCount; i++) {
            Elevter elevter = elevterCollect.get(i);
            if (elevter.isIdle()) {
                idleCnt++;
            }
        }
        return idleCnt == elevterCount;
    }
    
//    private int getTotalElevterStep() {
//        int step = 0;
//        int elevterCount = elevterCollect.getSize();
//        for (int i = 0; i < elevterCount; i++) {
//            Elevter elevter = elevterCollect.get(i);
//            step += elevter.getTotalStep();
//        }
//        return step;
//    }
//
//    private int getTotalElevterLoad() {
//        int load = 0;
//        int elevterCount = elevterCollect.getSize();
//        for (int i = 0; i < elevterCount; i++) {
//            Elevter elevter = elevterCollect.get(i);
//            load += elevter.getTotalLoad();
//        }
//        return load;
//    }
    
    private void dumpCurrentStatus() {
        
    }
}
