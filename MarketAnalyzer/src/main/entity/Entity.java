/*
 * File:    Entity.java
 * Package: main.entity
 * Author:  Zachary Gill
 */

package main.entity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

import commons.lambda.function.checked.CheckedFunction;

public abstract class Entity<C extends Comparable<C>> implements Comparable<Entity<C>> {
    
    //Constants
    
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    
    
    //Static Functions
    
    public static final CheckedFunction<Object, Date> dateParser = (Object dateObject) ->
            Optional.ofNullable(dateObject).map(String::valueOf)
                    .map((CheckedFunction<String, Date>) e -> new SimpleDateFormat(DATE_FORMAT).parse(e))
                    .orElse(null);
    
    public static final CheckedFunction<Object, Long> longParser = (Object longData) ->
            Optional.ofNullable(longData).map(String::valueOf)
                    .map((CheckedFunction<String, Long>) Long::parseLong)
                    .orElse(null);
    
    public static final CheckedFunction<Object, Float> floatParser = (Object floatObject) ->
            Optional.ofNullable(floatObject).map(String::valueOf)
                    .map((CheckedFunction<String, Float>) Float::parseFloat)
                    .orElse(null);
    
    public static final CheckedFunction<Date, String> dateString = (Date date) ->
            Optional.ofNullable(date)
                    .map((CheckedFunction<Date, String>) e -> new SimpleDateFormat(DATE_FORMAT).format(e))
                    .orElse(null);
    
    
    //Methods
    
    public String getIdentifier() {
        return String.valueOf(getKey());
    }
    
    public abstract C getKey();
    
    @Override
    public int compareTo(Entity<C> o) {
        return getKey().compareTo(o.getKey());
    }
    
}
