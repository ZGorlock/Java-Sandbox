/*
 * File:    EodData.java
 * Package: main.entity
 * Author:  Zachary Gill
 */

package main.entity;

import java.util.Date;
import java.util.Map;

public class EodData extends Entity<Date> {
    
    //Fields
    
    public Date date;
    
    public Long volume;
    
    public Float open;
    
    public Float close;
    
    public Float adjustedClose;
    
    public Float high;
    
    public Float low;
    
    
    //Constructors
    
    public EodData(Map<String, Object> data) {
        this.date = dateParser.apply(data.get("date"));
        this.volume = longParser.apply(data.get("volume"));
        this.open = floatParser.apply(data.get("open"));
        this.close = floatParser.apply(data.get("close"));
        this.adjustedClose = floatParser.apply(data.get("adjustedClose"));
        this.high = floatParser.apply(data.get("high"));
        this.low = floatParser.apply(data.get("low"));
    }
    
    
    //Methods
    
    @Override
    public String getIdentifier() {
        return dateString.apply(date);
    }
    
    @Override
    public Date getKey() {
        return date;
    }
    
}
