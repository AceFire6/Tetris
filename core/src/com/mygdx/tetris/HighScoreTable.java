package com.mygdx.tetris;


import java.util.*;

public class HighScoreTable extends LinkedHashMap<String, Integer> {

    public HighScoreTable() {
        super();
    }

    public String getAsString() {
        String highScoreString = "";
        Set<String> keys = sortByValue((HighScoreTable)this.clone()).keySet();
        int counter = 0;
        for (String key : keys) {
            if (counter < 5) {
                highScoreString += key;
                if (key.length() < 5) {
                    for (int i = 0; i < 6 - key.length(); i++) {
                        highScoreString += " ";
                    }
                }
                highScoreString += "   " + this.get(key) + "\n";
                counter++;
            } else {
                break;
            }
        }
        return highScoreString;
    }

    public String getForFile() {
        String highScoreString = "";
        Set<String> keys = this.keySet();
        for (String key : keys) {
            highScoreString += key;
            highScoreString += ":";
            highScoreString += this.get(key) + "\n";
        }
        return highScoreString;
    }

    public Integer peek() {
        if (size() > 0) {
            return ((Integer) sortByValue(this).values().toArray()[size() - 1]);
        } else {
            return 0;
        }
    }

    /**
     * Adapted from http://stackoverflow.com/a/2581754
     */
    public static HighScoreTable sortByValue(HighScoreTable hsTable) {
        List<Map.Entry<String, Integer>> list =
                new LinkedList<Map.Entry<String, Integer>>(hsTable.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

        HighScoreTable result = new HighScoreTable();
        for (Map.Entry<String, Integer> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }
}
