package com.aug.elevator.tools;

import com.aug.elevator.main.Constants;
import com.aug.elevator.model.Seed;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class SeedsReader {
    
    private ArrayList<Seed> seedsList = null;
    private int currentIndex = 0;
    
    public void init() {
        seedsList = new ArrayList<Seed>();
        BufferedReader reader = null;
        try {
            FileReader fileReader = null;
            fileReader = new FileReader(Constants.seedFilePath);
            reader = new BufferedReader(fileReader);
            String record = reader.readLine();
            while (record != null) {
                if (record.startsWith("#")) {
                    record = reader.readLine();
                    continue;
                }
                Seed seed = Seed.parse(record);
                if (seed != null) {
                    seedsList.add(seed);
                }
                record = reader.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    public Seed getNext() {
        Seed seed = getCurrentSeed();
        if (seed != null) {
            int wait = seed.getWaitTime() - 1;
            if (wait > 0) {
                seed.setWaitTime(wait);
                seed = null;
            } else {
                currentIndex++;
            }
        }
        return seed;
    }
    
    public int getRemainSeedCount() {
        int cnt = seedsList.size() - currentIndex;
        return cnt > 0 ? cnt : 0;
    }
    
    private Seed getCurrentSeed() {
        Seed seed = null;
        if (0 <= currentIndex && currentIndex < seedsList.size()) {
            seed = seedsList.get(currentIndex);
        }
        return seed;
    }
}
