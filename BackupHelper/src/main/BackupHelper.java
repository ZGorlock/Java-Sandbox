/*
 * File:    BackupHelper.java
 * Package: main
 * Author:  Zachary Gill
 */

package main;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import commons.access.Filesystem;
import commons.access.Project;
import commons.object.string.StringUtility;
import commons.time.DateTimeUtility;
import main.util.BackupUtil;
import main.util.Drive;
import main.util.WindowsBackupTools;

public class BackupHelper {
    
    //Main Methods
    
    public static void main(String[] args) throws Exception {
        final long startTime = System.currentTimeMillis();
        System.out.println("" +
                "  ____             _                  _   _      _                 \n" +
                " | __ )  __ _  ___| | ___   _ _ __   | | | | ___| |_ __   ___ _ __ \n" +
                " |  _ \\ / _` |/ __| |/ / | | | '_ \\  | |_| |/ _ \\ | '_ \\ / _ \\ '__|\n" +
                " | |_) | (_| | (__|   <| |_| | |_) | |  _  |  __/ | |_) |  __/ |   \n" +
                " |____/ \\__,_|\\___|_|\\_\\\\__,_| .__/  |_| |_|\\___|_| .__/ \\___|_|   \n" +
                "                             |_|                  |_|              ");
        
        backupDocuments();
        
        backupSpecimens();
        
        backupCoding();
        backupMaven();
        
        backupRunelite();
        
        backupData();
        backupRegistry();
        backupManifest();
        backupRecoveryDrive();
        
        backupSavedData();
        backupSavedSettings();
        backupUtilities();
        backupTweaks();
        
        backupDevices();
        backupWorkPC();
        
        backupWindows();
        
        syncExternalBackup();
        
        final long endTime = System.currentTimeMillis();
        System.out.println("\n\n\nBackup Complete in " + DateTimeUtility.durationToDurationString(
                (endTime - startTime), true, false, true));
    }
    
    
    //Static Methods
    
    private static void backupDocuments() {
        System.out.println("\n\n\n--- DOCUMENTS ---\n");
        BackupUtil.clearTmpDir();
        
        final String backupName = "Documents";
        
        final File localDir = new File(Drive.STORAGE.drive, backupName);
        final File localBackupDir = new File(Drive.STORAGE.drive, Filesystem.generatePath("Other", "Backup", "Backups"));
        final File backupDir = new File(Drive.BACKUP.drive, "Backups");
        
        if (!BackupUtil.monthlyBackupExists(localBackupDir, backupName)) {
            
            final File documentsCache = new File(Filesystem.getTemporaryDirectory(), backupName);
            BackupUtil.makeBackupCache(documentsCache);
            
            BackupUtil.addToBackupCache(documentsCache, new File(localDir, "DnD"), List.of("Campaigns", "Tools"));
            BackupUtil.addToBackupCache(documentsCache, new File(localDir, "Housing"));
            BackupUtil.addToBackupCache(documentsCache, new File(localDir, "Money"));
            BackupUtil.addToBackupCache(documentsCache, new File(localDir, "Other"), List.of("Car", "Cat", "Doctor", "Genealogy", "Music", "PC", "Tesla Coil"));
            BackupUtil.addToBackupCache(documentsCache, new File(localDir, "Resume"));
            BackupUtil.addToBackupCache(documentsCache, new File(localDir, "Work"), List.of("REST"), true);
            BackupUtil.addToBackupCache(documentsCache, new File(localDir, "Writing"));
            
            final File documentsBackup = BackupUtil.compressBackupCache(documentsCache, BackupUtil.Stamper.stamp(backupName));
            BackupUtil.commitBackup(localBackupDir, documentsBackup);
            BackupUtil.cleanBackupDir(localBackupDir, backupName, 1);
        }
        
        BackupUtil.syncBackupDir(localBackupDir, backupDir, backupName);
    }
    
    private static void backupSpecimens() {
        System.out.println("\n\n\n--- SPECIMENS ---\n");
        BackupUtil.clearTmpDir();
        
        final String backupName = "Specimens";
        
        final File localDir = new File(Drive.STORAGE.drive, Filesystem.generatePath("Documents", backupName));
        final File localBackupDir = new File(Drive.STORAGE.drive, Filesystem.generatePath("Other", "Backup", "Backups"));
        final File backupDir = new File(Drive.BACKUP.drive, "Backups");
        
        if (!BackupUtil.monthlyBackupExists(localBackupDir, backupName)) {
            
            final File specimensBackup = BackupUtil.compressBackupCache(localDir, BackupUtil.Stamper.stamp(backupName));
            BackupUtil.commitBackup(localBackupDir, specimensBackup);
            BackupUtil.cleanBackupDir(localBackupDir, backupName, 1);
        }
        
        BackupUtil.syncBackupDir(localBackupDir, backupDir, backupName);
    }
    
