/*
 * File:    VariableUtil.java
 * Package: main.util.persistence
 * Author:  Zachary Gill
 */

package main.util.persistence;

import java.io.File;
import java.util.Collections;
import java.util.Map;

import commons.access.Project;

public class VariableUtil {
    
    //Constants
    
    private static File VARIABLE_MAP_FILE = new File(Project.DATA_DIR, "variables.txt");
    
    
    //Static Fields
    
    private static final Map<Integer, String> variableMap = DataFileLoader.readLineDataMap(VARIABLE_MAP_FILE,
                    e -> parseHex(e.getKey()),
                    Map.Entry::getValue)
            .orElseGet(Collections::emptyMap);
    
    
    //Static Methods
    
    public static String get(Integer key) {
        return variableMap.get(key);
    }
    
    public static String get(String key) {
        return get(parseHex(key));
    }
    
    private static Integer parseHex(String hexStr) {
        return Integer.parseInt(hexStr.replace("0x", ""), 16);
    }
    
}
