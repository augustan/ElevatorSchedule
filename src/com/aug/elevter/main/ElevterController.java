package com.aug.elevter.main;

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

        int waitingSeedCount = seedsOnFloorCollect.getWaitingSeedCount();
        LogUtils.d("   process elevterCollect. wait count = " + waitingSeedCount);
        
        if (shouldContinue()) {
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
        
        // 1. 电梯停在某楼层
        for (int i = 0; i < elevterCount; i++) {
            Elevter elevter = elevterCollect.get(i);
            elevter.onStopAtFloor();
        }
        
//        // 2. 获取所有电梯所在楼层
//        HashMap<Integer, Object> elevterAtFloor = new HashMap<Integer, Object>();
//        for (int i = 0; i < elevterCount; i++) {
//            Elevter elevter = elevterCollect.get(i);
//            elevterAtFloor.put(elevter.getCurrentFloor(), new Object());
//        }
        
        // 3. 清除电梯所在楼层seeds的stepCost
//        for (Integer floor : elevterAtFloor.keySet()) {
//            resetStepCost(seedsOnFloorCollect.getSeedsListAt(floor));
//        }
        seedsOnFloorCollect.clearAllStepCost();
        
        // 4. 更新电梯所在楼层seeds的stepCost
//        for (int i = 0; i < elevterCount; i++) {
//            Elevter elevter = elevterCollect.get(i);
//            for (Integer floor : elevterAtFloor.keySet()) {
//                elevter.preHandleSeeds(floor, seedsOnFloorCollect.getSeedsListAt(floor));
//            }
//        }
        // 4. 更新所有楼层seeds的stepCost
        for (int i = 0; i < elevterCount; i++) {
            Elevter elevter = elevterCollect.get(i);
            int totalFloorSize = seedsOnFloorCollect.getFloorSize();
            for (int floor = 0; floor < totalFloorSize; floor++) {
                elevter.preHandleSeeds(floor + 1, seedsOnFloorCollect.getSeedsListAt(floor));
            }
        }
        
        // 5. 检查seeds，如果对应的电梯是idle状态，把电梯启动
        for (int i = 0; i < elevterCount; i++) {
            Elevter elevter = elevterCollect.get(i);
            elevter.checkActive(true);
        }
        int totalFloorSize = seedsOnFloorCollect.getFloorSize();
        for (int floor = 0; floor < totalFloorSize; floor++) {
            ArrayList<Seed> list = seedsOnFloorCollect.getSeedsListAt(floor);
            for (Seed seed : list) {
                int seedAtFloor = seed.getFloor();
                int elevterId = seed.getMarkElevterId();
                elevterId--;
                if (elevterId < 0 || elevterId >= elevterCount) {
//                    LogUtils.e("!!! error !!! wrong elevterId = " + elevterId);
                } else {
                    Elevter elevter = elevterCollect.get(elevterId);
                    elevter.setActive(seedAtFloor);
                }
            }
        }
        for (int i = 0; i < elevterCount; i++) {
            Elevter elevter = elevterCollect.get(i);
            elevter.checkActive(false);
        }
        
        // 6. 电梯载人，走向下一个楼层
        for (int i = 0; i < elevterCount; i++) {
            Elevter elevter = elevterCollect.get(i);
            int floor = elevter.getCurrentFloor();
            int id = elevter.getId();
            ArrayList<Seed> newSeeds = seedsOnFloorCollect.takeSeeds(floor - 1, id);
            
            elevter.takeSeeds(newSeeds);
            elevter.gotoNextFloor();
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
