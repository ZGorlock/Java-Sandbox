/*
 * File:    PropertyUtil.java
 * Package: main.util
 * Author:  Zachary Gill
 */

package main.util;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import commons.access.Filesystem;
import commons.access.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PropertyUtil {
    
    //Logger
    
    private static final Logger logger = LoggerFactory.getLogger(PropertyUtil.class);
    
    
    //Static Methods
    
    public static String readProperty(File file) {
        return Optional.ofNullable(file)
                .filter(File::exists).map(Filesystem::readFileToString)
                .filter(e -> !e.isBlank())
                .orElseThrow();
    }
    
    public static String readProperty(String fileName) {
        return readProperty(new File(Project.DATA_DIR, fileName));
    }
    
    public static List<String> readPropertyList(File file, boolean raw) {
        return Optional.ofNullable(file)
                .filter(File::exists).map(Filesystem::readLines)
                .map(e -> e.stream()
                        .filter(e2 -> !e2.isBlank())
                        .filter(e2 -> !e2.trim().startsWith("#"))
                        .collect(Collectors.toList()))
                .filter(e -> !e.isEmpty())
                .orElseThrow();
    }
    
    public static List<String> readPropertyList(File file) {
        return readPropertyList(file, false);
    }
    
    public static List<String> readPropertyList(String fileName, boolean raw) {
        return readPropertyList(new File(Project.DATA_DIR, fileName), raw);
    }
    
    public static List<String> readPropertyList(String fileName) {
        return readPropertyList(fileName, false);
    }
    
}
