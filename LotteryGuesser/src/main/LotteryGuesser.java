/*
 * File:    LotteryGuesser.java
 * Package: main
 * Author:  Zachary Gill
 */

package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LotteryGuesser {
    
    private static final List<List<Integer>> tallies = new ArrayList<>();
    
    private static final List<List<Double>> odds = new ArrayList<>();
    
    private static int populationSize = 0;
    
    public static void main(String[] args) {
        initializeStructures();
        populateTallies();
        calculateOdds();
        guessLottery();
    }
    
    private static void guessLottery() {
        StringBuilder numbers = new StringBuilder();
        for (List<Double> odd : odds) {
            if (!numbers.toString().isEmpty()) {
                numbers.append(" - ");
            }
            numbers.append(maximumValueIndex(odd) + 1);
        }
        System.out.println(numbers.toString());
    }
    
    private static void calculateOdds() {
        for (int i = 0; i < tallies.size(); i++) {
            final double hitChance = 1.0 / tallies.get(i).size();
            final double missChance = (double) (tallies.get(i).size() - 1) / tallies.get(i).size();
            
            for (int j = 0; j < tallies.get(i).size(); j++) {
                int tallyCount = tallies.get(i).get(j) + 1;
                double currentOdds = 1.0 - Math.pow(hitChance, tallyCount) - Math.pow(missChance, populationSize - tallyCount);
                odds.get(i).set(j, currentOdds);
            }
        }
    }
    
    private static void initializeStructures() {
        for (int i = 0; i < 5; i++) {
            List<Integer> tally = new ArrayList<>();
            List<Double> odd = new ArrayList<>();
            for (int j = 0; j < 69; j++) {
                tally.add(0);
                odd.add(0.0);
            }
            tallies.add(tally);
            odds.add(odd);
        }
        
        List<Integer> tally = new ArrayList<>();
        List<Double> odd = new ArrayList<>();
        for (int j = 0; j < 26; j++) {
            tally.add(0);
            odd.add(0.0);
        }
        tallies.add(tally);
        odds.add(odd);
    }
    
    private static void populateTallies() {
        List<String> lines = readData();
        
        for (String line : lines) {
            String[] elements = line.split(",");
            
            if (elements.length == 7 && isValidDate(elements[0])) {
                for (int i = 0; i < 6; i++) {
                    int ball = Integer.valueOf(elements[i + 1]) - 1;
                    tallies.get(i).set(ball, tallies.get(i).get(ball) + 1);
                }
                populationSize++;
            }
        }
    }
    
    private static List<String> readData() {
        List<String> lines = new ArrayList<>();
        
        try {
            File dataFile = new File("resources/powerball.csv");
            FileReader reader = new FileReader(dataFile);
            BufferedReader bufferedReader = new BufferedReader(reader);
            
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                lines.add(line);
            }
            
        } catch (IOException ignored) {
        }
        
        return lines;
    }
    
    private static boolean isValidDate(String date) {
        List<SimpleDateFormat> possibleFormats = new ArrayList<>();
        possibleFormats.add(new SimpleDateFormat("MM/dd/yyyy"));
        possibleFormats.add(new SimpleDateFormat("MM/d/yyyy"));
        possibleFormats.add(new SimpleDateFormat("M/dd/yyyy"));
        possibleFormats.add(new SimpleDateFormat("M/d/yyyy"));
        
        Date d = null;
        for (SimpleDateFormat sdf : possibleFormats) {
            try {
                d = sdf.parse(date);
                break;
            } catch (ParseException ignored) {
            }
        }
        if (d == null) {
            return false;
        }
        
        try {
            Date changeover = possibleFormats.get(0).parse("10/07/2015");
            return d.after(changeover) || d.compareTo(changeover) == 0;
        } catch (ParseException e) {
            return false;
        }
    }
    
    private static int maximumValueIndex(List<Double> list) {
        int index = -1;
        double max = 0;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) > max) {
                max = list.get(i);
                index = i;
            }
        }
        return index;
    }
    
}
