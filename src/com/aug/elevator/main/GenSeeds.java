package com.aug.elevator.main;

import com.aug.elevator.model.Seed;

import java.io.FileOutputStream;
import java.io.IOException;

public class GenSeeds {

    public static void main(String[] arg) {
        FileOutputStream fo = null;
        try {
            fo = new FileOutputStream(Constants.seedFilePath);
            fo.write("#id, 楼层, 上/下按钮, 要去几层, 本条数据生效的间隔时间\n".getBytes());
            
            for (int i = 0; i < Constants.recordCount; i++) {
                Seed seed = new Seed(i + 1);
                fo.write(seed.toString().getBytes());
                fo.write("\n".getBytes());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fo != null) {
                try {
                    fo.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
