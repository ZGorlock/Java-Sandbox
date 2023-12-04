/*
 * File:    XCopyUtil.java
 * Package: main.util
 * Author:  Zachary Gill
 */

package main.util;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import commons.access.CmdLine;
import commons.access.Filesystem;
import commons.io.console.ProgressBar;
import commons.object.collection.ListUtility;
import commons.object.string.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class XCopyUtil {
    
    //Logger
    
    private static final Logger logger = LoggerFactory.getLogger(XCopyUtil.class);
    
    
    //Static Methods
    
    public static boolean xcopy(File source, File target, boolean showProgressBar) {
        final String xcopy = xcopyCmd(source, target);
        
        final XCopyProgressBar progressBar = showProgressBar ? new XCopyProgressBar(source, target) : null;
        
        try {
            final String response = CmdLine.executeCmd(xcopy, progressBar);
            return Optional.ofNullable(response)
                    .filter(e -> !StringUtility.isNullOrBlank(e))
                    .map(e -> !e.contains("[*]"))
                    .orElse(false);
            
        } catch (Exception e) {
            if (progressBar != null) {
                progressBar.fail();
            }
        } finally {
            if (progressBar != null) {
                progressBar.complete();
            }
        }
        return false;
    }
    
    public static boolean xcopy(File source, File target) {
        return xcopy(source, target, false);
    }
    
    private static String xcopyCmd(File sourceFile, File targetFile) {
        final String source = sourceFile.getAbsolutePath();
        final String target = sourceFile.isDirectory() ? targetFile.getAbsolutePath() : (targetFile.getParentFile().getAbsolutePath() + "\\");
        
        return String.join(" ",
                "xcopy",
                StringUtility.quote(source),
                StringUtility.quote(target),
                getFlags(sourceFile, targetFile));
    }
    
    private static String getFlags(File source, File target, String startDate, List<String> excluded,
            boolean includeArchive, boolean unsetArchive, boolean includeHidden, boolean ignoreEncryption,
            boolean includeFolders, boolean includeEmptyFolders, boolean preserveLinks,
            boolean copyAttributes, boolean copyOwnership, boolean copyAuditSettings,
            boolean overwriteFiles, boolean overwriteReadOnly, boolean assumeDestinationIsFolder,
            boolean ignoreErrors, boolean folderStructureOnly, boolean onlyExisting,
            boolean listFilesOnly, boolean logFullPaths, boolean logQuiet) {
        return Stream.of(
                        (includeArchive ? "/a" : ""),
                        ((includeArchive && unsetArchive) ? "/m" : ""),
                        (includeHidden ? "/h" : ""),
                        (ignoreEncryption ? "/g" : ""),
                        (!StringUtility.isNullOrBlank(startDate) ? ("/d:" + startDate) : ""),
                        (!ListUtility.isNullOrEmpty(excluded) ? ("/exclude:" + String.join("+", excluded)) : ""),
                        (includeFolders ? (includeEmptyFolders ? "/e" : "/s") : ""),
                        (preserveLinks ? "/b" : ""),
                        (copyAttributes ? "/k" : ""),
                        ((copyOwnership && !copyAuditSettings) ? "/o" : ""),
                        (copyAuditSettings ? "/x" : ""),
                        (overwriteFiles ? "/y" : "/-y"),
                        ((overwriteFiles && overwriteReadOnly) ? "/r" : ""),
                        (assumeDestinationIsFolder ? "/i" : ""),
                        (ignoreErrors ? "/c" : ""),
                        (folderStructureOnly ? "/t" : ""),
                        (onlyExisting ? "/u" : ""),
                        (listFilesOnly ? "/l" : ""),
                        ((logFullPaths && !logQuiet) ? "/f" : ""),
                        (logQuiet ? "/q" : "")
                )
                .filter(e -> !StringUtility.isNullOrBlank(e))
                .collect(Collectors.joining(" "));
    }
    
    private static String getFlags(File sourceFile, File targetFile) {
        return getFlags(sourceFile, targetFile, null, null,
                true, false, true, false,
                sourceFile.isDirectory(), sourceFile.isDirectory(), true,
                true, false, false,
                true, true, sourceFile.isDirectory(),
                true, false, false,
                false, false, false);
    }
    
    
    //Inner Classes
    
    private static class XCopyProgressBar extends ProgressBar {
        
        //Constants
        
        private static final Pattern COMPLETED_LOG_PATTERN = Pattern.compile("(?<total>\\d+) File\\(s\\) copied");
        
        
        //Constructors
        
        public XCopyProgressBar(File source, File target) {
            super(target.getName(), (source.isFile() ? 1 : Filesystem.getFilesRecursively(source).size()));
        }
        
        
        //Methods
        
        @Override
        public synchronized boolean processLog(String log, boolean error) {
//            logger.trace(log);
            return Optional.ofNullable(log).map(COMPLETED_LOG_PATTERN::matcher)
                    .filter(Matcher::matches)
                    .map(logMatcher -> logMatcher.group("total")).map(Integer::parseInt)
                    .map(this::processCompletion)
                    .orElseGet(this::addOne);
        }
        
        private synchronized boolean processCompletion(int total) {
            updateTotal(total);
            complete();
            return true;
        }
        
    }
    
}
