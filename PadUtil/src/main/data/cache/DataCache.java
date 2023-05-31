/*
 * File:    DataCache.java
 * Package: main.data.cache
 * Author:  Zachary Gill
 */

package main.data.cache;

import java.io.File;
import java.util.List;
import java.util.Optional;

import commons.access.Filesystem;

public class DataCache {
    
    //Static Methods
    
    public static Optional<File> getLocalFile(String sourceUrl) {
        return Optional.ofNullable(sourceUrl)
                .map(url -> url.replaceAll("^.+://", ""))
                .map(sourcePath -> new File(DataMirror.MIRROR_DIR, sourcePath));
    }
    
    public static Optional<File> getPageDir(String pagePath) {
        return Optional.ofNullable(pagePath)
                .map(dir -> new File(DataMirror.DIR_BASE, dir));
    }
    
    public static Optional<List<File>> getAllPageFiles(String pagePath) {
        return Optional.of(getPageDir(pagePath).orElse(DataMirror.DIR_BASE))
                .filter(File::exists)
                .map(Filesystem::getFilesRecursively);
    }
    
    public static Optional<List<File>> getAllPageFiles() {
        return getAllPageFiles(null);
    }
    
    public static Optional<File> getPageFile(String pagePath, String pageName) {
        return Optional.of(getPageDir(pagePath).orElse(DataMirror.DIR_BASE))
                .flatMap(pageDir -> Optional.ofNullable(pageName)
                        .map(name -> new File(pageDir, name)));
    }
    
    public static Optional<File> getPageFile(String pageName) {
        return getPageFile(null, pageName);
    }
    
    public static Optional<File> getResourceDir(String resourcePath) {
        return Optional.ofNullable(resourcePath)
                .map(dir -> new File(DataMirror.DIR_STATIC, dir));
    }
    
    public static Optional<List<File>> getAllResourceFiles(String resourcePath) {
        return Optional.of(getResourceDir(resourcePath).orElse(DataMirror.DIR_STATIC))
                .filter(File::exists)
                .map(Filesystem::getFilesRecursively);
    }
    
    public static Optional<List<File>> getAllResourceFiles() {
        return getAllResourceFiles(null);
    }
    
    public static Optional<File> getResourceFile(String resourcePath, String resourceName) {
        return Optional.of(getResourceDir(resourcePath).orElse(DataMirror.DIR_STATIC))
                .flatMap(resourceDir -> Optional.ofNullable(resourceName)
                        .map(name -> new File(resourceDir, name)));
    }
    
    public static Optional<File> getResourceFile(String resourceName) {
        return getResourceFile(null, resourceName);
    }
    
    public static boolean localFileExists(File resource) {
        return Optional.ofNullable(resource)
                .map(e -> (e.exists() && !Filesystem.isEmpty(e)))
                .orElse(false);
    }
    
    public static boolean localFileNotExists(File resource) {
        return !localFileExists(resource);
    }
}
