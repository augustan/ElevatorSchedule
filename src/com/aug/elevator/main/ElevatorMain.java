package com.aug.elevator.main;

import com.aug.elevator.tools.LogUtils;


public class ElevatorMain {
    
    public static void main(String[] arg) {
        LogUtils.d("=== ElevatorController start === ");
        ElevatorController elevatorCon = new ElevatorController();
        elevatorCon.init();
        elevatorCon.start();
        synchronized (elevatorCon) {
            try {
                elevatorCon.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        LogUtils.d("=== ElevatorController finish === ");
    }
}
