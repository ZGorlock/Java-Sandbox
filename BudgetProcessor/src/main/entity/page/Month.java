/*
 * File:    Month.java
 * Package: main.entity.page
 * Author:  Zachary Gill
 */

package main.entity.page;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import main.entity.base.Page;
import main.entity.line.Category;
import main.entity.line.Item;

public class Month extends Page {
    
    //Constants
    
    public static final List<String> BASE_HEADERS = List.of("Date", "Purchase", "Price");
    
    public static final List<String> HEADERS = Stream.concat(
                    Stream.of(BASE_HEADERS),
                    Arrays.stream(Category.Type.values())
                            .map(type -> Category.getAllOfType(type).stream().map(category -> category.header).collect(Collectors.toList())))
            .flatMap(headerGroup -> Stream.of(headerGroup, List.of("")))
            .flatMap(Collection::stream).collect(Collectors.toList());
    
    public static final List<String> MONTH_KEYS = List.of(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec");
    
    public static final int TOTALS_ROW_INDEX = 2;
    
    public static final int FIRST_ITEM_ROW_INDEX = 3;
    
    
    //Fields
    
    public int year;
    
    public int month;
    
    public List<Item> items;
    
    public float total;
    
    public Map<Category, Float> categoryTotals;
    
    
    //Constructors
    
    public Month(Page page) {
        super(page);
    }
    
    
    //Methods
    
    @Override
    protected void parse() {
        year = Integer.parseInt("20" + this.title.split("-")[1]);
        month = MONTH_KEYS.indexOf(this.title.split("-")[0]) + 1;
        
        items = new ArrayList<>();
        for (int i = FIRST_ITEM_ROW_INDEX; i < lineCount(); i++) {
            boolean empty = lines.get(i).data.isEmpty() || (lines.get(i).data.get(0) == null) || lines.get(i).data.get(0).isBlank();
            if (!empty) {
                items.add(new Item(lines.get(i)));
            }
        }
        
        categoryTotals = new LinkedHashMap<>();
        Arrays.stream(Category.values()).forEachOrdered(category ->
                categoryTotals.put(category, (float) items.stream().map(e -> e.categoryPrices.get(category)).mapToDouble(e -> e).sum()));
        
        total = (float) categoryTotals.values().stream().mapToDouble(e -> e).sum();
    }
    
}
