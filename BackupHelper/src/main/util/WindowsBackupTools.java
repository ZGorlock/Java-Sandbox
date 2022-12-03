/*
 * File:    WindowsBackupTools.java
 * Package: main.util
 * Author:  Zachary Gill
 */

package main.util;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import commons.access.CmdLine;
import commons.io.console.ProgressBar;
import commons.object.string.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class WindowsBackupTools {
    
    //Logger
    
    private static final Logger logger = LoggerFactory.getLogger(WindowsBackupTools.class);
    
    
    //Static Methods
    
    public static void exportRegistry(File regFile) {
        logger.info("Exporting registry...");
        if (!BackupUtil.TEST_MODE) {
            CmdLine.executeCmd("regedit /e " + StringUtility.quote(regFile.getAbsolutePath()));
        }
    }
    
    public static void createManifest(File location, File manifestFile) {
        logger.info(StringUtility.format("Creating manifest of {}...", StringUtility.quote(location.getAbsolutePath(), true)));
        if (!BackupUtil.TEST_MODE) {
            CmdLine.executeCmd("dir " + StringUtility.quote(location.getAbsolutePath()) + " /s /b > " + StringUtility.quote(manifestFile.getAbsolutePath()));
        }
    }
    
    public static void createSystemImage(String backupTarget) {
        logger.info("Creating system image...");
        if (!BackupUtil.TEST_MODE) {
            CmdLine.executeCmd("wbadmin start backup" +
                            " -backupTarget:" + backupTarget +
                            " -include:C: -allCritical" +
                            " -vssCopy -quiet",
                    new SystemImageBackupProgressBar());
        }
    }
    
    public static List<Date> systemImageBackupDates(String backupTarget) {
        final String response = CmdLine.executeCmd("wbadmin get versions" +
                " -backupTarget:" + backupTarget);
        
        final SimpleDateFormat versionIdDateFormat = new SimpleDateFormat("MM/dd/yyyy-HH:mm");
        final Pattern versionIdPattern = Pattern.compile("^\\s*Version\\sidentifier:\\s(?<date>.+)$");
        
        final List<Date> backupDates = new ArrayList<>();
        for (String responseLine : StringUtility.splitLines(response)) {
            final Matcher versionIdMatcher = versionIdPattern.matcher(responseLine);
            if (versionIdMatcher.matches()) {
                final String versionId = versionIdMatcher.group("date");
                
                try {
                    backupDates.add(versionIdDateFormat.parse(versionId));
                } catch (ParseException ignored) {
                }
            }
        }
        return backupDates;
    }
    
    public static boolean recentSystemImageExists(String backupTarget) {
        if (BackupUtil.CHECK_RECENT) {
            logger.debug("Checking for recent system image");
            
            if (BackupUtil.ASSUME_RECENT_EXISTS) {
                logger.warn(BackupUtil.ERROR + "Assuming recent system image was found");
                return true;
                
            } else {
                final List<Date> backupDates = systemImageBackupDates(backupTarget);
                final Date latestBackupDate = backupDates.stream().sorted(Comparator.naturalOrder()).limit(1).findFirst().orElse(null);
                
                if (BackupUtil.Search.isRecent(latestBackupDate)) {
                    logger.debug(BackupUtil.INDENT + StringUtility.format("Found recent system image from: {}", BackupUtil.Log.logStamp(latestBackupDate)));
                    return true;
                }
            }
            
            logger.debug(BackupUtil.INDENT + "No recent system image found");
        }
        return false;
    }
    
    
    //Inner Classes
    
    private static class SystemImageBackupProgressBar extends ProgressBar {
        
        //Constants
        
        private static final Pattern PROGRESS_PATTERN = Pattern.compile("^\\s*Creating a backup of volume (?<volume>.+), copied \\((?<percent>\\d+)%\\)\\.\\s*$");
        
        
        //Constructors
        
        public SystemImageBackupProgressBar() {
            super("System Image Backup", 100);
            
            setShowRatio(false);
            setShowSpeed(false);
        }
        
        
        //Methods
        
        @Override
        public synchronized boolean processLog(String log, boolean error) {
            final Matcher progressMatcher = PROGRESS_PATTERN.matcher(log);
            if (progressMatcher.matches()) {
                final String volume = progressMatcher.group("volume");
                final int percent = Integer.parseInt(progressMatcher.group("percent"));
                
                return update(percent);
            }
            return false;
        }
        
    }
    
}
