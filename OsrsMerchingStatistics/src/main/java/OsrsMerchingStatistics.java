/*
 * File:    OsrsMerchingStatistics.java
 * Package: PACKAGE_NAME
 * Author:  Zachary Gill
 */

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.Month;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import common.Filesystem;
import common.StringUtility;

public class OsrsMerchingStatistics {
    
    private static final File log = new File("Merchanting Log - 1 Year.csv");
    
    private static final List<Transaction> transactions = new ArrayList<>();
    
    
    public static void main(String[] args) throws Exception {
        parseLog();
        evaluateTransactions();
    }
    
    
    private static void evaluateTransactions() throws Exception {
        long totalProfit = 0;
        long totalCost = 0;
        long totalRevenue = 0;
        
        Map<String, Long> mostProfitable = new LinkedHashMap<>();
        Map<String, Long> mostTraded = new LinkedHashMap<>();
        
        Map<String, Long> profitPerMonth = new LinkedHashMap<>();
        Map<String, Long> profitPerWeek = new LinkedHashMap<>();
        Map<String, Long> profitPerWeekDay = new LinkedHashMap<>();
        
        for (Transaction t : transactions) {
            totalProfit += t.totalProfit;
            totalCost += t.totalCost;
            totalRevenue += t.totalRevenue;
            
            mostProfitable.putIfAbsent(t.item, 0L);
            mostTraded.putIfAbsent(t.item, 0L);
            mostProfitable.replace(t.item, mostProfitable.get(t.item) + t.totalProfit);
            mostTraded.replace(t.item, mostTraded.get(t.item) + t.quantity);
            
            Calendar cal = Calendar.getInstance();
            cal.setTime(t.soldDate);
            String monthKey = Month.values()[cal.get(Calendar.MONTH)].name() + " " + cal.get(Calendar.YEAR);
            String weekKey = "Week " + StringUtility.padZero(cal.get(Calendar.WEEK_OF_YEAR), 2) + " " + cal.get(Calendar.YEAR);
            String weekdayKey = DayOfWeek.values()[(cal.get(Calendar.DAY_OF_WEEK) + 5) % 7].name();
            profitPerMonth.putIfAbsent(monthKey, 0L);
            profitPerWeek.putIfAbsent(weekKey, 0L);
            profitPerWeekDay.putIfAbsent(weekdayKey, 0L);
            profitPerMonth.replace(monthKey, profitPerMonth.get(monthKey) + t.totalProfit);
            profitPerWeek.replace(weekKey, profitPerWeek.get(weekKey) + t.totalProfit);
            profitPerWeekDay.replace(weekdayKey, profitPerWeekDay.get(weekdayKey) + t.totalProfit);
        }
        
        List<String> output = new ArrayList<>();
        output.add("Total Profit:  " + commaify(totalProfit));
        output.add("");
        output.add("Total Cost:    " + commaify(totalCost));
        output.add("Total Revenue: " + commaify(totalRevenue));
        output.add("Total Trade:   " + commaify(totalCost + totalRevenue));
        output.add("Profit Margin: " + ((totalRevenue - totalCost) / (double) totalCost * 100) + "%");
        output.add("");
    
        output.add("Most Profitable:");
        mostProfitable.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).forEachOrdered(e -> output.add(e.getKey() + ": " + commaify(e.getValue())));
        output.add("");
        output.add("Most Traded:");
        mostTraded.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).forEachOrdered(e -> output.add(e.getKey() + ": " + commaify(e.getValue())));
        output.add("");
    
        output.add("Profit Per Month:");
        profitPerMonth.forEach((key, value) -> output.add(StringUtility.padRight((key + ": "), "SEPTEMBER 0000: ".length()) + commaify(value)));
        output.add("AVERAGE PER MONTH: " + commaify(totalProfit / profitPerMonth.size()));
        output.add("");
        output.add("Profit Per Week:");
        profitPerWeek.forEach((key, value) -> output.add(key + ": " + commaify(value)));
        output.add("AVERAGE PER WEEK: " + commaify(totalProfit / profitPerWeek.size()));
        output.add("");
        output.add("Profit Per Week Day:");
        profitPerWeekDay.forEach((key, value) -> output.add(StringUtility.padRight((key + ": "), "WEDNESDAY: ".length()) + commaify(value)));
        output.add("");
        
        for (String outputLine : output) {
            System.out.println(outputLine);
        }
        Filesystem.writeLines(new File("stats.txt"), output);
    }
    
    private static void parseLog() throws Exception {
        List<String> lines = Filesystem.readLines(log);
        for (String line : lines) {
            String[] lineParts = line.split(",", -1);
            Transaction transaction = new Transaction();
            transaction.item = lineParts[0];
            transaction.buyPrice = Integer.parseInt(lineParts[1]);
            transaction.sellPrice = Integer.parseInt(lineParts[2]);
            transaction.quantity = Integer.parseInt(lineParts[3]);
            transaction.totalCost = Long.parseLong(lineParts[4]);
            transaction.totalRevenue = Long.parseLong(lineParts[5]);
            transaction.profitPer = lineParts[6].isEmpty() ? 0 : Integer.parseInt(lineParts[6]);
            transaction.totalProfit = lineParts[7].isEmpty() ? 0 : Long.parseLong(lineParts[7]);
            transaction.cumulativeProfit = Long.parseLong(lineParts[8]);
            transaction.gpPerHour = lineParts[9].isEmpty() ? 0 : Long.parseLong(lineParts[9]);
            transaction.totalTransactionTime = lineParts[10];
            transaction.buyDate = parseDate(lineParts[11]);
            transaction.soldDate = parseDate(lineParts[12]);
            transactions.add(transaction);
        }
    }
    
    private static Date parseDate(String dateString) throws Exception {
        String[] dateStringParts = dateString.split("\\s");
        String[] dateParts = dateStringParts[0].split("/");
        String[] timeParts = dateStringParts[1].split(":");
        String newDateString = StringUtility.padZero(dateParts[1], 2) + "/" +
                               StringUtility.padZero(dateParts[0], 2) + "/" + 
                               "20" + dateParts[2] + " " + 
                               StringUtility.padZero(timeParts[0], 2) + ":" + 
                               StringUtility.padZero(timeParts[1], 2) + ":" + "00";
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        return sdf.parse(newDateString);
    }
    
    private static String commaify(long value) {
        StringBuilder commaified = new StringBuilder();
        String valueString = String.valueOf(value);
        for (int i = 0; i < valueString.length(); i++) {
            if (i % 3 == 0 && i > 0) {
                commaified.insert(0, ",");
            }
            commaified.insert(0, valueString.charAt(valueString.length() - 1 - i));
        }
        return commaified.toString();
    }
    
    
    private static class Transaction {
        String item;
        int buyPrice;
        int sellPrice;
        int quantity;
        long totalCost;
        long totalRevenue;
        int profitPer;
        long totalProfit;
        long cumulativeProfit;
        long gpPerHour;
        String totalTransactionTime;
        Date buyDate;
        Date soldDate;
    }
    
}