    private static void backupCoding() {
        System.out.println("\n\n\n--- CODING ---\n");
        BackupUtil.clearTmpDir();
        
        final String backupName = "Coding";
        
        final File localDir = Drive.CODING.drive;
        final File localBackupDir = new File(Drive.STORAGE.drive, Filesystem.generatePath("Other", "Backup", backupName));
        final File backupDir = new File(Drive.BACKUP.drive, backupName);
        
        for (String language : List.of("C", "C#", "C++", "Github", "Haskell", "HTML", "Java", "Javascript", "Python", "QB64", "VB")) {
            
            System.out.println("\n--- Backing up " + language + " ---\n");
            
            if (!BackupUtil.monthlyBackupExists(localBackupDir, language)) {
                
                final File languageLocalDir = new File(localDir, language);
                
                final File languageBackup = BackupUtil.compressBackupCache(languageLocalDir, BackupUtil.Stamper.stamp(language));
                BackupUtil.commitBackup(localBackupDir, languageBackup);
                BackupUtil.cleanBackupDir(localBackupDir, language, 1);
            }
            
            System.out.println("\n-------------------" + StringUtility.fillStringOfLength('-', language.length()) + "\n");
        }
        
        BackupUtil.syncBackupDir(localBackupDir, backupDir);
    }
    
    private static void backupMaven() {
        System.out.println("\n\n\n--- MAVEN ---\n");
        BackupUtil.clearTmpDir();
        
        final String backupName = "Maven";
        
        final String userName = Filesystem.readFileToString(new File(Project.DATA_DIR, "name-user.txt"));
        
        final File localDir = new File(Drive.BOOT.drive, Filesystem.generatePath("Users", userName, ".m2"));
        final File localBackupDir = new File(Drive.STORAGE.drive, Filesystem.generatePath("Other", "Backup", "Backups"));
        final File backupDir = new File(Drive.BACKUP.drive, "Backups");
        
        if (!BackupUtil.monthlyBackupExists(localBackupDir, backupName)) {
            
            final File mavenBackup = BackupUtil.compressBackupCache(localDir, BackupUtil.Stamper.stamp(backupName));
            BackupUtil.commitBackup(localBackupDir, mavenBackup);
            BackupUtil.cleanBackupDir(localBackupDir, backupName, 1);
        }
        
        BackupUtil.syncBackupDir(localBackupDir, backupDir, backupName);
    }
    
    private static void backupRunelite() {
        System.out.println("\n\n\n--- RUNELITE ---\n");
        BackupUtil.clearTmpDir();
        
        final String backupName = "RuneLite";
        
        final File localDir = new File(Drive.GAMES.drive, Filesystem.generatePath(backupName, ".runelite"));
        final File localBackupDir = new File(Drive.STORAGE.drive, Filesystem.generatePath("Other", "Backup", "Backups"));
        final File backupDir = new File(Drive.BACKUP.drive, "Backups");
        
        if (!BackupUtil.monthlyBackupExists(localBackupDir, backupName)) {
            
            final File runeliteCache = new File(Filesystem.getTemporaryDirectory(), backupName);
            BackupUtil.makeBackupCache(runeliteCache);
            
            BackupUtil.addToBackupCache(runeliteCache, localDir, true, List.of("jagexcache", "jagexcache1", "repository2", "jagex_cl_oldschool_LIVE.dat", "jagex_cl_oldschool_LIVE1.dat", "random.dat"), true);
            
            final File runeliteBackup = BackupUtil.compressBackupCache(runeliteCache, BackupUtil.Stamper.stamp(backupName));
            BackupUtil.commitBackup(localBackupDir, runeliteBackup);
            BackupUtil.cleanBackupDir(localBackupDir, backupName, 1);
        }
        
        BackupUtil.syncBackupDir(localBackupDir, backupDir, backupName);
    }
    
