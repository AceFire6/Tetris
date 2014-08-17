package com.mygdx.tetris;


import java.util.*;

/**
 * @author Jethro Muller - MLLJET001
 * @version 1.0.0
 *
 * The highscore table helps manage Tetris's high score list.
 */

public class HighScoreTable extends ArrayList<String> {

    /**
     * Default constructor
     */
    public HighScoreTable() {
        super();
    }

    /**
     * Adds the given player detials to the highscore list.
     * @param name         String containing the player's name.
     * @param playscore    int that is the player's score.
     * @param date         The date the player achieved the score.
     */
    public void addScore(String name, int playscore, Date date) {
        sort();
        GregorianCalendar dateCal = new GregorianCalendar();
        dateCal.setTime(date);
        if (name.length() < 8) {
            for (int i = 0; i < 10 - name.length(); i++) {
                name += " ";
            }
        }
        name += dateCal.get(Calendar.DAY_OF_MONTH) + "/" + dateCal.get(Calendar.MONTH) +
                "/" + dateCal.get(Calendar.YEAR);
        add(name + ": " + playscore);
        cullValues();
    }

    /**
     * Returns the highscore list as a string so it can be displayed on the UI.
     * @return Highscore list as a string.
     */
    public String getAsString() {
        sort();
        String highScoreString = "";
        for (String key: this) {
            highScoreString += key + "\n";
        }
        return highScoreString;
    }

    /**
     * Looks at the smallest value in the highscore list.
     * @return Smallest value in the highscore list.
     */
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

    /**
     * Sort the highscore list by the player's score.
     */
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

    /**
     * Removes all values except the top 5.
     */
    private void cullValues() {
        sort();
        int length = Math.min(5, size());
        String[] scores = new String[length];

        for (int i = 0; i < length; i++) {
            scores[i] = this.get(i);
        }

        this.clear();
        this.addAll(Arrays.asList(scores));
    }
}
