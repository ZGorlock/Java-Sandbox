/*
 * File:    OsrsMerchingActiveProfit.java
 * Package: main
 * Author:  Zachary Gill
 */

package main;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import common.Filesystem;

public class OsrsMerchingActiveProfit {
    
    private static final File activeTransactionsHtml = new File("data/activeTransactions.html");
    
    public static void main(String[] args) throws Exception {
        evaluateActiveProfit();
    }
    
    private static void evaluateActiveProfit() throws Exception {
        long profit = 0;
        Pattern pattern = Pattern.compile("^\\s+(?<profit>-?[\\d,]+)gp$");
        List<String> lines = Filesystem.readLines(activeTransactionsHtml);
        for (String line : lines) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.matches()) {
                profit += Long.parseLong(matcher.group("profit").replace(",", ""));
            }
        }
        System.out.println(profit);
    }
    
}