    private static void backupData() {
        System.out.println("\n\n\n--- DATA ---\n");
        BackupUtil.clearTmpDir();
        
        final String backupName = "Data";
        final String programDataName = "ProgramData";
        final String appDataName = "AppData";
        final String userDataName = "User";
        
        final String userName = Filesystem.readFileToString(new File(Project.DATA_DIR, "name-user.txt"));
        
        final File localBackupDir = new File(Drive.STORAGE.drive, Filesystem.generatePath("Other", "Backup", backupName));
        final File backupDir = new File(Drive.BACKUP.drive, backupName);
        
        System.out.println("\n--- Backing up Program Data ---\n");
        
        if (!BackupUtil.monthlyBackupExists(localBackupDir, programDataName)) {
            
            final File programDataLocalDir = new File(Drive.BOOT.drive, programDataName);
            
            final File programDataCache = new File(Filesystem.getTemporaryDirectory(), programDataName);
            BackupUtil.makeBackupCache(programDataCache);
            
            BackupUtil.addToBackupCache(programDataCache, programDataLocalDir, true, List.of("Application Data", "Carbonite", "Desktop", "Documents", "ntuser.pol", "Package Cache", "Packages", "Start Menu", "Templates", "USOPrivate", "USOShared"), true);
            
            final File programDataBackup = BackupUtil.compressBackupCache(programDataCache, BackupUtil.Stamper.stamp(programDataName));
            BackupUtil.commitBackup(localBackupDir, programDataBackup, true);
            BackupUtil.cleanBackupDir(localBackupDir, programDataName, 1);
        }
        
        System.out.println("\n-------------------------------\n");
        
        System.out.println("\n--- Backing up App Data ---\n");
        
        if (!BackupUtil.monthlyBackupExists(localBackupDir, appDataName)) {
            
            final File appDataLocalDir = new File(Drive.BOOT.drive, Filesystem.generatePath("Users", userName, appDataName));
            
            final File appDataCache = new File(Filesystem.getTemporaryDirectory(), appDataName);
            BackupUtil.makeBackupCache(appDataCache);
            
            BackupUtil.addToBackupCache(appDataCache, new File(appDataLocalDir, "Local"), List.of("Comms", "Package Cache", "Packages", "PackageStaging", "Temp"), true);
            BackupUtil.addToBackupCache(appDataCache, new File(appDataLocalDir, "LocalLow"), List.of("IGDump", "Temp"), true);
            BackupUtil.addToBackupCache(appDataCache, new File(appDataLocalDir, "Roaming"), List.of(), true);
            
            final File appDataBackup = BackupUtil.compressBackupCache(appDataCache, BackupUtil.Stamper.stamp(appDataName));
            BackupUtil.commitBackup(localBackupDir, appDataBackup);
            BackupUtil.cleanBackupDir(localBackupDir, appDataName, 1);
        }
        
        System.out.println("\n---------------------------\n");
        
        System.out.println("\n--- Backing up User Data ---\n");
        
        if (!BackupUtil.monthlyBackupExists(localBackupDir, userDataName)) {
            
            final File userDataLocalDir = new File(Drive.BOOT.drive, Filesystem.generatePath("Users", userName));
            
            final File userDataCache = new File(Filesystem.getTemporaryDirectory(), userDataName);
            BackupUtil.makeBackupCache(userDataCache);
            
            BackupUtil.addToBackupCache(userDataCache, userDataLocalDir, true, List.of(".m2", ".runelite", "AppData", "Downloads", "jagexcache"), true);
            
            final File userDataBackup = BackupUtil.compressBackupCache(userDataCache, BackupUtil.Stamper.stamp(userDataName));
            BackupUtil.commitBackup(localBackupDir, userDataBackup, true);
            BackupUtil.cleanBackupDir(localBackupDir, userDataName, 1);
        }
        
        System.out.println("\n----------------------------\n");
        
        final Date backupDate = BackupUtil.Search.getNewestDate(localBackupDir);
        final File backupDateDir = new File(backupDir, BackupUtil.Stamper.formatDate(backupDate));
        
        BackupUtil.syncBackupDir(localBackupDir, backupDateDir);
        BackupUtil.cleanBackupDir(backupDir, 4);
    }
    
