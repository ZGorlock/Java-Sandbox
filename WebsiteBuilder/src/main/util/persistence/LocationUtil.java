/*
 * File:    LocationUtil.java
 * Package: main.util.persistence
 * Author:  Zachary Gill
 */

package main.util.persistence;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import commons.access.Project;
import commons.object.string.StringUtility;
import main.entity.base.Entity;

public class LocationUtil {
    
    //Constants
    
    private static File ROOT_LOCATION_FILE = new File(Project.DATA_DIR, "siteRoot.txt");
    
    private static File ENTITY_LOCATION_MAP_FILE = new File(Project.DATA_DIR, "locations.txt");
    
    private static String DATE_FORMAT = "yyyyMMdd-HHmm";
    
    
    //Static Fields
    
    private static final File rootLocation = DataFileLoader.read(ROOT_LOCATION_FILE)
            .map(File::new).orElseThrow();
    
    private static final Map<String, List<File>> entityLocationMap = DataFileLoader.readGroupedDataMap(ENTITY_LOCATION_MAP_FILE,
                    Map.Entry::getKey,
                    e -> new File(e.getValue().replace("~", StringUtility.fileString(getRootLocation()))))
            .orElseGet(Collections::emptyMap);
    
    
    //Static Methods
    
    public static File getRootLocation() {
        return rootLocation;
    }
    
    public static File getLocation(Integer... pathVariables) {
        return Arrays.stream(pathVariables)
                .map(e -> Optional.ofNullable(e)
                        .map(VariableUtil::get)
                        .orElse(""))
                .collect(Collectors.collectingAndThen(
                        Collectors.joining("/"),
                        path -> new File(getRootLocation(), path)));
    }
    
    public static List<File> getEntityLocations(Class<? extends Entity> type) {
        return entityLocationMap.get(type.getSimpleName());
    }
    
}
