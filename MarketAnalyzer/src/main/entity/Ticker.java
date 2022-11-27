/*
 * File:    Ticker.java
 * Package: main.entity
 * Author:  Zachary Gill
 */

package main.entity;

import java.util.Map;

public class Ticker extends Entity<String> {
    
    //Fields
    
    public String code;
    
    public String name;
    
    public String exchange;
    
    public String currency;
    
    public String country;
    
    public String type;
    
    public String isin;
    
    
    //Constructors
    
    public Ticker(Map<String, Object> data) {
        this.code = (String) data.get("Code");
        this.name = (String) data.get("Name");
        this.exchange = (String) data.get("Exchange");
        this.currency = (String) data.get("Currency");
        this.country = (String) data.get("Country");
        this.type = (String) data.get("Type");
        this.isin = (String) data.get("Isin");
    }
    
    
    //Methods
    
    @Override
    public String getKey() {
        return code;
    }
    
}
