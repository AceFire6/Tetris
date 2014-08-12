package com.mygdx.tetris;


import java.util.*;

public class HighScoreTable extends LinkedHashMap<String, Integer> {

    public HighScoreTable() {
        super();
    }

    public void addScore(String name, int playscore, Date date) {
        GregorianCalendar dateCal = new GregorianCalendar();
        dateCal.setTime(date);
        if (name.length() < 6) {
            for (int i = 0; i < 6 - name.length(); i++) {
                name += " ";
            }
        }
        name += "  "  + dateCal.get(Calendar.DAY_OF_MONTH) + "/" + dateCal.get(Calendar.MONTH) +
                "/" + dateCal.get(Calendar.YEAR);
        put(name, playscore);
    }

    public String getAsString() {
        String highScoreString = "";
        Set<String> keys = sortByValue((HighScoreTable) this.clone()).keySet();
        int counter = 0;
        for (String key : keys) {
            if (counter < 5) {
                highScoreString += key + "  " + this.get(key) + "\n";
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
            //Check the fifth value
            return ((Integer) sortByValue(this).values().toArray()[4]);
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
