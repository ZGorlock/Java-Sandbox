/*
 * File:    SyncUtil.java
 * Package: main.util
 * Author:  Zachary Gill
 */

package main.util;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import commons.access.Filesystem;
import commons.io.console.ProgressBar;
import commons.lambda.stream.mapper.Mappers;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SyncUtil {
    
    //Logger
    
    private static final Logger logger = LoggerFactory.getLogger(SyncUtil.class);
    
    
    //Constants
    
    private static final boolean USE_PROGRESS_BAR = false;
    
    
    //Static Methods
    
    public static boolean sync(File sourceDir, File targetDir, String baseName, List<String> fileExclusions) {
        final boolean additions = performAdditions(sourceDir, targetDir, baseName, fileExclusions);
        final boolean removals = performRemovals(sourceDir, targetDir, baseName, fileExclusions);
        
        return additions && removals;
    }
    
    public static boolean syncMonitored(File sourceDir, File targetDir, String baseName, List<String> fileExclusions) {
        final List<Map.Entry<File, File>> additions = findAdditions(sourceDir, targetDir, baseName, fileExclusions);
        final List<Map.Entry<File, File>> removals = findRemovals(sourceDir, targetDir, baseName, fileExclusions);
        
        final List<Map.Entry<File, File>> actions = Stream.of(additions, removals)
                .flatMap(Collection::stream).collect(Collectors.toList());
        
        if (actions.isEmpty()) {
            return true;
        }
        final ProgressBar progressBar = USE_PROGRESS_BAR ? new ProgressBar(targetDir.getName(), actions.size()) : null;
        
        boolean success = false;
        try {
            success = actions.stream()
                    .map(e -> e.getKey().exists() ?
                            BackupUtil.Action.copy(e.getKey(), e.getValue()) :
                            BackupUtil.Action.delete(e.getValue()))
                    .map(Mappers.forEach(e -> Optional.ofNullable(progressBar).ifPresent(ProgressBar::addOne)))
                    .reduce(true, Boolean::logicalAnd);
            
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (progressBar != null) {
                if (success) {
                    progressBar.complete();
                } else {
                    progressBar.fail();
                }
            }
        }
        
        return success;
    }
    
    private static boolean performAdditions(File sourceDir, File targetDir, String baseName, List<String> fileExclusions) {
        return addStream(sourceDir, targetDir, baseName, fileExclusions)
                .map(e -> BackupUtil.Action.copy(e.getKey(), e.getValue()))
                .reduce(true, Boolean::logicalAnd);
    }
    
    private static Stream<Map.Entry<File, File>> addStream(File sourceDir, File targetDir, String baseName, List<String> fileExclusions) {
        return Filesystem.getFilesAndDirsRecursively(sourceDir).stream()
                .map(e -> Map.entry(e, mapToBackup(e, sourceDir, targetDir)))
                .filter(e -> (isTarget(e.getKey(), baseName) && !isExcluded(e.getKey(), fileExclusions)))
                .filter(e -> (!e.getValue().exists() || (e.getKey().isFile() && FileUtils.isFileNewer(e.getKey(), e.getValue()))));
    }
    
    public static List<Map.Entry<File, File>> findAdditions(File sourceDir, File targetDir, String baseName, List<String> fileExclusions) {
        final Set<File> added = new HashSet<>();
        return addStream(sourceDir, targetDir, baseName, fileExclusions)
                .filter(e -> !added.add(e.getKey())).filter(e -> !e.getKey().isDirectory() || !added.addAll(Filesystem.getFilesAndDirsRecursively(e.getKey())))
                .collect(Collectors.toList());
    }
    
    private static boolean performRemovals(File sourceDir, File targetDir, String baseName, List<String> fileExclusions) {
        return removalStream(sourceDir, targetDir, baseName, fileExclusions)
                .map(e -> BackupUtil.Action.delete(e.getValue()))
                .reduce(true, Boolean::logicalAnd);
    }
    
    private static Stream<Map.Entry<File, File>> removalStream(File sourceDir, File targetDir, String baseName, List<String> fileExclusions) {
        return Filesystem.getFilesAndDirsRecursively(targetDir).stream()
                .map(e -> Map.entry(mapToSource(e, sourceDir, targetDir), e))
                .filter(e -> ((isTarget(e.getValue(), baseName) && !e.getKey().exists()) || isExcluded(e.getValue(), fileExclusions)));
    }
    
    public static List<Map.Entry<File, File>> findRemovals(File sourceDir, File targetDir, String baseName, List<String> fileExclusions) {
        final Set<File> removed = new HashSet<>();
        return removalStream(sourceDir, targetDir, baseName, fileExclusions)
                .filter(e -> !removed.add(e.getKey())).filter(e -> !e.getKey().isDirectory() || !removed.addAll(Filesystem.getFilesAndDirsRecursively(e.getKey())))
                .collect(Collectors.toList());
    }
    
    private static File mapFile(File file, Map.Entry<File, File> dirChange) {
        return new File(file.getAbsolutePath()
                .replace(dirChange.getKey().getAbsolutePath(), dirChange.getValue().getAbsolutePath()))
                .getAbsoluteFile();
    }
    
    public static File mapToBackup(File sourceFile, File sourceDir, File targetDir) {
        return mapFile(sourceFile, Map.entry(sourceDir, targetDir));
    }
    
    public static File mapToSource(File backupFile, File sourceDir, File targetDir) {
        return mapFile(backupFile, Map.entry(targetDir, sourceDir));
    }
    
    public static boolean isTarget(File file, String baseName) {
        return Optional.ofNullable(baseName).filter(e -> !e.isBlank())
                .map(e -> BackupUtil.Stamper.baseName(file).equals(e))
                .orElse(true);
    }
    
    public static boolean isExcluded(File file, List<String> fileExclusions) {
        return Stream.of(BackupUtil.BLACKLIST, fileExclusions)
                .filter(Objects::nonNull).flatMap(Collection::stream)
                .anyMatch(e -> file.getAbsolutePath().replace("\\", "/")
                        .matches("^.*/" + Pattern.quote(e) + "(?:/.*)?$"));
    }
    
}
    
    
    
