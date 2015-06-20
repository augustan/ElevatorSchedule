package com.aug.elevator.main;

import com.aug.elevator.tools.LogUtils;


public class ElevatorMain {
    
    private static Thread runThread = new Thread(new Runnable() {
        
        private Object waitObj = new Object();

        ElevatorRunCallback callack = new ElevatorRunCallback() {
            
            @Override
            public void onOneStep() {
            }
            
            @Override
            public void onFinish() {
                synchronized (waitObj) {
                    waitObj.notifyAll();
                }
            }
        };
        
        @Override
        public void run() {
            LogUtils.d("=== ElevatorController start === ");
            ElevatorController elevatorCon = new ElevatorController();
            elevatorCon.init(true, callack);
            elevatorCon.start();
            
            synchronized (waitObj) {
                try {
                    waitObj.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            synchronized (this) {
                this.notify();
            }
            LogUtils.d("=== ElevatorController finish === ");
        }
    }, "ElevatorController");
    
    public static void main(String[] arg) {
        runThread.start();
        synchronized (runThread) {
            try {
                runThread.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        LogUtils.d("=== main exit === ");
    }
}
