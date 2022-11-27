/*
 * File:    AssetTrends.java
 * Package: main.util
 * Author:  Zachary Gill
 */

package main.util;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import commons.access.Filesystem;
import commons.lambda.function.checked.CheckedFunction;
import commons.lambda.stream.collector.MapCollectors;
import commons.object.collection.ListUtility;

public class AssetTrends {
    
    //Constants
    
    //https://fred.stlouisfed.org/series/DJIA
    public static final File DOW_DATA = new File("data", "dji.csv");
    
    //https://fred.stlouisfed.org/series/CSUSHPINSA
    public static final File HPI_DATA = new File("data", "hpi.csv");
    
    //https://stooq.com/q/d/?s=xauusd
    public static final File GLD_DATA = new File("data", "gld.csv");
    
    
    //Static Fields
    
    public static final Map<String, Map<LocalDate, Double>> assets = new HashMap<>();
    
    
    //Static Methods
    
    public static void load() {
        if (!assets.isEmpty()) {
            return;
        }
        
        assets.put("DOW", load(DOW_DATA));
        assets.put("GLD", load(GLD_DATA));
        assets.put("HPI", load(HPI_DATA));
    }
    
    private static Map<LocalDate, Double> load(File data) {
        return Filesystem.readLines(data).stream()
                .map(e -> e.split(","))
                .map((CheckedFunction<String[], Map.Entry<LocalDate, Double>>) e -> Map.entry(
                        LocalDate.parse(e[0], DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                        Double.parseDouble(e[1])))
                .collect(MapCollectors.toLinkedHashMap());
    }
    
    private static double getAtDate(String asset, LocalDate date) {
        return ListUtility.getOrDefault(assets.get(asset).entrySet().stream()
                        .filter(e -> !e.getKey().isAfter(date))
                        .sorted((o1, o2) -> o2.getKey().compareTo(o1.getKey()))
                        .limit(1)
                        .map(Map.Entry::getValue)
                        .collect(Collectors.toList()),
                0, 0.0);
    }
    
    public static void assetInGold(String asset) {
        Filesystem.writeLines(new File("output", asset + "-in-Gold.csv"),
                assets.get(asset).entrySet().stream()
                        .map(e -> String.join(",",
                                e.getKey().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                                String.valueOf(e.getValue() / getAtDate("GLD", e.getKey()))
                        )).collect(Collectors.toList())
        );
    }
    
}
