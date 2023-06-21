/*
 * File:    SortMethod.java
 * Package: main.entity.shortcut.subreddit
 * Author:  Zachary Gill
 */

package main.entity.shortcut.subreddit;

import java.util.Arrays;

public enum SortMethod {
    
    //Values
    
    NONE,
    HOT, NEW, RISING, TOP,
    TOP_HOUR, TOP_DAY, TOP_WEEK, TOP_MONTH, TOP_YEAR, TOP_ALL;
    
    
    //Fields
    
    private final String queryParameter;
    
    
    //Constructors
    
    SortMethod() {
        this.queryParameter = (name() + (name().contains("_") ? "" : "_")).toLowerCase()
                .replace("none_", "")
                .replace("_", "/?t=").replaceAll("\\?t=$", "");
    }
    
    
    //Getters
    
    public String getQueryParameter() {
        return queryParameter;
    }
    
    
    //Static Methods
    
    public static SortMethod parseQueryParameter(String url) {
        return Arrays.stream(values())
                .filter(e -> url.toUpperCase().contains("/" + e.name().replaceAll("_", "/?T=")))
                .findFirst().orElse(NONE);
    }
    
}
