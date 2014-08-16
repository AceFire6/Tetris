package com.mygdx.tetris;


import java.util.*;

public class HighScoreTable extends ArrayList<String> {

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
        add(name + ": " + playscore);
    }

    public String getAsString() {
        sort();
        String highScoreString = "";
        for (String key: this) {
            highScoreString += key + "\n";
        }
        return highScoreString;
    }

    public Integer peek() {
        int[] scores = new int[size()];
        int index = 0;
        for (String key: this.toArray(new String[size()])) {
            scores[index] = Integer.parseInt(key.split(": ")[1]);
            index++;
        }

        Arrays.sort(scores);

        if (size() > 0) {
            return scores[0];
        }
        return 0;
    }

    private void sort() {
        String[] scores = new String[size()];
        this.toArray(scores);
        for (int i = 0; i < scores.length; i++) {
            for (int j = 0; j < scores.length; j++) {
                if (Integer.parseInt(scores[j].split(": ")[1]) < Integer.parseInt(scores[i].split
                        (": ")[1])) {
                    String temp = scores[j];
                    scores[j] = scores[i];
                    scores[i] = temp;
                }
            }
        }

        this.clear();
        this.addAll(Arrays.asList(scores));
    }


        HighScoreTable result = new HighScoreTable();
        for (Map.Entry<String, Integer> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }
}
