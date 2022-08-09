/*
 * File:    Category.java
 * Package: main.entity.line
 * Author:  Zachary Gill
 */

package main.entity.line;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import main.entity.page.Month;

public enum Category {
    
    //Values
    
    FOOD("Food", 0),
    HOME("Home", 0),
    CAR("Car", 0),
    FUN("Fun", 0),
    OTHER("Other", 0),
    RENT("Rent", 1),
    CAR_PAYMENT("Car Pymt.", 1),
    INSURANCE("Insurance", 1),
    ELECTRICITY("Electricity", 1),
    WATER("Water", 1),
    PHONE("Phone", 1),
    INTERNET("Internet", 1);
    
    
    //Enums
    
    public enum Type {
        STANDARD,
        BILLS
    }
    
    
    //Fields
    
    public final String header;
    
    public final Type type;
    
    public final int columnIndex;
    
    
    //Constructors
    
    Category(String header, int typeOrdinal) {
        this.header = header;
        this.type = Type.values()[typeOrdinal];
        this.columnIndex = Month.BASE_HEADERS.size() + this.ordinal() + typeOrdinal + 1;
    }
    
    
    //Static Methods
    
    public static Category getByColumnIndex(int columnIndex) {
        return Arrays.stream(values())
                .filter(e -> (e.columnIndex == columnIndex))
                .findFirst().orElse(null);
    }
    
    public static Category getByHeader(String header) {
        return Arrays.stream(values())
                .filter(e -> (e.header.equals(header)))
                .findFirst().orElse(null);
    }
    
    public static List<Category> getAllOfType(Type type) {
        return Arrays.stream(values())
                .filter(e -> (e.type == type))
                .collect(Collectors.toList());
    }
    
}
