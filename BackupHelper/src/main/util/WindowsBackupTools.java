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

public final class WindowsBackupTools {
    
    //Static Methods
    
    public static void exportRegistry(File regFile) {
        System.out.println("Exporting registry...");
        if (!BackupUtil.TEST_MODE) {
            CmdLine.executeCmd("regedit /e " + StringUtility.quote(regFile.getAbsolutePath()));
        }
    }
    
    public static void createManifest(File location, File manifestFile) {
        System.out.println(StringUtility.format("Creating manifest of {}...", StringUtility.quote(location.getAbsolutePath(), true)));
        if (!BackupUtil.TEST_MODE) {
            CmdLine.executeCmd("dir " + StringUtility.quote(location.getAbsolutePath()) + " /s /b > " + StringUtility.quote(manifestFile.getAbsolutePath()));
        }
    }
    
    public static void createSystemImage(String backupTarget) {
        System.out.println("Creating system image...");
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
    
    public static boolean monthlyBackupExists(String backupTarget) {
        if (BackupUtil.CHECK_RECENT) {
            System.out.println("Checking for existing monthly system backup");
            
            if (BackupUtil.ASSUME_RECENT_EXISTS) {
                System.out.println(BackupUtil.ERROR + "Assuming existing backup was found");
                return true;
                
            } else {
                final List<Date> backupDates = systemImageBackupDates(backupTarget);
                final Date latestBackupDate = backupDates.stream().sorted(Comparator.naturalOrder()).limit(1).findFirst().orElse(null);
                
                if (((latestBackupDate != null) && (latestBackupDate.compareTo(BackupUtil.Stamper.ONE_MONTH_AGO) >= 0))) {
                    System.out.println(BackupUtil.INDENT + StringUtility.format("Found existing backup from: {}", BackupUtil.Log.logStamp(latestBackupDate)));
                    return true;
                }
            }
            
            System.out.println(BackupUtil.INDENT + "No existing backup found");
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
