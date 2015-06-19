package com.aug.elevter.main;

import com.aug.elevter.tools.LogUtils;


public class ElevterMain {
    
    public static void main(String[] arg) {
        LogUtils.d("=== ElevterController start === ");
        ElevterController elevterCon = new ElevterController();
        elevterCon.init();
        elevterCon.start();
        synchronized (elevterCon) {
            try {
                elevterCon.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        LogUtils.d("=== ElevterController finish === ");
    }
}
