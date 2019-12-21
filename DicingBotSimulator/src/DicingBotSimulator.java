/*
 * File:    DicingBotSimulator.java
 * Package: PACKAGE_NAME
 * Author:  Zachary Gill
 */


public class DicingBotSimulator {
    
    public static long profit;
    public static double rate;
    public static long rolls;
    public static long initialBet;
    public static long limitBet;
    
    
    public static void main(String[] args) {
        profit = 0;
        rate = 0.49;
        rolls = 1000;
        initialBet = 50000;
        limitBet = Long.MAX_VALUE;
        
        long total = 0;
        
        for (long j = 0; j < 10000; j++) {
            long currentBet = initialBet;
            boolean lost = true;
            for (long i = 0; i < rolls; i++) {
                if (lost) {
                    currentBet = currentBet * 2;
                    if (currentBet > limitBet) {
                        currentBet = initialBet;
                    }
                }
        
                double roll = Math.random();
        
                if (roll <= rate) {
                    profit += currentBet;
                    lost = false;
                } else {
                    profit -= currentBet;
                    lost = true;
                }
            }
            total += profit;
        }
    
        System.out.println(total / 10000);
    }
    
}