    private static void backupRegistry() {
        System.out.println("\n\n\n--- REGISTRY ---\n");
        BackupUtil.clearTmpDir();
        
        final String backupName = "Registry";
        
        final File localBackupDir = new File(Drive.STORAGE.drive, Filesystem.generatePath("Other", "Backup", backupName));
        final File backupDir = new File(Drive.BACKUP.drive, backupName);
        
        if (!BackupUtil.monthlyBackupExists(localBackupDir)) {
            
            final File registryCacheEntry = new File(Filesystem.getTemporaryDirectory(), BackupUtil.Stamper.stamp(".reg"));
            BackupUtil.makeBackupCache(registryCacheEntry.getParentFile());
            
            WindowsBackupTools.exportRegistry(registryCacheEntry);
            
            final File registryBackup = BackupUtil.compressBackupCache(registryCacheEntry, BackupUtil.Stamper.stamp(backupName));
            BackupUtil.commitBackup(localBackupDir, registryBackup);
        }
        
        BackupUtil.syncBackupDir(localBackupDir, backupDir);
    }
    
    private static void backupManifest() {
        System.out.println("\n\n\n--- MANIFEST ---\n");
        BackupUtil.clearTmpDir();
        
        final String backupName = "Manifest";
        
        final File localBackupDir = new File(Drive.STORAGE.drive, Filesystem.generatePath("Other", "Backup", backupName));
        final File backupDir = new File(Drive.BACKUP.drive, backupName);
        
        if (!BackupUtil.monthlyBackupExists(localBackupDir)) {
            
            final File manifestCache = new File(Filesystem.getTemporaryDirectory(), backupName);
            BackupUtil.makeBackupCache(manifestCache);
            
            for (Drive drive : Drive.values()) {
                final File manifestEntry = new File(manifestCache, BackupUtil.Stamper.stamp(drive.driveLetter + ".txt"));
                WindowsBackupTools.createManifest(drive.drive, manifestEntry);
            }
            
            final File manifestBackup = BackupUtil.compressBackupCache(manifestCache, true, BackupUtil.Stamper.stamp(backupName));
            BackupUtil.commitBackup(localBackupDir, manifestBackup);
        }
        
        BackupUtil.syncBackupDir(localBackupDir, backupDir);
    }
    
    private static void backupRecoveryDrive() {
        System.out.println("\n\n\n--- RECOVERY DRIVE ---\n");
        BackupUtil.clearTmpDir();
        
        final String backupName = "Recovery";
        
        final String password = Filesystem.readFileToString(new File(Project.DATA_DIR, "pass-recovery.txt"));
        
        final File localDir = Drive.RECOVERY.drive;
        final File localBackupDir = new File(Drive.STORAGE.drive, Filesystem.generatePath("Other", "Backup", "Backups"));
        final File backupDir = new File(Drive.BACKUP.drive, "Backups");
        
        if (!BackupUtil.monthlyBackupExists(localBackupDir, backupName)) {
            
            final File recoveryCache = new File(Filesystem.getTemporaryDirectory(), backupName);
            BackupUtil.makeBackupCache(recoveryCache);
            
            BackupUtil.addToBackupCache(recoveryCache, localDir, true);
            
            final File recoveryBackup = BackupUtil.compressBackupCache(recoveryCache, true, BackupUtil.Stamper.stamp(backupName), false, password);
            BackupUtil.commitBackup(localBackupDir, recoveryBackup);
            BackupUtil.cleanBackupDir(localBackupDir, backupName, 1);
        }
        
        BackupUtil.syncBackupDir(localBackupDir, backupDir, backupName);
    }
    
    private static void backupSavedData() {
        System.out.println("\n\n\n--- SAVED DATA ---\n");
        BackupUtil.clearTmpDir();
        
        final String backupName = "Data";
        
        final File localDir = new File(Drive.STORAGE.drive, Filesystem.generatePath("Other", "Backup", "Backups", backupName));
        final File backupDir = new File(Drive.BACKUP.drive, Filesystem.generatePath("Backups", backupName));
        
        BackupUtil.syncBackupDir(localDir, backupDir);
    }
    
    private static void backupSavedSettings() {
        System.out.println("\n\n\n--- SAVED SETTINGS ---\n");
        BackupUtil.clearTmpDir();
        
        final String backupName = "Settings";
        
        final File localDir = new File(Drive.STORAGE.drive, Filesystem.generatePath("Other", "Backup", "Backups", backupName));
        final File backupDir = new File(Drive.BACKUP.drive, Filesystem.generatePath("Backups", backupName));
        
        BackupUtil.syncBackupDir(localDir, backupDir);
    }
    
    private static void backupUtilities() {
        System.out.println("\n\n\n--- UTILITIES ---\n");
        BackupUtil.clearTmpDir();
        
        final String backupName = "Utilities";
        
        final File localDir = new File(Drive.STORAGE.drive, Filesystem.generatePath("Other", backupName));
        final File backupDir = new File(Drive.BACKUP.drive, backupName);
        
        BackupUtil.syncBackupDir(localDir, backupDir);
    }
    
