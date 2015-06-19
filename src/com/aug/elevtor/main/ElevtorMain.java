package com.aug.elevtor.main;

import com.aug.elevtor.tools.LogUtils;


public class ElevtorMain {
    
    public static void main(String[] arg) {
        LogUtils.d("=== ElevtorController start === ");
        ElevtorController elevtorCon = new ElevtorController();
        elevtorCon.init();
        elevtorCon.start();
        synchronized (elevtorCon) {
            try {
                elevtorCon.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        LogUtils.d("=== ElevtorController finish === ");
    }
}
