/*
 * File:    LastProcessUtil.java
 * Package: main.util.persistence
 * Author:  Zachary Gill
 */

package main.util.persistence;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import commons.access.Project;
import main.entity.base.Entity;

public class LastProcessUtil {
    
    //Constants
    
    private static File LAST_PROCESSED_MAP_FILE = new File(Project.DATA_DIR, "last.txt");
    
    private static String DATE_FORMAT = "yyyyMMdd-HHmm";
    
    
    //Static Fields
    
    private static final Map<String, Date> lastProcessMap = DataFileLoader.readLineDataMap(LAST_PROCESSED_MAP_FILE,
                    Map.Entry::getKey,
                    e -> new SimpleDateFormat(DATE_FORMAT).parse(e.getValue()))
            .orElseGet(Collections::emptyMap);
    
    
    //Static Methods
    
    public static Date getLastProcessDate(Class<? extends Entity> type) {
        return lastProcessMap.get(Optional.ofNullable(type).map(Class::getSimpleName).orElse(""));
    }
    
}
