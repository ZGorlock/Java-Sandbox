/*
 * File:    Item.java
 * Package: main.entity.line
 * Author:  Zachary Gill
 */

package main.entity.line;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import main.entity.base.Line;
import main.entity.page.Month;

public class Item extends Line {
    
    //Fields
    
    public Date date;
    
    public String purchase;
    
    public float price;
    
    public Map<Category, Float> categoryPrices;
    
    
    //Constructors
    
    public Item(Line line) {
        super(line);
    }
    
    
    //Methods
    
    @Override
    protected void parse() {
        date = parseDate(data.get(Month.HEADERS.indexOf("Date")));
        purchase = data.get(Month.HEADERS.indexOf("Purchase"));
        
        categoryPrices = new LinkedHashMap<>();
        Arrays.stream(Category.values()).forEachOrdered(category -> {
            boolean empty = (data.size() <= category.columnIndex) || (data.get(category.columnIndex) == null) || data.get(category.columnIndex).isBlank();
            categoryPrices.put(category, empty ? 0 : Float.parseFloat(data.get(category.columnIndex)));
        });
        
        price = (float) categoryPrices.values().stream().mapToDouble(e -> e).sum();
    }
    
    private Date parseDate(String dateString) {
        for (int i = 0; i < Month.MONTH_KEYS.size(); i++) {
            dateString = dateString.replace(Month.MONTH_KEYS.get(i), ("0" + (i + 1)).replaceAll("0(\\d{2})", "$1"));
        }
        try {
            return new SimpleDateFormat("dd-MM-yyyy").parse(dateString);
        } catch (Exception ignored) {
        }
        return null;
    }
    
}
