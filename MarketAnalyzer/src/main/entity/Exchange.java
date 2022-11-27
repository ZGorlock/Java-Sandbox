/*
 * File:    Exchange.java
 * Package: main.entity
 * Author:  Zachary Gill
 */

package main.entity;

import java.util.Map;

public class Exchange extends Entity<String> {
    
    //Fields
    
    public String code;
    
    public String name;
    
    public String currency;
    
    public String country;
    
    public String countryIso2;
    
    public String countryIso3;
    
    public String operatingMic;
    
    
    //Constructors
    
    public Exchange(Map<String, Object> data) {
        this.code = (String) data.get("Code");
        this.name = (String) data.get("Name");
        this.currency = (String) data.get("Currency");
        this.country = (String) data.get("Country");
        this.countryIso2 = (String) data.get("CountryISO2");
        this.countryIso3 = (String) data.get("CountryISO3");
        this.operatingMic = (String) data.get("OperatingMIC");
    }
    
    
    //Methods
    
    @Override
    public String getKey() {
        return code;
    }
    
}
