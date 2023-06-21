/*
 * File:    Series.java
 * Package: main.entity.video.show
 * Author:  Zachary Gill
 */

package main.entity.video.show;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import main.entity.base.Folder;
import main.util.persistence.VariableUtil;

public class Series extends Folder<Episode> {
    
    //Constants
    
    public static final String EPISODE_DIR_NAME = VariableUtil.get(0x9f06);
    
    
    //Static Fields
    
    public static final Map<File, Series> loaded = new ConcurrentHashMap<>();
    
    
    //Fields
    
    protected SeriesInfo info;
    
    
    //Constructors
    
    protected Series(File seriesDir) {
        super(seriesDir);
        
        setEntities(Episode.loadAllEpisodes(seriesDir));
    }
    
    
    //Getters
    
    public SeriesInfo getInfo() {
        return info;
    }
    
    
    //Setters
    
    public void setInfo(SeriesInfo info) {
        this.info = info;
    }
    
    
    //Static Methods
    
    public static Series loadSeries(File seriesDir) {
        if (!seriesDir.isDirectory()) {
            throw new UnsupportedOperationException();
        }
        return loaded.computeIfAbsent(seriesDir, Series::new);
    }
    
}
