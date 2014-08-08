package com.mygdx.tetris;


import java.util.Arrays;
import java.util.Hashtable;
import java.util.Set;

public class HighScoreTable extends Hashtable<String, Integer> {

    public HighScoreTable() {
        super();
    }

    @Override
    public String toString() {
        String highScoreString = "";
        Set<String> keys = this.keySet();
        int counter = 0;
        for (String key : keys) {
            if (counter < 5) {
                highScoreString = key + "    " + this.get(key) + "\n" + highScoreString;
                counter++;
            } else {
                break;
            }
        }
        return highScoreString;
    }

    public String getForFile() {
        String highScoreString = "";
        int counter = 0;
        Set<String> keys = this.keySet();
        for (String key : keys) {
            if (counter < 5) {
                highScoreString += key;
                highScoreString += ":";
                highScoreString += this.get(key) + "\n";
                counter++;
            } else {
                break;
            }
        }
        return highScoreString;
    }

    public void sort() {
        HighScoreTable newTable = new HighScoreTable();
        String[] keys = this.keySet().toArray(new String[this.keySet().size()]);
        Integer[] values = this.values().toArray(new Integer[this.values().size()]);

        Arrays.sort(values);
        for (int i = values.length - 1; i >= 0; i--) {
            newTable.put(keys[i], values[i]);
        }
        this.clear();
        this.putAll(newTable);
    }

    public Integer peek() {
        return ((Integer) this.values().toArray()[0]);
    }
}
