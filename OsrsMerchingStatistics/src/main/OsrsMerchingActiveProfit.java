/*
 * File:    OsrsMerchingActiveProfit.java
 * Package: main
 * Author:  Zachary Gill
 */

package main;

import java.io.File;
import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import common.Filesystem;

public class OsrsMerchingActiveProfit {
    
    private static final File activeTransactionsHtml = new File("data/activeTransactions.html");
    
    public static void main(String[] args) throws Exception {
        evaluateActiveProfit();
    }
    
    private static void evaluateActiveProfit() throws Exception {
        final Pattern itemPattern = Pattern.compile("^\\s+<a\\s+.+>(?<item>.+)</a>\\s*$");
        final Pattern profitPattern = Pattern.compile("^\\s+(?<profit>-?[\\d,]+)gp\\s*$");
        final DecimalFormat profitFormat = new DecimalFormat("#,##0");
        
        Map<String, Long> itemProfits = new LinkedHashMap<>();
        long profit = 0;
        
        String itemName = null;
        Long itemProfit = null;
        for (String line : Filesystem.readLines(activeTransactionsHtml)) {
            final Matcher itemMatcher = itemPattern.matcher(line);
            if (itemMatcher.matches()) {
                itemName = itemMatcher.group("item");
            }
            
            final Matcher patternMatcher = profitPattern.matcher(line);
            if (patternMatcher.matches()) {
                itemProfit = Long.parseLong(patternMatcher.group("profit").replace(",", ""));
            }
            
            if ((itemName != null) && (itemProfit != null)) {
                itemProfits.put(itemName, itemProfit);
                profit += itemProfit;
                itemName = null;
                itemProfit = null;
            }
        }
        
        System.out.println(profitFormat.format(profit));
        System.out.println();
        
        int maxNameLength = itemProfits.keySet().stream().mapToInt(String::length).max().orElse(0);
        int maxProfitLength = itemProfits.values().stream().map(profitFormat::format).mapToInt(String::length).max().orElse(0);
        itemProfits.entrySet().stream().sorted((o1, o2) -> o2.getValue().compareTo(o1.getValue())).forEachOrdered(item ->
                System.out.printf(("%-" + maxNameLength + "s %" + maxProfitLength + "s%n"),
                        item.getKey(), profitFormat.format(item.getValue())));
    }
    
}
