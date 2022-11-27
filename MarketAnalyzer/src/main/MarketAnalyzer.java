/*
 * File:    MarketAnalyzer.java
 * Package: main
 * Author:  Zachary Gill
 */

package main;

import main.util.AssetTrends;

public class MarketAnalyzer {
    
    public static void main(String[] args) throws Exception {
//        Map<String, Exchange> exchanges = MarketApi.listExchanges();
//        
//        Map<String, Ticker> tickers = MarketApi.listTickers();
//        Map<String, Ticker> tickers = MarketApi.listTickers("US", true);
//        Map<String, Ticker> tickers = MarketApi.listTickers("LSE");
//        
//        Map<String, EodData> eod = MarketApi.eodData("US", "GME");
        
        AssetTrends.load();
        AssetTrends.assetInGold("DOW");
        AssetTrends.assetInGold("HPI");
    }
    
}
