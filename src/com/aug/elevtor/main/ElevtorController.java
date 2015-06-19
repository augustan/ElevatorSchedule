package com.aug.elevtor.main;

import com.aug.elevtor.model.EdgeSeedFloor;
import com.aug.elevtor.model.Elevtor;
import com.aug.elevtor.model.Seed;
import com.aug.elevtor.model.Statistic;
import com.aug.elevtor.model.collect.ElevtorCollect;
import com.aug.elevtor.model.collect.SeedsOnFloorCollect;
import com.aug.elevtor.tools.LogUtils;
import com.aug.elevtor.tools.SeedsReader;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class ElevtorController extends TimerTask {
    
    private Timer globalTimer;
    private SeedsReader reader = null;
    
    private ElevtorCollect elevtorCollect;
    private SeedsOnFloorCollect seedsOnFloorCollect;
    
    public void init() {
        reader = new SeedsReader();
        reader.init();
        globalTimer = new Timer(true);
        
        elevtorCollect = new ElevtorCollect(Constants.elevtorCount);
        seedsOnFloorCollect = new SeedsOnFloorCollect(Constants.totalFloor);
    }
    
    public void start() {
        globalTimer.scheduleAtFixedRate(this, 0, Constants.timeSlot);
    }
    
    private boolean shouldContinue() {
        int waitingSeedCount = seedsOnFloorCollect.getWaitingSeedCount();
        return reader.getRemainSeedCount() > 0 || waitingSeedCount > 0 ||
        !isAllElevtorIdle();
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
            LogUtils.d("   process elevtorCollect. wait count = " + waitingSeedCount);
            
            Seed seed = reader.getNext();
            if (seed != null) {
                onProcessSeed(seed);
            }
            onProcessElevtorNextStep();
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

    private void onProcessElevtorNextStep() {
        int elevtorCount = elevtorCollect.getSize();
        
        // 1. 清除电梯所在楼层seeds的stepCost
        seedsOnFloorCollect.clearAllStepCost();

        EdgeSeedFloor edgeFloor = new EdgeSeedFloor();
        seedsOnFloorCollect.getTopBottomSeedFloor(edgeFloor);
        
        // 2. 标记空载的电梯，如果没有目标seed，将要停下
        for (int i = 0; i < elevtorCount; i++) {
            Elevtor elevtor = elevtorCollect.get(i);
            elevtor.preSetActive(edgeFloor);
        }
        
        // 3. 更新所有楼层seeds的stepCost
        for (int i = 0; i < elevtorCount; i++) {
            Elevtor elevtor = elevtorCollect.get(i);
            int totalFloorSize = seedsOnFloorCollect.getFloorSize();
            for (int floor = 0; floor < totalFloorSize; floor++) {
                elevtor.preHandleSeeds(floor + 1, 
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
//                int elevtorId = seed.getMarkElevtorId();
//                elevtorId--;
//                if (elevtorId < 0 || elevtorId >= elevtorCount) {
////                    LogUtils.e("!!! error !!! wrong elevtorId = " + elevtorId);
//                } else {
//                    Elevtor elevtor = elevtorCollect.get(elevtorId);
//                    elevtor.setActive(seedAtFloor);
//                }
//            }
//        }
//        for (int i = 0; i < elevtorCount; i++) {
//            Elevtor elevtor = elevtorCollect.get(i);
//            elevtor.checkActive(false);
//        }

        // 4. 停下空载的电梯
        for (int i = 0; i < elevtorCount; i++) {
            Elevtor elevtor = elevtorCollect.get(i);
            elevtor.setActiveIdle();
        }
        
        // 5. 电梯载人，走向下一个楼层
        for (int i = 0; i < elevtorCount; i++) {
            Elevtor elevtor = elevtorCollect.get(i);
            int floor = elevtor.getCurrentFloor();
            int id = elevtor.getId();
            ArrayList<Seed> newSeeds = seedsOnFloorCollect.takeSeeds(floor - 1, id, elevtor.getMoveStatus());
            
            elevtor.takeSeeds(newSeeds);
            elevtor.gotoNextFloor();
        }

        // 6. 电梯停在某楼层
        for (int i = 0; i < elevtorCount; i++) {
            Elevtor elevtor = elevtorCollect.get(i);
            elevtor.onStopAtFloor();
        }
    }
    
    private boolean isAllElevtorIdle() {
        int idleCnt = 0;
        int elevtorCount = elevtorCollect.getSize();
        for (int i = 0; i < elevtorCount; i++) {
            Elevtor elevtor = elevtorCollect.get(i);
            if (elevtor.isIdle()) {
                idleCnt++;
            }
        }
        return idleCnt == elevtorCount;
    }
    
//    private int getTotalElevtorStep() {
//        int step = 0;
//        int elevtorCount = elevtorCollect.getSize();
//        for (int i = 0; i < elevtorCount; i++) {
//            Elevtor elevtor = elevtorCollect.get(i);
//            step += elevtor.getTotalStep();
//        }
//        return step;
//    }
//
//    private int getTotalElevtorLoad() {
//        int load = 0;
//        int elevtorCount = elevtorCollect.getSize();
//        for (int i = 0; i < elevtorCount; i++) {
//            Elevtor elevtor = elevtorCollect.get(i);
//            load += elevtor.getTotalLoad();
//        }
//        return load;
//    }
    
    private void dumpCurrentStatus() {
        
    }
}
