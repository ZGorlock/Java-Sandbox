/*
 * File:    SeriesInfo.java
 * Package: main.entity.video.show
 * Author:  Zachary Gill
 */

package main.entity.video.show;

import java.util.List;

import main.entity.image.Image;

public class SeriesInfo {
    
    //Fields
    
    public String seriesUrl;
    
    public List<String> tags;
    
    public String rating;
    
    public String description;
    
    public String title;
    
    public String originalTitle;
    
    public String studio;
    
    public String firstAirDate;
    
    public String lastAirDate;
    
    public String episodeCount;
    
    public String status;
    
    public String posterUrl;
    
    public Image poster;
    
    
    //Constructors
    
    public SeriesInfo() {
        super();
    }
    
    public SeriesInfo(String seriesUrl) {
        this();
        
        this.seriesUrl = seriesUrl;
    }
    
}
