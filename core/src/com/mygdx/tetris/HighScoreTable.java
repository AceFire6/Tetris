package com.mygdx.tetris;


import java.util.Arrays;
import java.util.Hashtable;

public class HighScoreTable extends Hashtable<String, Integer> {

    public HighScoreTable() {
        super();
    }

    @Override
    public String toString() {
        String highScoreString = "";
        while (this.keys().hasMoreElements()) {
            String currentKey = this.keys().nextElement();
            highScoreString += currentKey;
            for (int i = 0; i < (currentKey + this.get(currentKey)).length(); i++) {
                highScoreString += ".";
            }
            highScoreString += this.get(currentKey) + "\n";
        }
        return highScoreString;
    }

    public String getForFile() {
        String highScoreString = "";
        while (this.keys().hasMoreElements()) {
            String currentKey = this.keys().nextElement();
            highScoreString += currentKey;
            highScoreString += ":";
            highScoreString += this.get(currentKey) + "\n";
        }
        return highScoreString;
    }

    public void sort() {
        HighScoreTable newTable = new HighScoreTable();
        String[] keys = this.keySet().toArray(new String[this.keySet().size()]);
        Integer[] values = this.values().toArray(new Integer[this.values().size()]);

        Arrays.sort(values);
        for (int i = 0; i < values.length; i++) {
            newTable.put(keys[i], values[i]);
        }
        this.clear();
        this.putAll(newTable);
    }

    public Integer peek() {
        this.sort();
        return ((Integer) this.values().toArray()[this.values().toArray().length - 1]);
    }
}