    private static void backupTweaks() {
        System.out.println("\n\n\n--- TWEAKS ---\n");
        BackupUtil.clearTmpDir();
        
        final String backupName = "Tweaks";
        
        final File localDir = new File(Drive.STORAGE.drive, Filesystem.generatePath("Other", backupName));
        final File backupDir = new File(Drive.BACKUP.drive, backupName);
        
        BackupUtil.syncBackupDir(localDir, backupDir);
    }
    
    private static void backupDevices() {
        System.out.println("\n\n\n--- DEVICES ---\n");
        BackupUtil.clearTmpDir();
        
        final String backupName = "Devices";
        
        final File localDir = new File(Drive.CODING.drive, backupName);
        final File localBackupDir = new File(Drive.STORAGE.drive, Filesystem.generatePath("Other", "Backup", backupName));
        final File backupDir = new File(Drive.BACKUP.drive, backupName);
        
        for (File deviceTypeDir : Filesystem.getDirs(localDir)) {
            for (File deviceDir : Filesystem.getDirs(new File(deviceTypeDir, "Backups"))) {
                
                final File deviceLocalDir = Optional.ofNullable(BackupUtil.Search.getNewest(deviceDir)).orElse(deviceDir);
                final File deviceLocalBackupDir = new File(localBackupDir, Filesystem.generatePath(deviceTypeDir.getName(), deviceDir.getName()));
                
                BackupUtil.syncBackupDir(deviceLocalDir, deviceLocalBackupDir);
            }
        }
        
        BackupUtil.syncBackupDir(localBackupDir, backupDir);
    }
    
    private static void backupWorkPC() {
        System.out.println("\n\n\n--- WORK PC ---\n");
        BackupUtil.clearTmpDir();
        
        final String backupName = "Work PC";
        
        final String name = Filesystem.readFileToString(new File(Project.DATA_DIR, "name-workPc.txt"));
        final String password = Filesystem.readFileToString(new File(Project.DATA_DIR, "pass-workPc.txt"));
        
        final File localDir = new File(Drive.VIRTUAL_MACHINES.drive, backupName);
        final File localDiskDir = Drive.WORK.drive;
        final File localBackupDir = new File(localDir, "Backup");
        final File localDiskBackupDir = new File(localDiskDir, "Backup");
        final File backupDir = new File(Drive.BACKUP.drive, backupName);
        
        if (!BackupUtil.monthlyBackupExists(localBackupDir, name)) {
            
            final File workPcCache = new File(Filesystem.getTemporaryDirectory(), name);
            BackupUtil.makeBackupCache(workPcCache);
            
            BackupUtil.addToBackupCache(workPcCache, localDir, true, List.of(localBackupDir.getName()), true);
            BackupUtil.addToBackupCache(workPcCache, localDiskDir, true, List.of(localDiskBackupDir.getName()), true);
            
            final File workPcBackup = BackupUtil.compressBackupCache(workPcCache, BackupUtil.Stamper.stamp(name).replace("-", " - "), false, password);
            BackupUtil.commitBackup(localBackupDir, workPcBackup);
            BackupUtil.cleanBackupDir(localBackupDir, name, 4);
        }
        
        BackupUtil.syncBackupDir(localBackupDir, localDiskBackupDir, name);
        BackupUtil.syncBackupDir(localBackupDir, backupDir, name);
    }
    
    private static void backupWindows() {
        System.out.println("\n\n\n--- WINDOWS ---\n");
        BackupUtil.clearTmpDir();
        
        final String backupName = "Windows";
        
        final String backupTarget = "\\\\localhost\\" + Drive.BACKUP.driveLetter + "$\\" + backupName + "\\";
        
        final File backupDir = new File(Drive.BACKUP.drive, backupName);
        
        if (!WindowsBackupTools.monthlyBackupExists(backupTarget)) {
            
            WindowsBackupTools.createSystemImage(backupTarget);
        }
    }
    
    private static void syncExternalBackup() {
        System.out.println("\n\n\n--- SYNC EXTERNAL BACKUP ---\n");
        BackupUtil.clearTmpDir();
        
        final File localBackupDir = Drive.BACKUP.drive;
        final File backupDir = Drive.EXTERNAL_BACKUP.drive;
        
        BackupUtil.rsyncBackupDir(localBackupDir, backupDir);
    }
    
}