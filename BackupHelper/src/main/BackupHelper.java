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
import main.util.PropertyUtil;
import main.util.WindowsBackupTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BackupHelper {
    
    //Logger
    
    private static final Logger logger = LoggerFactory.getLogger(BackupHelper.class);
    
    static {
        System.setProperty("logback.configurationFile", new File(Project.RESOURCES_DIR, "logback.xml").getAbsolutePath());
    }
    
    
    //Main Methods
    
    public static void main(String[] args) throws Exception {
        final long startTime = System.currentTimeMillis();
        logger.info("\n" +
                "  ____             _                  _   _      _                 \n" +
                " | __ )  __ _  ___| | ___   _ _ __   | | | | ___| |_ __   ___ _ __ \n" +
                " |  _ \\ / _` |/ __| |/ / | | | '_ \\  | |_| |/ _ \\ | '_ \\ / _ \\ '__|\n" +
                " | |_) | (_| | (__|   <| |_| | |_) | |  _  |  __/ | |_) |  __/ |   \n" +
                " |____/ \\__,_|\\___|_|\\_\\\\__,_| .__/  |_| |_|\\___|_| .__/ \\___|_|   \n" +
                "                             |_|                  |_|              ");
        
        if (BackupUtil.EXTERNAL_BACKUP_TYPE != BackupUtil.ExternalBackupType.PRESERVE) {
            syncExternalBackup();
        }
        
        backupDocuments();
        
        backupSpecimens();
        
        backupCoding();
        backupMaven();
        
        backupRuneScape();
        backupStableDiffusion();
        
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
        
        if (BackupUtil.EXTERNAL_BACKUP_TYPE != BackupUtil.ExternalBackupType.DUPLICATE) {
            syncExternalBackup();
        }
        
        final long endTime = System.currentTimeMillis();
        logger.info("\n\n\nBackup Complete in " + DateTimeUtility.durationToDurationString(
                (endTime - startTime), true, false, true));
    }
    
    
    //Static Methods
    
    private static void backupDocuments() {
        logger.info("\n\n\n--- DOCUMENTS ---\n");
        BackupUtil.clearTmpDir();
        
        final String backupName = "Documents";
        
        final File localDir = new File(Drive.STORAGE.drive, backupName);
        final File localBackupDir = new File(Drive.STORAGE.drive, Filesystem.generatePath("Other", "Backup", "Backups"));
        final File backupDir = new File(Drive.BACKUP.drive, "Backups");
        
        if (!BackupUtil.recentBackupExists(localBackupDir, backupName) && BackupUtil.modifiedSinceLastBackup(localDir, localBackupDir, backupName)) {
            
            final File documentsCache = new File(Filesystem.getTemporaryDirectory(), backupName);
            BackupUtil.makeBackupCache(documentsCache);
            
            BackupUtil.addToBackupCache(documentsCache, new File(localDir, "DnD"), List.of("Campaigns", "Tools"));
            BackupUtil.addToBackupCache(documentsCache, new File(localDir, "Health"));
            BackupUtil.addToBackupCache(documentsCache, new File(localDir, "Housing"));
            BackupUtil.addToBackupCache(documentsCache, new File(localDir, "Money"));
            BackupUtil.addToBackupCache(documentsCache, new File(localDir, "Other"), List.of("Car", "Cat", "Music", "Other", "Smelting", "Tesla Coil", "Wish List"));
            BackupUtil.addToBackupCache(documentsCache, new File(localDir, "PaD"));
            BackupUtil.addToBackupCache(documentsCache, new File(localDir, "PC"));
            BackupUtil.addToBackupCache(documentsCache, new File(localDir, "Resume"));
            BackupUtil.addToBackupCache(documentsCache, new File(localDir, "Runescape"));
            BackupUtil.addToBackupCache(documentsCache, new File(localDir, "Work"), List.of("REST"), true);
            BackupUtil.addToBackupCache(documentsCache, new File(localDir, "Writing"));
            
            final File documentsBackup = BackupUtil.compressBackupCache(documentsCache, BackupUtil.Stamper.stamp(backupName));
            BackupUtil.commitBackup(localBackupDir, documentsBackup);
        }
        BackupUtil.cleanBackupDir(localBackupDir, backupName, 1);
        
        BackupUtil.syncBackupDir(localBackupDir, backupDir, backupName);
    }
    
    private static void backupSpecimens() {
        logger.info("\n\n\n--- SPECIMENS ---\n");
        BackupUtil.clearTmpDir();
        
        final String backupName = "Specimens";
        
        final File localDir = new File(Drive.STORAGE.drive, Filesystem.generatePath("Documents", backupName));
        final File localBackupDir = new File(Drive.STORAGE.drive, Filesystem.generatePath("Other", "Backup", "Backups"));
        final File backupDir = new File(Drive.BACKUP.drive, "Backups");
        
        if (!BackupUtil.recentBackupExists(localBackupDir, backupName) && BackupUtil.modifiedSinceLastBackup(localDir, localBackupDir, backupName)) {
            
            final File specimensBackup = BackupUtil.compressBackupCache(localDir, BackupUtil.Stamper.stamp(backupName));
            BackupUtil.commitBackup(localBackupDir, specimensBackup);
        }
        BackupUtil.cleanBackupDir(localBackupDir, backupName, 1);
        
        BackupUtil.syncBackupDir(localBackupDir, backupDir, backupName);
    }
    
    private static void backupCoding() {
        logger.info("\n\n\n--- CODING ---\n");
        BackupUtil.clearTmpDir();
        
        final String backupName = "Coding";
        
        final File localDir = Drive.CODING.drive;
        final File localBackupDir = new File(Drive.STORAGE.drive, Filesystem.generatePath("Other", "Backup", backupName));
        final File backupDir = new File(Drive.BACKUP.drive, backupName);
        
        for (String language : List.of("C", "C#", "C++", "Haskell", "HTML", "Java", "Javascript", "Other", "Python", "QB64", "VB")) {
            
            logger.info("\n--- Backing up " + language + " ---\n");
            
            final File languageLocalDir = new File(localDir, language);
            
            if (!BackupUtil.recentBackupExists(localBackupDir, language) && BackupUtil.modifiedSinceLastBackup(languageLocalDir, localBackupDir, language)) {
                
                final File languageBackup = BackupUtil.compressBackupCache(languageLocalDir, BackupUtil.Stamper.stamp(language));
                BackupUtil.commitBackup(localBackupDir, languageBackup);
            }
            BackupUtil.cleanBackupDir(localBackupDir, language, 1);
            
            logger.info("\n-------------------" + StringUtility.fillStringOfLength('-', language.length()) + "\n");
        }
        
        BackupUtil.syncBackupDir(localBackupDir, backupDir);
    }
    
    private static void backupMaven() {
        logger.info("\n\n\n--- MAVEN ---\n");
        BackupUtil.clearTmpDir();
        
        final String backupName = "Maven";
        
        final String userName = PropertyUtil.readProperty("name-user.txt");
        
        final File localDir = new File(Drive.BOOT.drive, Filesystem.generatePath("Users", userName, ".m2"));
        final File localBackupDir = new File(Drive.STORAGE.drive, Filesystem.generatePath("Other", "Backup", "Backups"));
        final File backupDir = new File(Drive.BACKUP.drive, "Backups");
        
        if (!BackupUtil.recentBackupExists(localBackupDir, backupName)) {
            
            final File mavenBackup = BackupUtil.compressBackupCache(localDir, BackupUtil.Stamper.stamp(backupName));
            BackupUtil.commitBackup(localBackupDir, mavenBackup);
        }
        BackupUtil.cleanBackupDir(localBackupDir, backupName, 1);
        
        BackupUtil.syncBackupDir(localBackupDir, backupDir, backupName);
    }
    
    private static void backupRuneScape() {
        logger.info("\n\n\n--- RUNESCAPE ---\n");
        BackupUtil.clearTmpDir();
        
        final String backupName = "RuneScape";
        
        final File localDir = new File(Drive.GAMES.drive, Filesystem.generatePath("RuneScape"));
        final File localBackupDir = new File(Drive.STORAGE.drive, Filesystem.generatePath("Other", "Backup", "Backups"));
        final File backupDir = new File(Drive.BACKUP.drive, "Backups");
        
        if (!BackupUtil.recentBackupExists(localBackupDir, backupName)) {
            
            final File runeScapeCache = new File(Filesystem.getTemporaryDirectory(), backupName);
            BackupUtil.makeBackupCache(runeScapeCache);
            
            BackupUtil.addToBackupCache(runeScapeCache, localDir, true);
            
            final File runeScapeBackup = BackupUtil.compressBackupCache(runeScapeCache, BackupUtil.Stamper.stamp(backupName));
            BackupUtil.commitBackup(localBackupDir, runeScapeBackup);
        }
        BackupUtil.cleanBackupDir(localBackupDir, backupName, 1);
        
        BackupUtil.syncBackupDir(localBackupDir, backupDir, backupName);
    }
    
    private static void backupStableDiffusion() {
        logger.info("\n\n\n--- STABLE DIFFUSION ---\n");
        BackupUtil.clearTmpDir();
        
        final String backupName = "StableDiffusion";
        
        final String userName = PropertyUtil.readProperty("name-user.txt");
        
        final File localDir = new File(Drive.GAMES.drive, Filesystem.generatePath("Stable Diffusion", "Stable Diffusion"));
        final File localBackupDir = new File(Drive.STORAGE.drive, Filesystem.generatePath("Other", "Backup", "Backups"));
        final File backupDir = new File(Drive.BACKUP.drive, "Backups");
        
        if (!BackupUtil.recentBackupExists(localBackupDir, backupName) && BackupUtil.modifiedSinceLastBackup(localDir, localBackupDir, backupName)) {
            
            final File stableDiffusionBackup = BackupUtil.compressBackupFromSource(localDir, BackupUtil.Stamper.stamp(backupName));
            BackupUtil.commitBackup(localBackupDir, stableDiffusionBackup);
        }
        BackupUtil.cleanBackupDir(localBackupDir, backupName, 1);
        
        BackupUtil.syncBackupDir(localBackupDir, backupDir, backupName);
    }
    
    private static void backupData() {
        logger.info("\n\n\n--- DATA ---\n");
        BackupUtil.clearTmpDir();
        
        final String backupName = "Data";
        final String programDataName = "ProgramData";
        final String appDataName = "AppData";
        final String userDataName = "User";
        
        final String userName = PropertyUtil.readProperty("name-user.txt");
        
        final File localBackupDir = new File(Drive.STORAGE.drive, Filesystem.generatePath("Other", "Backup", backupName));
        final File backupDir = new File(Drive.BACKUP.drive, backupName);
        
        logger.info("\n--- Backing up Program Data ---\n");
        
        if (!BackupUtil.recentBackupExists(localBackupDir, programDataName)) {
            
            final File programDataLocalDir = new File(Drive.BOOT.drive, programDataName);
            
            final File programDataCache = new File(Filesystem.getTemporaryDirectory(), programDataName);
            BackupUtil.makeBackupCache(programDataCache);
            
            BackupUtil.addToBackupCache(programDataCache, programDataLocalDir, true, List.of("Application Data", "Carbonite", "Desktop", "Documents", "ntuser.pol", "Package Cache", "Packages", "Start Menu", "Templates", "USOPrivate", "USOShared"), true);
            
            final File programDataBackup = BackupUtil.compressBackupCache(programDataCache, BackupUtil.Stamper.stamp(programDataName));
            BackupUtil.commitBackup(localBackupDir, programDataBackup, true);
        }
        BackupUtil.cleanBackupDir(localBackupDir, programDataName, 1);
        
        logger.info("\n-------------------------------\n");
        
        logger.info("\n--- Backing up App Data ---\n");
        
        if (!BackupUtil.recentBackupExists(localBackupDir, appDataName)) {
            
            final File appDataLocalDir = new File(Drive.BOOT.drive, Filesystem.generatePath("Users", userName, appDataName));
            
            final File appDataCache = new File(Filesystem.getTemporaryDirectory(), appDataName);
            BackupUtil.makeBackupCache(appDataCache);
            
            BackupUtil.addToBackupCache(appDataCache, new File(appDataLocalDir, "Local"), List.of("Comms", "Package Cache", "Packages", "PackageStaging", "Temp"), true);
            BackupUtil.addToBackupCache(appDataCache, new File(appDataLocalDir, "LocalLow"), List.of("IGDump", "Temp"), true);
            BackupUtil.addToBackupCache(appDataCache, new File(appDataLocalDir, "Roaming"), List.of(), true);
            
            final File appDataBackup = BackupUtil.compressBackupCache(appDataCache, BackupUtil.Stamper.stamp(appDataName));
            BackupUtil.commitBackup(localBackupDir, appDataBackup);
        }
        BackupUtil.cleanBackupDir(localBackupDir, appDataName, 1);
        
        logger.info("\n---------------------------\n");
        
        logger.info("\n--- Backing up User Data ---\n");
        
        if (!BackupUtil.recentBackupExists(localBackupDir, userDataName)) {
            
            final File userDataLocalDir = new File(Drive.BOOT.drive, Filesystem.generatePath("Users", userName));
            
            final File userDataCache = new File(Filesystem.getTemporaryDirectory(), userDataName);
            BackupUtil.makeBackupCache(userDataCache);
            
            BackupUtil.addToBackupCache(userDataCache, userDataLocalDir, true, List.of(".m2", ".runelite", "AppData", "Downloads"), true);
            
            final File userDataBackup = BackupUtil.compressBackupCache(userDataCache, BackupUtil.Stamper.stamp(userDataName));
            BackupUtil.commitBackup(localBackupDir, userDataBackup, true);
        }
        BackupUtil.cleanBackupDir(localBackupDir, userDataName, 1);
        
        logger.info("\n----------------------------\n");
        
        final Date backupDate = BackupUtil.Search.getNewestDate(localBackupDir);
        final File backupDateDir = new File(backupDir, BackupUtil.Stamper.formatDate(backupDate));
        
        BackupUtil.syncBackupDir(localBackupDir, backupDateDir);
        BackupUtil.cleanBackupDir(backupDir, 4);
    }
    
    private static void backupRegistry() {
        logger.info("\n\n\n--- REGISTRY ---\n");
        BackupUtil.clearTmpDir();
        
        final String backupName = "Registry";
        
        final File localBackupDir = new File(Drive.STORAGE.drive, Filesystem.generatePath("Other", "Backup", backupName));
        final File backupDir = new File(Drive.BACKUP.drive, backupName);
        
        if (!BackupUtil.recentBackupExists(localBackupDir)) {
            
            final File registryCacheEntry = new File(Filesystem.getTemporaryDirectory(), BackupUtil.Stamper.stamp(".reg"));
            BackupUtil.makeBackupCache(registryCacheEntry.getParentFile());
            
            WindowsBackupTools.exportRegistry(registryCacheEntry);
            
            final File registryBackup = BackupUtil.compressBackupCache(registryCacheEntry, BackupUtil.Stamper.stamp(backupName));
            BackupUtil.commitBackup(localBackupDir, registryBackup);
        }
        
        BackupUtil.syncBackupDir(localBackupDir, backupDir);
    }
    
    private static void backupManifest() {
        logger.info("\n\n\n--- MANIFEST ---\n");
        BackupUtil.clearTmpDir();
        
        final String backupName = "Manifest";
        
        final File localBackupDir = new File(Drive.STORAGE.drive, Filesystem.generatePath("Other", "Backup", backupName));
        final File backupDir = new File(Drive.BACKUP.drive, backupName);
        
        if (!BackupUtil.recentBackupExists(localBackupDir)) {
            
            final File manifestCache = new File(Filesystem.getTemporaryDirectory(), backupName);
            BackupUtil.makeBackupCache(manifestCache);
            
            for (Drive drive : List.of(Drive.BOOT, Drive.GAMES, Drive.STORAGE, Drive.CODING, Drive.VIRTUAL_MACHINES, Drive.WORK, Drive.BACKUP, Drive.EXTERNAL_BACKUP)) {
                
                final File manifestEntry = new File(manifestCache, BackupUtil.Stamper.stamp(drive.driveLetter + ".txt"));
                WindowsBackupTools.createManifest(drive.drive, manifestEntry);
            }
            
            final File manifestBackup = BackupUtil.compressBackupCache(manifestCache, true, BackupUtil.Stamper.stamp(backupName));
            BackupUtil.commitBackup(localBackupDir, manifestBackup);
        }
        
        BackupUtil.syncBackupDir(localBackupDir, backupDir);
    }
    
    private static void backupRecoveryDrive() {
        logger.info("\n\n\n--- RECOVERY DRIVE ---\n");
        BackupUtil.clearTmpDir();
        
        if (!Drive.RECOVERY.available()) {
            logger.warn(BackupUtil.ERROR + "Drive: " + Drive.RECOVERY.driveLetter + " is not available");
            return;
        }
        
        final String backupName = "Recovery";
        
        final String password = PropertyUtil.readProperty("pass-recovery.txt");
        
        final File localDir = Drive.RECOVERY.drive;
        final File localBackupDir = new File(Drive.STORAGE.drive, Filesystem.generatePath("Other", "Backup", "Backups"));
        final File backupDir = new File(Drive.BACKUP.drive, "Backups");
        
        if (!BackupUtil.recentBackupExists(localBackupDir, backupName)) {
            
            final File recoveryManifestCache = new File(Filesystem.getTemporaryDirectory(), (backupName + "-manifest"));
            BackupUtil.makeBackupCache(recoveryManifestCache);
            
            final File recoveryManifestEntry = new File(recoveryManifestCache, BackupUtil.Stamper.stamp("manifest.txt"));
            WindowsBackupTools.createManifest(localDir, recoveryManifestEntry);
            
            final File recoveryManifestDir = new File(localDir, Filesystem.generatePath(".index", "manifest"));
            BackupUtil.commitBackup(recoveryManifestDir, recoveryManifestEntry);
            
            final File recoveryCache = new File(Filesystem.getTemporaryDirectory(), backupName);
            BackupUtil.makeBackupCache(recoveryCache);
            
            BackupUtil.addToBackupCache(recoveryCache, localDir, true, List.of("Videos"), true);
            
            final File recoveryBackup = BackupUtil.compressBackupCache(recoveryCache, true, BackupUtil.Stamper.stamp(backupName), false, password);
            BackupUtil.commitBackup(localBackupDir, recoveryBackup);
        }
        BackupUtil.cleanBackupDir(localBackupDir, backupName, 1);
        
        BackupUtil.syncBackupDir(localBackupDir, backupDir, backupName);
    }
    
    private static void backupSavedData() {
        logger.info("\n\n\n--- SAVED DATA ---\n");
        BackupUtil.clearTmpDir();
        
        final String backupName = "Data";
        
        final File localDir = new File(Drive.STORAGE.drive, Filesystem.generatePath("Other", "Backup", "Backups", backupName));
        final File backupDir = new File(Drive.BACKUP.drive, Filesystem.generatePath("Backups", backupName));
        
        BackupUtil.syncBackupDir(localDir, backupDir);
    }
    
    private static void backupSavedSettings() {
        logger.info("\n\n\n--- SAVED SETTINGS ---\n");
        BackupUtil.clearTmpDir();
        
        final String backupName = "Settings";
        
        final File localDir = new File(Drive.STORAGE.drive, Filesystem.generatePath("Other", "Backup", "Backups", backupName));
        final File backupDir = new File(Drive.BACKUP.drive, Filesystem.generatePath("Backups", backupName));
        
        BackupUtil.syncBackupDir(localDir, backupDir);
    }
    
    private static void backupUtilities() {
        logger.info("\n\n\n--- UTILITIES ---\n");
        BackupUtil.clearTmpDir();
        
        final String backupName = "Utilities";
        
        final File localDir = new File(Drive.STORAGE.drive, Filesystem.generatePath("Other", backupName));
        final File backupDir = new File(Drive.BACKUP.drive, backupName);
        
        BackupUtil.syncBackupDir(localDir, backupDir);
    }
    
    private static void backupTweaks() {
        logger.info("\n\n\n--- TWEAKS ---\n");
        BackupUtil.clearTmpDir();
        
        final String localName = "WindowsTweaks";
        final String backupName = "Tweaks";
        
        final File localDir = new File(Drive.CODING.drive, Filesystem.generatePath("Other", localName));
        final File localBackupDir = new File(Drive.STORAGE.drive, Filesystem.generatePath("Other", backupName));
        final File backupDir = new File(Drive.BACKUP.drive, backupName);
        
        BackupUtil.syncBackupDir(localDir, localBackupDir, List.of(".git", ".idea", "README.md"));
        BackupUtil.syncBackupDir(localBackupDir, backupDir);
    }
    
    private static void backupDevices() {
        logger.info("\n\n\n--- DEVICES ---\n");
        BackupUtil.clearTmpDir();
        
        final String backupName = "Devices";
        
        final File localDir = new File(Drive.VIRTUAL_MACHINES.drive, backupName);
        final File localBackupDir = new File(Drive.STORAGE.drive, Filesystem.generatePath("Other", "Backup", backupName));
        final File backupDir = new File(Drive.BACKUP.drive, backupName);
        
        for (File deviceTypeDir : BackupUtil.getSubDirs(localDir)) {
            for (File deviceDir : BackupUtil.getSubDirs(new File(deviceTypeDir, "Backups"))) {
                
                logger.info("\n--- Backing up " + deviceDir.getName() + " ---\n");
                
                final File deviceLocalDir = Optional.ofNullable(BackupUtil.Search.getNewest(deviceDir)).orElse(deviceDir);
                final File deviceLocalBackupDir = new File(localBackupDir, Filesystem.generatePath(deviceTypeDir.getName(), deviceDir.getName()));
                
                if (deviceTypeDir.getName().equals("Phone")) {
                    BackupUtil.cleanBackupDir(deviceDir, 6);
                }
                
                BackupUtil.syncBackupDir(deviceLocalDir, deviceLocalBackupDir);
                
                logger.info("\n-------------------" + StringUtility.fillStringOfLength('-', deviceDir.getName().length()) + "\n");
            }
        }
        
        BackupUtil.syncBackupDir(localBackupDir, backupDir);
    }
    
    private static void backupWorkPC() {
        logger.info("\n\n\n--- WORK PC ---\n");
        BackupUtil.clearTmpDir();
        
        final String backupName = "Work PC";
        
        final String name = PropertyUtil.readProperty("name-workPc.txt");
        final String password = PropertyUtil.readProperty("pass-workPc.txt");
        
        final File localDir = new File(Drive.VIRTUAL_MACHINES.drive, backupName);
        final File localDiskDir = Drive.WORK.drive;
        final File localBackupDir = new File(localDir, "Backup");
        final File localDiskBackupDir = new File(localDiskDir, "Backup");
        final File backupDir = new File(Drive.BACKUP.drive, backupName);
        
        if (!BackupUtil.recentBackupExists(localBackupDir, name)) {
            
            final File workPcCache = new File(Filesystem.getTemporaryDirectory(), name);
            BackupUtil.makeBackupCache(workPcCache);
            
            BackupUtil.addToBackupCache(workPcCache, localDir, true, List.of(localBackupDir.getName()), true);
            BackupUtil.addToBackupCache(workPcCache, localDiskDir, true, List.of(localDiskBackupDir.getName()), true);
            
            final File workPcBackup = BackupUtil.compressBackupCache(workPcCache, BackupUtil.Stamper.stamp(name), false, password);
            BackupUtil.commitBackup(localBackupDir, workPcBackup);
        }
        BackupUtil.cleanBackupDir(localBackupDir, name, 4);
        
        BackupUtil.syncBackupDir(localBackupDir, localDiskBackupDir, name);
        BackupUtil.syncBackupDir(localBackupDir, backupDir, name);
    }
    
    private static void backupWindows() {
        logger.info("\n\n\n--- WINDOWS ---\n");
        BackupUtil.clearTmpDir();
        
        final String backupName = "Windows";
        
        final String backupTarget = "\\\\localhost\\" + Drive.BACKUP.driveLetter + "$\\" + backupName + "\\";
        
        final File backupDir = new File(Drive.BACKUP.drive, backupName);
        
        if (!WindowsBackupTools.recentSystemImageExists(backupTarget)) {
            
            WindowsBackupTools.createSystemImage(backupTarget);
        }
    }
    
    private static void syncExternalBackup() {
        logger.info("\n\n\n--- SYNC EXTERNAL BACKUP ---\n");
        BackupUtil.clearTmpDir();
        
        if (!Drive.EXTERNAL_BACKUP.available()) {
            logger.warn(BackupUtil.ERROR + "Drive: " + Drive.EXTERNAL_BACKUP.driveLetter + " is not available");
            return;
        }
        
        final File localBackup = Drive.BACKUP.drive;
        final File backup = Drive.EXTERNAL_BACKUP.drive;
        
        for (File localBackupDir : BackupUtil.getSubDirs(localBackup)) {
            final File backupDir = new File(backup, localBackupDir.getName());
            
            if (BackupUtil.USE_RSYNC) {
                BackupUtil.rsyncBackupDir(localBackupDir, backupDir);
            } else {
                BackupUtil.syncBackupDir(localBackupDir, backupDir);
            }
        }
    }
    
}
