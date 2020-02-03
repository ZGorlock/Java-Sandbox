/*
 * File:    AlchBuddy.java
 * Package:
 * Author:  Zachary Gill
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class AlchBuddy {
    
    public static final String GE_PRICE_TABLE_URL = "https://rsbuddy.com/exchange/summary.json";
    
    public static final int NATURE_RUNE_PRICE = 215;
    
    public static final List<String> itemNames = new ArrayList<>();
    
    public static final List<Long> itemAlchValues = new ArrayList<>();
    
    public static final List<Long> itemLimits = new ArrayList<>();
    
    public static final List<Long> itemGeValues = new ArrayList<>();
    
    public static final List<Long> itemProfits = new ArrayList<>();
    
    public static void main(String[] args) {
        //generateItemsJson();
        
        parseItemsJson();
        
        parseGeValuesJson();
        
        calculateProfits();
        
        sort();
        
        output();
    }
    
    public static void parseItemsJson() {
        String json = "";
        try {
            Scanner scanner = new Scanner(new File("items.json"), "UTF-8");
            json = scanner.nextLine();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(0);
        }
        if (json.isEmpty()) {
            System.exit(0);
        }
        
        try {
            JSONParser parser = new JSONParser();
            JSONObject data = (JSONObject) parser.parse(json);
            JSONArray items = (JSONArray) data.get("items");
            for (Object item : items) {
                JSONObject itemData = (JSONObject) item;
                String name = (String) itemData.get("name");
                long alch = (Long) itemData.get("alch");
                long limit = (Long) itemData.get("limit");
                itemNames.add(name);
                itemAlchValues.add(alch);
                itemLimits.add(limit);
            }
        } catch (ParseException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }
    
    public static void parseGeValuesJson() {
        URL url = null;
        try {
            url = new URL(GE_PRICE_TABLE_URL);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            System.exit(0);
        }
        
        String geJson = "";
        try {
            Scanner s = new Scanner(url.openStream());
            geJson = s.nextLine();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
        if (geJson.isEmpty()) {
            System.exit(0);
        }
        
        for (int i = 0; i < itemNames.size(); i++) {
            itemGeValues.add(Long.MAX_VALUE);
        }
        
        try {
            JSONParser parser = new JSONParser();
            JSONObject items = (JSONObject) parser.parse(geJson);
            
            for (Object o : items.keySet()) {
                String key = (String) o;
                if (items.get(key) instanceof JSONObject) {
                    JSONObject item = (JSONObject) items.get(key);
                    String name = (String) item.get("name");
                    long buy = (Long) item.get("buy_average");
                    if (buy == 0) {
                        buy = Integer.MAX_VALUE;
                    }
                    for (int i = 0; i < itemNames.size(); i++) {
                        if (itemNames.get(i).equals(name)) {
                            itemGeValues.set(i, buy);
                            break;
                        }
                    }
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }
    
    public static void calculateProfits() {
        for (int i = 0; i < itemNames.size(); i++) {
            long profit = itemAlchValues.get(i) - itemGeValues.get(i) - NATURE_RUNE_PRICE;
            if (profit > Long.MAX_VALUE / 2) {
                profit = Long.MIN_VALUE;
            }
            itemProfits.add(profit);
        }
    }
    
    public static void sort() {
        for (int i = 0; i < itemNames.size() - 1; i++) {
            int index = i;
            for (int j = i + 1; j < itemNames.size(); j++) {
                if (itemProfits.get(j) > itemProfits.get(index)) {
                    index = j;
                }
            }
            
            String saveName = itemNames.get(index);
            long saveAlchValue = itemAlchValues.get(index);
            long saveLimit = itemLimits.get(index);
            long saveGeValue = itemGeValues.get(index);
            long saveProfit = itemProfits.get(index);
            
            itemNames.set(index, itemNames.get(i));
            itemAlchValues.set(index, itemAlchValues.get(i));
            itemLimits.set(index, itemLimits.get(i));
            itemGeValues.set(index, itemGeValues.get(i));
            itemProfits.set(index, itemProfits.get(i));
            
            itemNames.set(i, saveName);
            itemAlchValues.set(i, saveAlchValue);
            itemLimits.set(i, saveLimit);
            itemGeValues.set(i, saveGeValue);
            itemProfits.set(i, saveProfit);
        }
    }
    
    public static void output() {
        StringBuilder output = new StringBuilder();
        output.append("ITEM, PROFIT, LIMIT, ALCH VALUE, GE VALUE").append(System.lineSeparator());
        for (int i = 0; i < itemNames.size(); i++) {
            output.append(itemNames.get(i)).append(", ").append(itemProfits.get(i)).append(", ").append(itemLimits.get(i)).append(", ").append(itemAlchValues.get(i)).append(", ").append(itemGeValues.get(i)).append(",").append(System.lineSeparator());
        }
        
        
        try (PrintWriter out = new PrintWriter("analysis.csv")) {
            out.println(output.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    private static void generateItemsJson() {
        List<String> names = new ArrayList<>(10000);
        List<String> value = new ArrayList<>(10000);
        List<String> limit = new ArrayList<>(10000);
        
        
        File file = new File("data1.txt");
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                StringTokenizer st = new StringTokenizer(line, ",");
                int n = 0;
                while (st.hasMoreTokens()) {
                    if (n == 0) {
                        names.add(st.nextToken());
                    } else if (n == 2) {
                        String s = st.nextToken();
                        value.add(s);
                        if (Long.valueOf(s) > 1000000) {
                            int x = 5;
                        }
                        break;
                    } else {
                        st.nextToken();
                    }
                    n++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        for (int i = 0; i < names.size(); i++) {
            limit.add("");
        }
        
        
        Pattern p1 = Pattern.compile(" {52}<tr>");
        Pattern p2 = Pattern.compile(" {32}<td><a href=\".*\" title=\".*\">(?<name>.*)</a></td>");
        Pattern p3 = Pattern.compile(" {76}(?<value>.*)");
        
        File file2 = new File("data2.txt");
        try (BufferedReader br = new BufferedReader(new FileReader(file2))) {
            String line;
            int c = 0;
            String name = "";
            String lim = "";
            while ((line = br.readLine()) != null) {
                if (c == 3) {
                    c = 2;
                    continue;
                }
                
                if (c == 2) {
                    Matcher m3 = p3.matcher(line);
                    if (m3.matches()) {
                        lim = m3.group("value");
                        
                        int n = -1;
                        for (int i = 0; i < names.size(); i++) {
                            if (name.equals(names.get(i))) {
                                n = i;
                                break;
                            }
                        }
                        if (n > -1) {
                            limit.set(n, lim);
                        }
                        
                    } else {
                        name = "";
                        lim = "";
                    }
                    c = 0;
                    continue;
                }
                
                if (c == 1) {
                    Matcher m2 = p2.matcher(line);
                    if (m2.matches()) {
                        name = m2.group("name");
                        c = 3;
                    } else {
                        name = "";
                        lim = "";
                        c = 0;
                    }
                    continue;
                }
                
                Matcher m1 = p1.matcher(line);
                if (m1.matches()) {
                    c = 1;
                    continue;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        
        JSONObject json = new JSONObject();
        JSONArray items = new JSONArray();
        for (int i = 0; i < names.size(); i++) {
            JSONObject item = new JSONObject();
            
            String alchValue = value.get(i);
            if (alchValue.isEmpty()) {
                continue;
            }
            alchValue = alchValue.replaceAll(",", "");
            
            String buyLimit = limit.get(i);
            if (buyLimit.isEmpty()) {
                buyLimit = "-1";
            }
            buyLimit = buyLimit.replaceAll(",", "");
            
            item.put("name", names.get(i));
            item.put("alch", Long.valueOf(alchValue));
            item.put("limit", Long.valueOf(buyLimit));
            items.add(item);
        }
        json.put("items", items);
        
        
        try (PrintWriter out = new PrintWriter("items.json")) {
            out.println(json.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    
}
